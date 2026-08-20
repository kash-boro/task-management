package com.taskmanager.controller;

import com.taskmanager.dto.UserDTO;
import com.taskmanager.entity.Role;
import com.taskmanager.entity.User;
import com.taskmanager.exception.DuplicateResourceException;
import com.taskmanager.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class UserController {

    @Autowired
    private UserService userService;

    // Admin-only -- enforced in SecurityConfig via /users/** -> hasRole("ADMIN")
    @GetMapping("/users")
    public String listUsers(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        model.addAttribute("roles", Role.values());

        // "newUser" arrives here via a flash attribute if this GET follows a
        // failed Add-User submit (see addUser() below). Otherwise give the
        // modal form a blank DTO to bind to.
        if (!model.containsAttribute("newUser")) {
            model.addAttribute("newUser", new UserDTO());
        }

        return "users";
    }

    @PostMapping("/users/add")
    public String addUser(@Valid @ModelAttribute("newUser") UserDTO userDTO,
                           BindingResult result,
                           RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            // Post-Redirect-Get, same pattern as the Add Task modal: redirect
            // back to /users carrying the errors + entered data + a flag to
            // reopen the modal, all as one-time flash attributes.
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.newUser", result);
            redirectAttributes.addFlashAttribute("newUser", userDTO);
            redirectAttributes.addFlashAttribute("showAddUserModal", true);
            return "redirect:/users";
        }

        try {
            userService.createUser(userDTO);
            redirectAttributes.addFlashAttribute("successMessage", "User Added Successfully");
        } catch (DuplicateResourceException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            redirectAttributes.addFlashAttribute("newUser", userDTO);
            redirectAttributes.addFlashAttribute("showAddUserModal", true);
        }

        return "redirect:/users";
    }

    @GetMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable Long id, Authentication authentication,
                              RedirectAttributes redirectAttributes) {
        User currentUser = userService.getUserByUsername(authentication.getName());

        try {
            userService.deleteUser(id, currentUser);
            redirectAttributes.addFlashAttribute("successMessage", "User Deleted Successfully");
        } catch (IllegalStateException ex) {
            // Thrown when trying to delete your own account or the last admin.
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/users";
    }

    // NOT under /users/**, so any logged-in user (not just admins) can reach
    // their own profile page.
    @GetMapping("/profile")
    public String profile(Model model, Authentication authentication) {
        User currentUser = userService.getUserByUsername(authentication.getName());
        model.addAttribute("username", currentUser.getUsername());
        model.addAttribute("name", currentUser.getName());
        model.addAttribute("email", currentUser.getEmail());
        model.addAttribute("role", currentUser.getRole());
        return "profile";
    }

    @PostMapping("/profile/change-password")
    public String changePassword(@RequestParam String currentPassword,
                                  @RequestParam String newPassword,
                                  @RequestParam String confirmPassword,
                                  Authentication authentication,
                                  RedirectAttributes redirectAttributes) {

        User currentUser = userService.getUserByUsername(authentication.getName());

        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("errorMessage", "New passwords do not match.");
            return "redirect:/profile";
        }

        boolean success = userService.changePassword(currentUser, currentPassword, newPassword);
        redirectAttributes.addFlashAttribute(
                success ? "successMessage" : "errorMessage",
                success ? "Password changed successfully." : "Current password is incorrect.");
        return "redirect:/profile";
    }

    // Lets the logged-in user delete their own account. After deleting we
    // must manually log them out -- their session is still technically
    // "authenticated" even though the underlying User row no longer exists.
    @PostMapping("/profile/delete-account")
    public String deleteOwnAccount(Authentication authentication,
                                    HttpServletRequest request,
                                    HttpServletResponse response,
                                    RedirectAttributes redirectAttributes) {

        User currentUser = userService.getUserByUsername(authentication.getName());

        try {
            userService.deleteOwnAccount(currentUser);
        } catch (IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/profile";
        }

        new SecurityContextLogoutHandler().logout(request, response, authentication);
        return "redirect:/login?accountDeleted";
    }
}
