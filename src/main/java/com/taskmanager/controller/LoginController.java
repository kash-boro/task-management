package com.taskmanager.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

// @Controller (not @RestController) because this returns the NAME of an HTML
// template for Thymeleaf to render, not raw JSON data.
@Controller
public class LoginController {

    // Spring Security's formLogin() is configured to use this exact URL as
    // the login page (see SecurityConfig.loginPage("/login")).
    @GetMapping("/login")
    public String loginPage() {
        return "login"; // resolves to src/main/resources/templates/login.html
    }

    // A friendly redirect so visiting the bare root URL doesn't 404.
    @GetMapping("/")
    public String home() {
        return "redirect:/dashboard";
    }
}
