package com.gestion.hotelera.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import com.gestion.hotelera.service.ClienteService;
import com.gestion.hotelera.service.HabitacionService;

@Controller
public class DashboardController {

    @Autowired
    private HabitacionService habitacionService;

    @Autowired
    private ClienteService clienteService;

    @GetMapping("/dashboard")
    public String dashboard(Model model, HttpSession session) {
        String role = (String) session.getAttribute("role");
        String username = (String) session.getAttribute("username");

        if (role == null) {
            return "redirect:/login";
        }
        
        long totalHabitaciones = habitacionService.listarTodasLasHabitaciones().size();
        long totalClientes = clienteService.listarTodosLosClientes().size();

        model.addAttribute("username", username);
        model.addAttribute("role", role);
        model.addAttribute("totalHabitaciones", totalHabitaciones);
        model.addAttribute("totalClientes", totalClientes);

        return "dashboard";
    }
}