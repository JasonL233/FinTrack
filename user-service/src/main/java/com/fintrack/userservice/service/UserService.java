package com.fintrack.userservice.service;

import com.fintrack.common.dto.ApiResponse;
import com.fintrack.common.exception.ResourceNotFoundException;
import com.fintrack.userservice.dto.AuthResponse;
import com.fintrack.userservice.dto.LoginRequest;
import com.fintrack.userservice.dto.RegisterRequest;
import com.fintrack.userservice.dto.UserResponse;
import com.fintrack.userservice.entity.User;
import com.fintrack.userservice.entity.UserRole;
import com.fintrack.userservice.repository.UserRepository;
import com.fintrack.userservice.security.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {
    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil, AuthenticationManager authenticationManager, UserDetailsService userDetailsService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
    }

    @Transactional  // Ensure database operations are atomic (all or nothing)
    public ApiResponse<AuthResponse> register(RegisterRequest request) {
        // Register new users

        log.info("Registering new user with email: {}", request.getEmail());

        if (userRepository.existsByEmail((request.getEmail()))) {
            return ApiResponse.error("Email already registered.");
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phoneNumber(request.getPhoneNumber())
                .role(UserRole.USER)
                .active(true)
                .build();
        
        user = userRepository.save(user);   // Save to database
        log.info("User registered successfully with ID: {}", user.getId());

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String token = jwtUtil.generateToken(userDetails, user.getId());

        AuthResponse authResponse = AuthResponse.builder()
                .token(token)
                .userId(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .build();

        return ApiResponse.success("User registered successfully", authResponse);
    }

    public ApiResponse<AuthResponse> login(LoginRequest request) {
        log.info("Login attempt for email: {}", request.getEmail());

        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );  // AuthenticationManager calls AuthenticationProvider to load user from database and validate password (check if user is authenticate)

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found!"));
        
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String token = jwtUtil.generateToken(userDetails, user.getId());    // Generate a new JWT token upon login for fresh session

        AuthResponse authResponse = AuthResponse.builder()
                .token(token)
                .userId(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .build();
        
        log.info("User logged in successfully with ID: {}", user.getId());
        return ApiResponse.success("Login successful", authResponse);
    }

    public ApiResponse<UserResponse> getUserProfile(String email) {
        log.info("Fetching profile for email: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found!"));
        
        UserResponse userResponse = UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole().name())
                .active(user.getActive())
                .createdAt(user.getCreatedAt())
                .build();

        return ApiResponse.success(userResponse);
    }
}
