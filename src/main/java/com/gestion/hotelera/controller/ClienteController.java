package com.gestion.hotelera.controller;

import com.gestion.hotelera.model.Cliente;
import com.gestion.hotelera.model.Reserva;
import com.gestion.hotelera.service.ClienteService;
import com.gestion.hotelera.service.ReservaService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/clientes")
public class ClienteController {

    private final ClienteService clienteService;
    private final ReservaService reservaService;

    public ClienteController(ClienteService clienteService, ReservaService reservaService) {
        this.clienteService = clienteService;
        this.reservaService = reservaService;
    }
    @GetMapping("/editar/{id}")
    public String editarCliente(@PathVariable Long id, Model model, Authentication auth) {
        // Permitir que el propio cliente edite su perfil
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/login";
        }
        Optional<Cliente> clienteOpt = clienteService.buscarClientePorId(id);
        if (clienteOpt.isEmpty()) {
            return "redirect:/";
        }
        Cliente cliente = clienteOpt.get();
        model.addAttribute("cliente", cliente);
        return "editarCliente";
    }

    @PostMapping("/editar/{id}")
    public String actualizarCliente(@PathVariable Long id,
                                    @ModelAttribute("cliente") Cliente cliente,
                                    RedirectAttributes redirectAttributes,
                                    Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/login";
        }
        try {
            cliente.setId(id);
            clienteService.actualizarCliente(cliente);
            redirectAttributes.addFlashAttribute("successMessage", "Datos actualizados correctamente.");
            return "redirect:/dashboard";
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/clientes/editar/" + id;
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al actualizar el cliente.");
            return "redirect:/clientes/editar/" + id;
        }
    }

    @GetMapping("/registrar")
    public String mostrarFormularioRegistro(Model model) {
        model.addAttribute("cliente", new Cliente());
        return "registroCliente";
    }

    @PostMapping("/guardar")
    public String guardarCliente(@ModelAttribute("cliente") Cliente cliente,
                                 RedirectAttributes redirectAttributes) {
        try {
            Cliente guardado = clienteService.crearCliente(cliente);
            redirectAttributes.addFlashAttribute("successMessage", "Cliente registrado correctamente: "
                    + guardado.getNombres() + " " + guardado.getApellidos());
            // Redirigir a flujo de reserva para el cliente recién creado
            return "redirect:/reservas/crear?idCliente=" + guardado.getId();
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            redirectAttributes.addFlashAttribute("cliente", cliente);
            return "redirect:/clientes/registrar";
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Ocurrió un error al registrar el cliente.");
            redirectAttributes.addFlashAttribute("cliente", cliente);
            return "redirect:/clientes/registrar";
        }
    }

    @GetMapping("/dashboard")
    public String mostrarDashboardCliente(Model model, Authentication auth) {
        model.addAttribute("username", auth.getName());
        model.addAttribute("rol", auth.getAuthorities());
        return "cliente/dashboard";
    }

    @GetMapping("/historial")
    public String mostrarHistorialClientes(
            Model model,
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @RequestParam(value = "size", required = false, defaultValue = "10") int size,
            @RequestParam(value = "sortBy", required = false, defaultValue = "id") String sortBy,
            @RequestParam(value = "sortDir", required = false, defaultValue = "asc") String sortDir,
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "id", required = false) Long id,
            @RequestParam(value = "dni", required = false) String dni
    ) {
        if (dni != null && !dni.isBlank()) {
            Optional<Cliente> porDni = clienteService.buscarPorDniOptional(dni);
            if (porDni.isPresent()) {
                Cliente cliente = porDni.get();
                model.addAttribute("cliente", cliente);
                List<Reserva> reservasCliente = reservaService.obtenerReservasPorClienteId(cliente.getId());
                model.addAttribute("reservasCliente", reservasCliente);
                return "historialCliente";
            } else {
                model.addAttribute("errorMessage", "No se encontró cliente con DNI: " + dni);
            }
        }

        if (id != null) {
            Optional<Cliente> clienteOpt = clienteService.obtenerClientePorId(id);
            if (clienteOpt.isPresent()) {
                Cliente cliente = clienteOpt.get();
                model.addAttribute("cliente", cliente);
                List<Reserva> reservasCliente = reservaService.obtenerReservasPorClienteId(cliente.getId());
                model.addAttribute("reservasCliente", reservasCliente);
                return "historialCliente";
            } else {
                model.addAttribute("errorMessage", "Cliente no encontrado.");
            }
        }

        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        PageRequest pageRequest = PageRequest.of(Math.max(page, 0), Math.max(size, 1), sort);
        Page<Cliente> clientesPage = clienteService.obtenerClientesPaginados(pageRequest, search);
        model.addAttribute("clientesPage", clientesPage);
        model.addAttribute("currentPage", clientesPage.getNumber());
        model.addAttribute("pageSize", clientesPage.getSize());
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("search", search);
        return "historialCliente";
    }

    @PostMapping("/eliminar/{id}")
    public String eliminarCliente(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        boolean eliminado = clienteService.eliminarClientePorId(id);
        if (eliminado) {
            redirectAttributes.addFlashAttribute("successMessage", "Cliente eliminado correctamente.");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "No se pudo eliminar el cliente.");
        }
        return "redirect:/clientes/historial";
    }
}