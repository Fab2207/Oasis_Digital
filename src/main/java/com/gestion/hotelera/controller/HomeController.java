package com.gestion.hotelera.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private static final Logger logger = LoggerFactory.getLogger(HomeController.class);

    @GetMapping("/home")
    public String showHomePage(Model model, Authentication authentication) {
        boolean isLoggedIn = false;
        String username = "";
        if (authentication != null && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken)) {
            isLoggedIn = true;
            username = authentication.getName();
        }
        model.addAttribute("isLoggedIn", isLoggedIn);
        model.addAttribute("username", username);
        logger.info("GET /home -> HomeController.showHomePage invoked; isLoggedIn={}", isLoggedIn);
        return "index";
    }
}