package com.taskmanager.dto;

import com.taskmanager.entity.Category;
import com.taskmanager.entity.Priority;
import com.taskmanager.entity.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

// This DTO is what the Thymeleaf form binds to (th:object).
// We never bind the form directly to the Task entity -- that would let a user
// tamper with fields like "id" or "user" via the form. The DTO only exposes
// the fields we actually want the user to edit.
public class TaskDTO {

    private Long id;

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    @NotNull(message = "Status is required")
    private TaskStatus status;

    @NotNull(message = "Priority is required")
    private Priority priority;

    private Category category;

    private LocalDate dueDate;

    public TaskDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }
}
