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
@RequestMapping("/cliente/reservas")
public class ClienteReservaController {

    private final ClienteService clienteService;
    private final HabitacionService habitacionService;
    private final ReservaService reservaService;

    public ClienteReservaController(ClienteService clienteService,
            HabitacionService habitacionService,
            ReservaService reservaService) {
        this.clienteService = clienteService;
        this.habitacionService = habitacionService;
        this.reservaService = reservaService;
    }

    @GetMapping("/crear")
    public String mostrarFormularioReserva(Model model,
            Authentication auth,
            @RequestParam(name = "habitacionId", required = false) Long habitacionId) {
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/login";
        }

        Cliente cliente = clienteService.obtenerPorUsername(auth.getName());
        if (cliente == null) {
            return "redirect:/login";
        }

        model.addAttribute("cliente", cliente);
        model.addAttribute("reserva", new Reserva());
        model.addAttribute("habitacionesDisponibles", habitacionService.obtenerHabitacionesDisponibles());
        if (habitacionId != null) {
            model.addAttribute("habitacionId", habitacionId);
        }
        return "generarReserva";
    }

    @PostMapping("/guardar")
    public String guardarReserva(@ModelAttribute Reserva reserva,
            @RequestParam("habitacionId") Long habitacionId,
            Authentication auth,
            RedirectAttributes redirectAttributes) {
        try {
            Cliente cliente = clienteService.obtenerPorUsername(auth.getName());
            reserva.setCliente(cliente);
            reserva.setHabitacion(new Habitacion(habitacionId));

            if (reserva.getFechaInicio().isBefore(LocalDate.now())) {
                redirectAttributes.addFlashAttribute("errorMessage", "La fecha de inicio no puede ser anterior a hoy.");
                return "redirect:/cliente/reservas/crear";
            }

            if (reserva.getFechaInicio().isEqual(LocalDate.now())) {
                reserva.setEstadoReserva("ACTIVA");
            } else {
                reserva.setEstadoReserva("PENDIENTE");
            }

            reservaService.crearOActualizarReserva(reserva);

            redirectAttributes.addFlashAttribute("successMessage", "✅ Reserva creada exitosamente.");
            return "redirect:/dashboard";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al crear la reserva: " + e.getMessage());
            return "redirect:/cliente/reservas/crear";
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
            return "{\"error\": \"Habitación no encontrada\"}";
        }

        Habitacion habitacion = habitacionOptional.get();
        Integer dias = reservaService.calcularDiasEstadia(fechaInicio, fechaFin);
        Double total = reservaService.calcularTotalPagar(habitacion.getPrecioPorNoche(), dias);
        return String.format("{\"dias\": %d, \"total\": %.2f}", dias, total);
    }
}