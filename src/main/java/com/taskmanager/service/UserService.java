package com.taskmanager.service;

import com.taskmanager.dto.RegisterRequest;
import com.taskmanager.dto.UserDTO;
import com.taskmanager.entity.Role;
import com.taskmanager.entity.User;
import com.taskmanager.exception.DuplicateResourceException;
import com.taskmanager.exception.ResourceNotFoundException;
import com.taskmanager.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Used by controllers to find out WHICH user is currently logged in,
    // based on the username Spring Security stores in the security context.
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    // Used by the admin-only "manage users" page.
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // Public self-registration (used by /register, open to anyone -- unlike
    // createUser() below, which is the admin-only Add User flow and lets
    // the admin choose a role). Self-registered accounts are always USER.
    public User registerUser(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("An account with this email already exists.");
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        // Login is done via email address -- we reuse the "username" field
        // Spring Security already looks up, just populated with the email.
        user.setUsername(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.USER);
        return userRepository.save(user);
    }

    // Creates a new user account. Only reachable via /users/add, which
    // SecurityConfig restricts to ADMIN accounts.
    public User createUser(UserDTO dto) {
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new DuplicateResourceException("Username already taken: " + dto.getUsername());
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        // Password is hashed before saving -- we never store plain text passwords.
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole(dto.getRole());
        return userRepository.save(user);
    }

    // Deletes a user account (and, via cascade = ALL on User.tasks, all of
    // their tasks too). Blocks two unsafe cases:
    //   1. An admin deleting their OWN account (would lock them out mid-session)
    //   2. Deleting the last remaining ADMIN account (would lock everyone out)
    public void deleteUser(Long id, User currentUser) {
        User target = getUserById(id);

        if (target.getId().equals(currentUser.getId())) {
            throw new IllegalStateException("You cannot delete your own account while logged in.");
        }

        if (target.getRole() == Role.ADMIN && userRepository.countByRole(Role.ADMIN) <= 1) {
            throw new IllegalStateException("Cannot delete the last remaining admin account.");
        }

        userRepository.delete(target);
    }

    // Lets a logged-in user delete their own account from the Profile page.
    // Still blocked if they're the last remaining admin -- otherwise the
    // system would be left with no admin at all.
    public void deleteOwnAccount(User currentUser) {
        if (currentUser.getRole() == Role.ADMIN && userRepository.countByRole(Role.ADMIN) <= 1) {
            throw new IllegalStateException("Cannot delete your account: you are the last remaining admin.");
        }
        userRepository.delete(currentUser);
    }

    // Returns false if currentPassword doesn't match what's stored, so the
    // controller can show "Current password is incorrect" instead of failing silently.
    public boolean changePassword(User user, String currentPassword, String newPassword) {
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            return false;
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        return true;
    }
}
