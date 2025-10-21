package com.gestion.hotelera.controller;

import com.gestion.hotelera.model.Cliente;
import com.gestion.hotelera.model.Reserva;
import com.gestion.hotelera.service.ClienteService;
import com.gestion.hotelera.service.ReservaService;
import com.gestion.hotelera.service.HabitacionService;
import com.gestion.hotelera.service.EmpleadoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.List;

@Controller
public class DashboardController {

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private ReservaService reservaService;

    @Autowired
    private HabitacionService habitacionService;

    @Autowired
    private EmpleadoService empleadoService;

    @GetMapping("/dashboard")
    public String mostrarDashboard(Model model, Authentication auth) {
        var roles = auth.getAuthorities();
        model.addAttribute("roles", roles);

        if (roles.stream().anyMatch(r -> r.getAuthority().equals("ROLE_CLIENTE"))) {
            Cliente cliente = clienteService.obtenerPorUsername(auth.getName());
            if (cliente != null) {
                List<Reserva> reservas = reservaService.obtenerReservasPorCliente(cliente);
                long reservasActivas = reservas.stream()
                        .filter(r -> r.getEstadoReserva().equalsIgnoreCase("ACTIVA")
                                || r.getEstadoReserva().equalsIgnoreCase("PENDIENTE"))
                        .count();
                long reservasFinalizadas = reservas.stream()
                        .filter(r -> r.getEstadoReserva().equalsIgnoreCase("FINALIZADA"))
                        .count();
                model.addAttribute("cliente", cliente);
                model.addAttribute("reservas", reservas);
                model.addAttribute("reservasActivas", reservasActivas);
                model.addAttribute("reservasFinalizadas", reservasFinalizadas);
                model.addAttribute("isLoggedIn", true);
                model.addAttribute("username", cliente.getNombres());
                return "dashboard";
            }
        }

        if (roles.stream().anyMatch(r ->
                r.getAuthority().equals("ROLE_ADMIN") || r.getAuthority().equals("ROLE_RECEPCIONISTA"))) {
            long totalHabitaciones = habitacionService.contarHabitaciones();
            long totalClientes = clienteService.contarClientes();
            long totalReservas = reservaService.contarReservas();
            long habitacionesDisponibles = habitacionService.contarDisponibles();
            long habitacionesOcupadas = habitacionService.contarOcupadas();
            long habitacionesMantenimiento = habitacionService.contarEnMantenimiento();
            long totalEmpleados = empleadoService.contarEmpleados();
            double ingresosTotales = reservaService.calcularIngresosTotales();
            long reservasPendientes = reservaService.contarReservasPorEstado("PENDIENTE");
            long reservasActivas = reservaService.contarReservasPorEstado("ACTIVA");
            long checkInsHoy = reservaService.contarCheckInsHoy();
            long checkOutsHoy = reservaService.contarCheckOutsHoy();

            model.addAttribute("totalHabitaciones", totalHabitaciones);
            model.addAttribute("totalClientes", totalClientes);
            model.addAttribute("totalReservas", totalReservas);
            model.addAttribute("habitacionesDisponibles", habitacionesDisponibles);
            model.addAttribute("habitacionesOcupadas", habitacionesOcupadas);
            model.addAttribute("habitacionesMantenimiento", habitacionesMantenimiento);
            model.addAttribute("totalEmpleados", totalEmpleados);
            model.addAttribute("ingresosTotales", ingresosTotales);
            model.addAttribute("reservasPendientes", reservasPendientes);
            model.addAttribute("reservasActivas", reservasActivas);
            model.addAttribute("checkInsHoy", checkInsHoy);
            model.addAttribute("checkOutsHoy", checkOutsHoy);

            return "dashboard";
        }

        return "redirect:/login";
    }
}