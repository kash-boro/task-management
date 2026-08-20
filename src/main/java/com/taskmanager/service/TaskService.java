package com.taskmanager.service;

import com.taskmanager.dto.TaskDTO;
import com.taskmanager.entity.Priority;
import com.taskmanager.entity.Task;
import com.taskmanager.entity.TaskStatus;
import com.taskmanager.entity.User;
import com.taskmanager.exception.ResourceNotFoundException;
import com.taskmanager.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    // Handles search + filter + pagination + sorting all in one call --
    // Pageable already carries the sort order chosen by the controller.
    public Page<Task> searchTasks(User user, String keyword, TaskStatus status, Priority priority, Pageable pageable) {
        String cleanKeyword = (keyword == null || keyword.isBlank()) ? null : keyword.trim();
        return taskRepository.searchTasks(user, cleanKeyword, status, priority, pageable);
    }

    public Task getTaskByIdAndUser(Long id, User user) {
        return taskRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));
    }

    public void createTask(TaskDTO dto, User user) {
        Task task = new Task();
        task.setTitle(dto.getTitle());
        task.setDescription(dto.getDescription());
        task.setStatus(dto.getStatus());
        task.setPriority(dto.getPriority());
        task.setCategory(dto.getCategory());
        task.setDueDate(dto.getDueDate());
        task.setUser(user);
        taskRepository.save(task); // @PrePersist on Task fills createdDate automatically
    }

    public void updateTask(Long id, TaskDTO dto, User user) {
        Task task = getTaskByIdAndUser(id, user);
        task.setTitle(dto.getTitle());
        task.setDescription(dto.getDescription());
        task.setStatus(dto.getStatus());
        task.setPriority(dto.getPriority());
        task.setCategory(dto.getCategory());
        task.setDueDate(dto.getDueDate());
        taskRepository.save(task);
    }

    public void deleteTask(Long id, User user) {
        taskRepository.delete(getTaskByIdAndUser(id, user));
    }

    // Converts an entity into the DTO the edit form needs.
    public TaskDTO mapToDTO(Task task) {
        TaskDTO dto = new TaskDTO();
        dto.setId(task.getId());
        dto.setTitle(task.getTitle());
        dto.setDescription(task.getDescription());
        dto.setStatus(task.getStatus());
        dto.setPriority(task.getPriority());
        dto.setCategory(task.getCategory());
        dto.setDueDate(task.getDueDate());
        return dto;
    }

    // True only when there's a due date, it's in the past, and the task
    // isn't already completed. Templates use this to show the OVERDUE badge.
    public boolean isOverdue(Task task) {
        return task.getDueDate() != null
                && task.getDueDate().isBefore(LocalDate.now())
                && task.getStatus() != TaskStatus.COMPLETED;
    }

    // Maps a status to a colored emoji badge for the task list/dashboard.
    public String statusEmoji(TaskStatus status) {
        return switch (status) {
            case COMPLETED -> "\uD83D\uDFE2"; // green circle
            case IN_PROGRESS -> "\uD83D\uDFE1"; // yellow circle
            default -> "\uD83D\uDD34"; // red circle
        };
    }

    // --- Dashboard statistics ---

    public long countTotalTasks(User user) {
        return taskRepository.findByUser(user).size();
    }

    public long countByStatus(User user, TaskStatus status) {
        return taskRepository.countByUserAndStatus(user, status);
    }

    public long countOverdue(User user) {
        return taskRepository.countByUserAndDueDateBeforeAndStatusNot(user, LocalDate.now(), TaskStatus.COMPLETED);
    }

    public List<Task> getRecentTasks(User user) {
        return taskRepository.findTop5ByUserOrderByCreatedDateDesc(user);
    }
}
