package com.taskmanager.repository;

import com.taskmanager.entity.Priority;
import com.taskmanager.entity.Task;
import com.taskmanager.entity.TaskStatus;
import com.taskmanager.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByUser(User user);

    Optional<Task> findByIdAndUser(Long id, User user);

    long countByUserAndStatus(User user, TaskStatus status);

    // Overdue = due date before today AND not already completed
    long countByUserAndDueDateBeforeAndStatusNot(User user, LocalDate today, TaskStatus status);

    List<Task> findTop5ByUserOrderByCreatedDateDesc(User user);

    // Each condition is skipped (matches everything) when its parameter is
    // null -- lets one query handle search + filter + neither + both together.
    @Query("SELECT t FROM Task t WHERE t.user = :user " +
           "AND (:keyword IS NULL OR LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "     OR LOWER(t.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:status IS NULL OR t.status = :status) " +
           "AND (:priority IS NULL OR t.priority = :priority)")
    Page<Task> searchTasks(@Param("user") User user,
                            @Param("keyword") String keyword,
                            @Param("status") TaskStatus status,
                            @Param("priority") Priority priority,
                            Pageable pageable);
}
