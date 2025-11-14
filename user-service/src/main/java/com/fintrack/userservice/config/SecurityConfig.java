/*
 * Security Configuration - Place to configure all Spring Security settings
 * It configures:
 * 1. Which endpoints are public vs protected
 * 2. How authentication works (JWT-based)
 * 3. Password encryption
 * 4. Filter chain (JWT filter)
 * 5. Session management (stateless)
 */
package com.fintrack.userservice.config;

import com.fintrack.userservice.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter, UserDetailsService userDetailsService) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // Main security configuration (routes, filters, sessions)
        http
                .csrf(AbstractHttpConfigurer::disable) // Cross-Site Request Forgery protection
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/users/register", "/api/users/login", "/api/users/health").permitAll()
                        .anyRequest().authenticated()) // Only allow the above 3 enpoints to be access in public, all
                                                       // other endpoints are protected
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // Server does NOT create or use HTTP
                                                                                 // sessions nor store any user state
                .authenticationProvider(authenticationProvider()) // Tells Spring Security HOW to authenticate users
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class); // Add JWT Filter since we
                                                                                             // want to validate JWT
                                                                                             // tokens BEFORE Spring
                                                                                             // tries other
                                                                                             // authentication methods

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        /*
         * Initiate Authentication Provider
         * 1. Loads user via UserDetailsService
         * Validates password using PasswordEncoder (BCrypt)
         */
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        /*
         * The main entry point for authyentication in Spring Security
         * Coordinates authentication attempts
         */
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
