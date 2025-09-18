package com.gestion.hotelera.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String username,
                        @RequestParam String password,
                        HttpSession session,
                        Model model) {
        if ("admin".equals(username) && "admin123".equals(password)) {
            session.setAttribute("role", "ADMIN");
            session.setAttribute("username", username);
            return "redirect:/dashboard";
        } else if ("recep".equals(username) && "recep123".equals(password)) {
            session.setAttribute("role", "RECEPCIONISTA");
            session.setAttribute("username", username);
            return "redirect:/dashboard";
        }
        model.addAttribute("error", "Credenciales incorrectas");
        return "login";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}