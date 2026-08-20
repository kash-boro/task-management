package com.taskmanager.dto;

import com.taskmanager.entity.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// Used to bind the "Add New User" form in the admin Manage Users page.
// We never bind that form directly to the User entity, since the entity's
// password field holds a BCrypt hash, not the plain text the form submits.
public class UserDTO {

    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Password is required")
    @Size(min = 4, message = "Password must be at least 4 characters")
    private String password;

    @NotNull(message = "Role is required")
    private Role role;

    public UserDTO() {
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

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}
