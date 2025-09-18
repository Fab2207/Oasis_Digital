package com.gestion.hotelera.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import com.gestion.hotelera.service.ClienteService;
import com.gestion.hotelera.service.HabitacionService;

@Controller
public class DashboardController {

    private final HabitacionService habitacionService;
    private final ClienteService clienteService;

    public DashboardController(HabitacionService habitacionService, ClienteService clienteService) {
        this.habitacionService = habitacionService;
        this.clienteService = clienteService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, HttpSession session) {
        String role = (String) session.getAttribute("role");
        String username = (String) session.getAttribute("username");

        if (role == null) {
            return "redirect:/login";
        }

        long totalHabitaciones = habitacionService.listarHabitaciones().size();
        long totalClientes = clienteService.listarClientes().size();
        long disponibles = habitacionService.listarPorEstado("Disponible").size();
        long ocupadas = habitacionService.listarPorEstado("Ocupada").size();
        long mantenimiento = habitacionService.listarPorEstado("Mantenimiento").size();

        model.addAttribute("username", username);
        model.addAttribute("role", role);
        model.addAttribute("totalHabitaciones", totalHabitaciones);
        model.addAttribute("totalClientes", totalClientes);
        model.addAttribute("disponible", disponibles);
        model.addAttribute("ocupada", ocupadas);
        model.addAttribute("mantenimiento", mantenimiento);

        return "dashboard";
    }
}