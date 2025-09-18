package com.gestion.hotelera.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.gestion.hotelera.service.ClienteService;

@Controller
public class HistorialController {

    @Autowired
    private ClienteService clienteService;

    @GetMapping("/historial")
    public String historial(@RequestParam(required = false) String dni, Model model) {
        if (dni != null) {
            model.addAttribute("cliente", clienteService.buscarPorDni(dni).orElse(null));
        }
        return "historial";
    }
}