package com.taskmanager.controller;

import com.taskmanager.dto.TaskDTO;
import com.taskmanager.entity.Category;
import com.taskmanager.entity.Priority;
import com.taskmanager.entity.Task;
import com.taskmanager.entity.TaskStatus;
import com.taskmanager.entity.User;
import com.taskmanager.service.TaskService;
import com.taskmanager.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
public class TaskController {

    @Autowired
    private TaskService taskService;

    @Autowired
    private UserService userService;

    // Helper: figures out which User entity is currently logged in, based on
    // the username Spring Security stores in the Authentication object.
    private User getCurrentUser(Authentication authentication) {
        return userService.getUserByUsername(authentication.getName());
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication authentication) {
        User currentUser = getCurrentUser(authentication);

        model.addAttribute("username", currentUser.getUsername());
        // Self-registered users have a real name; the seeded demo accounts
        // don't, so fall back to their username in that case.
        model.addAttribute("displayName",
                (currentUser.getName() != null && !currentUser.getName().isBlank())
                        ? currentUser.getName() : currentUser.getUsername());
        model.addAttribute("role", currentUser.getRole());
        model.addAttribute("totalTasks", taskService.countTotalTasks(currentUser));
        model.addAttribute("completedTasks", taskService.countByStatus(currentUser, TaskStatus.COMPLETED));
        model.addAttribute("pendingTasks", taskService.countByStatus(currentUser, TaskStatus.PENDING));
        model.addAttribute("overdueTasks", taskService.countOverdue(currentUser));
        model.addAttribute("recentTasks", taskService.getRecentTasks(currentUser));
        // Passed into the template so it can call taskService.isOverdue(task) / statusEmoji(status) directly
        model.addAttribute("taskService", taskService);

        return "dashboard";
    }

    @GetMapping("/tasks")
    public String listTasks(@RequestParam(required = false) String keyword,
                             @RequestParam(required = false) TaskStatus status,
                             @RequestParam(required = false) Priority priority,
                             @RequestParam(defaultValue = "dueDate") String sortBy,
                             @RequestParam(defaultValue = "0") int page,
                             Model model,
                             Authentication authentication) {

        User currentUser = getCurrentUser(authentication);

        Sort sort = switch (sortBy) {
            case "priority" -> Sort.by("priority").descending();
            case "createdDate" -> Sort.by("createdDate").descending();
            case "title" -> Sort.by("title").ascending();
            default -> Sort.by("dueDate").ascending();
        };
        Pageable pageable = PageRequest.of(page, 5, sort);
        Page<Task> taskPage = taskService.searchTasks(currentUser, keyword, status, priority, pageable);

        model.addAttribute("taskPage", taskPage);
        model.addAttribute("tasks", taskPage.getContent());
        model.addAttribute("keyword", keyword);
        model.addAttribute("status", status);
        model.addAttribute("priority", priority);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("taskService", taskService);
        model.addAttribute("statuses", TaskStatus.values());
        model.addAttribute("priorities", Priority.values());
        model.addAttribute("categories", Category.values());

        // "task" arrives here via a flash attribute if this GET follows a
        // failed Add-Task submit (see addTask() below). Otherwise give the
        // modal form a blank DTO to bind to.
        if (!model.containsAttribute("task")) {
            model.addAttribute("task", new TaskDTO());
        }

        return "tasks";
    }

    @PostMapping("/tasks/add")
    public String addTask(@Valid @ModelAttribute("task") TaskDTO taskDTO,
                           BindingResult result,
                           Authentication authentication,
                           RedirectAttributes redirectAttributes) {

        // New tasks can't have a due date in the past.
        if (taskDTO.getDueDate() != null && taskDTO.getDueDate().isBefore(LocalDate.now())) {
            result.rejectValue("dueDate", "invalid", "Invalid Due Date");
        }

        if (result.hasErrors()) {
            // Post-Redirect-Get: redirect back to /tasks (so refreshing the
            // page never resubmits the form) while passing the validation
            // errors, the entered data, and a flag to reopen the modal --
            // all as one-time flash attributes.
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.task", result);
            redirectAttributes.addFlashAttribute("task", taskDTO);
            redirectAttributes.addFlashAttribute("showAddModal", true);
            return "redirect:/tasks";
        }

        taskService.createTask(taskDTO, getCurrentUser(authentication));
        redirectAttributes.addFlashAttribute("successMessage", "Task Added Successfully");
        return "redirect:/tasks";
    }

    @GetMapping("/tasks/edit/{id}")
    public String showEditTaskForm(@PathVariable Long id, Model model, Authentication authentication) {
        Task task = taskService.getTaskByIdAndUser(id, getCurrentUser(authentication));

        model.addAttribute("task", taskService.mapToDTO(task));
        model.addAttribute("statuses", TaskStatus.values());
        model.addAttribute("priorities", Priority.values());
        model.addAttribute("categories", Category.values());
        return "edit-task";
    }

    @PostMapping("/tasks/edit/{id}")
    public String editTask(@PathVariable Long id,
                            @Valid @ModelAttribute("task") TaskDTO taskDTO,
                            BindingResult result,
                            Model model,
                            Authentication authentication,
                            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            model.addAttribute("statuses", TaskStatus.values());
            model.addAttribute("priorities", Priority.values());
            model.addAttribute("categories", Category.values());
            return "edit-task";
        }

        taskService.updateTask(id, taskDTO, getCurrentUser(authentication));
        redirectAttributes.addFlashAttribute("successMessage", "Task Updated Successfully");
        return "redirect:/tasks";
    }

    @GetMapping("/tasks/delete/{id}")
    public String deleteTask(@PathVariable Long id, Authentication authentication,
                              RedirectAttributes redirectAttributes) {
        taskService.deleteTask(id, getCurrentUser(authentication));
        redirectAttributes.addFlashAttribute("successMessage", "Task Deleted Successfully");
        return "redirect:/tasks";
    }

    // Exports the current user's tasks as a downloadable CSV file.
    // (Excel/.xlsx export would need the Apache POI library -- CSV covers
    // the "export" requirement without adding a new dependency.)
    @GetMapping("/tasks/export")
    @ResponseBody
    public ResponseEntity<String> exportTasksCsv(Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        List<Task> tasks = taskService.searchTasks(currentUser, null, null, null,
                PageRequest.of(0, Integer.MAX_VALUE)).getContent();

        StringBuilder csv = new StringBuilder("ID,Title,Description,Status,Priority,Category,Due Date\n");
        for (Task t : tasks) {
            csv.append(t.getId()).append(",")
               .append(escapeCsv(t.getTitle())).append(",")
               .append(escapeCsv(t.getDescription())).append(",")
               .append(t.getStatus()).append(",")
               .append(t.getPriority()).append(",")
               .append(t.getCategory() == null ? "" : t.getCategory()).append(",")
               .append(t.getDueDate() == null ? "" : t.getDueDate())
               .append("\n");
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=tasks.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv.toString());
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
