package com.taskmanager.controller;

import com.taskmanager.dto.RegisterRequest;
import com.taskmanager.exception.DuplicateResourceException;
import com.taskmanager.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

// Public controller -- SecurityConfig permits "/register" without login,
// same as "/login" itself. This is how a brand-new person creates an
// account with their own name/email instead of an admin creating it for them.
@Controller
public class RegisterController {

    @Autowired
    private UserService userService;

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("registerRequest", new RegisterRequest());
        return "register"; // -> register.html
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("registerRequest") RegisterRequest request,
                            BindingResult result,
                            Model model) {

        if (!request.getPassword().equals(request.getConfirmPassword())) {
            result.rejectValue("confirmPassword", "mismatch", "Passwords do not match");
        }

        if (result.hasErrors()) {
            // Re-render the same page (not a redirect) so the validation
            // messages show immediately -- there's no modal to reopen here,
            // registration is a full standalone page.
            return "register";
        }

        try {
            userService.registerUser(request);
        } catch (DuplicateResourceException ex) {
            result.rejectValue("email", "duplicate", ex.getMessage());
            return "register";
        }

        return "redirect:/login?registered";
    }
}
