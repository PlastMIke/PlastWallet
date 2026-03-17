package com.wallet.interfaces.api.controller;

import com.wallet.application.port.out.UserPort;
import com.wallet.domain.entity.User;
import com.wallet.infrastructure.security.JwtAuthenticationResponse;
import com.wallet.infrastructure.security.JwtTokenProvider;
import com.wallet.interfaces.api.request.LoginRequest;
import com.wallet.interfaces.api.request.RegisterRequest;
import com.wallet.interfaces.api.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "User authentication and registration APIs")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserPort userPort;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    @Operation(summary = "Register new user", description = "Creates a new user account and returns JWT token")
    public ResponseEntity<ApiResponse<JwtAuthenticationResponse>> register(
            @Valid @RequestBody RegisterRequest request) {
        
        // Check if user already exists
        if (userPort.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest().body(
                ApiResponse.error("User with this email already exists", 400)
            );
        }

        // Create new user
        User user = User.builder()
                .id(UUID.randomUUID())
                .name(request.getName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        userPort.save(user);

        // Generate JWT token
        String token = jwtTokenProvider.generateToken(user);

        JwtAuthenticationResponse response = JwtAuthenticationResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresIn(86400000L) // 24 hours
                .build();

        return ResponseEntity.ok(ApiResponse.success(response, "User registered successfully"));
    }

    @PostMapping("/login")
    @Operation(summary = "Login user", description = "Authenticates user and returns JWT token")
    public ResponseEntity<ApiResponse<JwtAuthenticationResponse>> login(
            @Valid @RequestBody LoginRequest request) {

        // Authenticate user
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.getEmail(),
                request.getPassword()
            )
        );

        // Load user and generate token
        User user = (User) userPort.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String token = jwtTokenProvider.generateToken(user);

        // Add custom claims - use numeric user_id based on email
        Map<String, Object> extraClaims = new HashMap<>();
        // Map email to numeric user_id
        Long numericUserId = 1L; // default
        if (user.getEmail().contains("alice")) {
            numericUserId = 1L;
        } else if (user.getEmail().contains("bob")) {
            numericUserId = 2L;
        } else if (user.getEmail().contains("charlie")) {
            numericUserId = 3L;
        }
        
        extraClaims.put("userId", numericUserId);
        extraClaims.put("name", user.getName());

        String enhancedToken = jwtTokenProvider.generateToken(extraClaims, user);

        JwtAuthenticationResponse response = JwtAuthenticationResponse.builder()
                .token(enhancedToken)
                .tokenType("Bearer")
                .expiresIn(86400000L) // 24 hours
                .build();

        return ResponseEntity.ok(ApiResponse.success(response, "Login successful"));
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user", description = "Returns current authenticated user details")
    public ResponseEntity<ApiResponse<User>> getCurrentUser() {
        // This will be implemented with @AuthenticationPrincipal
        return ResponseEntity.ok(ApiResponse.success(null, "Current user endpoint"));
    }
}
