package com.gestion.hotelera.controller;

import com.gestion.hotelera.model.Cliente;
import com.gestion.hotelera.service.ClienteService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthLoginController {

    private final ClienteService clienteService;

    public AuthLoginController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @GetMapping("/")
    public String mostrarIndex(Model model, Authentication auth) {
        boolean isLoggedIn = auth != null && auth.isAuthenticated();
        String username = null;
        String rol = null;

        if (isLoggedIn && auth != null) {
            // SI ES PERSONAL, MOSTRAR INDEX IGUAL (según pedido): no redirigimos al dashboard

            Cliente cliente = clienteService.obtenerPorUsername(auth.getName());
            if (cliente != null) {
                username = cliente.getNombres();
                rol = "ROLE_CLIENTE";
            } else {
                username = auth.getName();
            }
        }

        model.addAttribute("isLoggedIn", isLoggedIn);
        model.addAttribute("username", username);
        model.addAttribute("rol", rol);
        return "index";
    }

    @GetMapping("/login")
    public String showLoginPage() {
        return "login";
    }

    @GetMapping("/registro")
    public String showRegisterPage() {
        return "register";
    }
}