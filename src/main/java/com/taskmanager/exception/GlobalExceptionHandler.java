package com.taskmanager.exception;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

// @ControllerAdvice (not @RestControllerAdvice) because our controllers return
// view names (HTML pages), not JSON. This class catches exceptions thrown from
// ANY controller and shows a friendly error page instead of a raw stack trace.
@ControllerAdvice
public class GlobalExceptionHandler {

    // Triggered when TaskService can't find a task/user that was requested.
    @ExceptionHandler(ResourceNotFoundException.class)
    public String handleResourceNotFound(ResourceNotFoundException ex, Model model) {
        model.addAttribute("errorMessage", ex.getMessage());
        model.addAttribute("statusCode", 404);
        return "error";
    }

    // Triggered when trying to register a username that's already taken.
    @ExceptionHandler(DuplicateResourceException.class)
    public String handleDuplicateResource(DuplicateResourceException ex, Model model) {
        model.addAttribute("errorMessage", ex.getMessage());
        model.addAttribute("statusCode", 409);
        return "error";
    }

    // Catch-all fallback for anything unexpected, so the user never sees a raw
    // Whitelabel Error Page or stack trace during the demo.
    @ExceptionHandler(Exception.class)
    public String handleGeneralException(Exception ex, Model model) {
        model.addAttribute("errorMessage", "Something went wrong: " + ex.getMessage());
        model.addAttribute("statusCode", 500);
        return "error";
    }
}
