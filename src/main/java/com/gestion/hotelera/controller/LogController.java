package com.gestion.hotelera.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LogController {

    @GetMapping("/logs")
    public String logs() {
        return "logs";
    }
}