package com.taskmanager.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService userDetailsService;

    // Encodes passwords with BCrypt before storing/comparing them.
    // We never store plain-text passwords in the database.
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Wires our CustomUserDetailsService + PasswordEncoder into Spring
    // Security's authentication flow.
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // CSS/JS files and the login page itself must be reachable by anyone, logged in or not.
                .requestMatchers("/login", "/register", "/css/**", "/js/**").permitAll()
                // Only ADMIN accounts can view the user-management page.
                .requestMatchers("/users/**").hasRole("ADMIN")
                // Everything else requires a logged-in user of any role.
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")                 // our custom login page
                .defaultSuccessUrl("/dashboard", true) // where to go after a successful login
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")    // back to login page with a message
                .permitAll()
            )
            .authenticationProvider(authenticationProvider());

        return http.build();
    }
}
