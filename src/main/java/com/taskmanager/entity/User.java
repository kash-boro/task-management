package com.taskmanager.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // This remains the identifier Spring Security logs in with. For
    // self-registered accounts we set this to the person's email address;
    // for the seeded demo accounts (admin/user) it stays a plain username.
    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    // Full name collected at registration -- purely for display (e.g. "Welcome, Jane Doe").
    private String name;

    // Kept as its own column (in addition to username) so we can enforce
    // email uniqueness independently and query by email if ever needed.
    @Column(unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    // One user owns many tasks. mappedBy="user" means the foreign key
    // (user_id) lives on the Task side, not here.
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Task> tasks = new ArrayList<>();

    public User() {
    }

    public User(String username, String password, Role role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public List<Task> getTasks() {
        return tasks;
    }

    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
    }
}
