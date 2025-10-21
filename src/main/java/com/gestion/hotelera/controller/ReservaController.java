package com.gestion.hotelera.controller;

import com.gestion.hotelera.model.Cliente;
import com.gestion.hotelera.model.Habitacion;
import com.gestion.hotelera.model.Reserva;
import com.gestion.hotelera.service.ClienteService;
import com.gestion.hotelera.service.HabitacionService;
import com.gestion.hotelera.service.ReservaService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.Optional;

@Controller
@RequestMapping("/reservas")
public class ReservaController {

    private final ClienteService clienteService;
    private final HabitacionService habitacionService;
    private final ReservaService reservaService;

    public ReservaController(ClienteService clienteService, HabitacionService habitacionService, ReservaService reservaService) {
        this.clienteService = clienteService;
        this.habitacionService = habitacionService;
        this.reservaService = reservaService;
    }

    @GetMapping
    public String mostrarReservasCliente(Model model, Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/login";
        }
        Cliente cliente = clienteService.obtenerPorEmail(auth.getName());
        model.addAttribute("clienteEncontrado", cliente);
        model.addAttribute("reserva", new Reserva());
        model.addAttribute("habitacionesDisponibles", habitacionService.obtenerHabitacionesDisponibles());
        return "reservas";
    }

    @GetMapping("/crear")
    public String showCrearReservaForm(Model model,
                                       @RequestParam(name = "dni", required = false) String dni,
                                       @RequestParam(name = "idCliente", required = false) Long idCliente) {
        model.addAttribute("cliente", new Cliente());
        model.addAttribute("reserva", new Reserva());
        model.addAttribute("habitacionesDisponibles", habitacionService.obtenerHabitacionesDisponibles());
        if (idCliente != null) {
            Optional<Cliente> clientePorId = clienteService.obtenerClientePorId(idCliente);
            if (clientePorId.isPresent()) {
                model.addAttribute("clienteEncontrado", clientePorId.get());
                model.addAttribute("cliente", clientePorId.get());
            } else {
                model.addAttribute("errorMessage", "Cliente con ID " + idCliente + " no encontrado.");
            }
        } else if (dni != null && !dni.trim().isEmpty()) {
            Optional<Cliente> clienteOptional = clienteService.buscarClientePorDni(dni);
            if (clienteOptional.isPresent()) {
                model.addAttribute("clienteEncontrado", clienteOptional.get());
                model.addAttribute("cliente", clienteOptional.get());
            } else {
                model.addAttribute("errorMessage", "Cliente con DNI " + dni + " no encontrado.");
            }
        }
        return "reservas";
    }

    @PostMapping("/buscar-cliente")
    public String buscarClienteParaReserva(@RequestParam("dniBuscar") String dni, RedirectAttributes redirectAttributes) {
        Optional<Cliente> clienteOptional = clienteService.buscarClientePorDni(dni);
        if (clienteOptional.isPresent()) {
            redirectAttributes.addFlashAttribute("successMessage", "Cliente encontrado!");
            return "redirect:/reservas/crear?dni=" + dni;
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Cliente con DNI " + dni + " no encontrado. Por favor, regístrelo primero.");
            return "redirect:/reservas/crear";
        }
    }

    @GetMapping("/calcular-costo")
    @ResponseBody
    public String calcularCosto(
            @RequestParam("habitacionId") Long habitacionId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {

        Optional<Habitacion> habitacionOptional = habitacionService.buscarHabitacionPorId(habitacionId);
        if (habitacionOptional.isEmpty()) {
            return "Error: Habitación no encontrada";
        }

        Habitacion habitacion = habitacionOptional.get();
        Integer dias = reservaService.calcularDiasEstadia(fechaInicio, fechaFin);
        Double total = reservaService.calcularTotalPagar(habitacion.getPrecioPorNoche(), dias);
        return String.format("{\"dias\": %d, \"total\": %.2f}", dias, total);
    }

    @PostMapping("/guardar")
    public String guardarReserva(@ModelAttribute Reserva reserva,
                                 @RequestParam("clienteDni") String clienteDni,
                                 @RequestParam("habitacionId") Long habitacionId,
                                 RedirectAttributes redirectAttributes,
                                 Authentication auth) {
        try {
            Optional<Cliente> clienteOptional = clienteService.buscarClientePorDni(clienteDni);
            if (clienteOptional.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Error: Cliente no encontrado para el DNI proporcionado.");
                return "redirect:/reservas";
            }
            reserva.setCliente(clienteOptional.get());
            reserva.setHabitacion(new Habitacion(habitacionId));
            if (reserva.getFechaInicio().isBefore(LocalDate.now())) {
                redirectAttributes.addFlashAttribute("errorMessage", "La fecha de inicio de la reserva no puede ser anterior a la fecha actual.");
                return "redirect:/reservas";
            }
            if (reserva.getFechaInicio().isEqual(LocalDate.now())) {
                reserva.setEstadoReserva("ACTIVA");
            } else {
                reserva.setEstadoReserva("PENDIENTE");
            }
            reservaService.crearOActualizarReserva(reserva);
            redirectAttributes.addFlashAttribute("successMessage", "Reserva creada exitosamente!");

            // Redirección inteligente por rol
            if (auth != null && auth.isAuthenticated() &&
                    auth.getAuthorities().stream().anyMatch(a ->
                            "ROLE_ADMIN".equals(a.getAuthority()) || "ROLE_RECEPCIONISTA".equals(a.getAuthority()))) {
                Long clienteId = clienteOptional.get().getId();
                return "redirect:/clientes/historial?id=" + clienteId;
            }

            return "redirect:/reservas";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al crear la reserva: " + e.getMessage());
            return "redirect:/reservas";
        }
    }

    @PostMapping("/cancelar/{id}")
    public String cancelarReserva(@PathVariable Long id, RedirectAttributes redirectAttributes, @RequestHeader(value = "Referer", required = false) String referer) {
        if (reservaService.cancelarReserva(id)) {
            redirectAttributes.addFlashAttribute("successMessage", "Reserva cancelada exitosamente.");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "No se pudo cancelar la reserva.");
        }
        return "redirect:" + (referer != null ? referer : "/dashboard");
    }

    @PostMapping("/finalizar/{id}")
    public String finalizarReserva(@PathVariable Long id, RedirectAttributes redirectAttributes, @RequestHeader(value = "Referer", required = false) String referer) {
        if (reservaService.finalizarReserva(id)) {
            redirectAttributes.addFlashAttribute("successMessage", "Reserva finalizada (check-out) exitosamente.");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "No se pudo finalizar la reserva.");
        }
        return "redirect:" + (referer != null ? referer : "/dashboard");
    }
}