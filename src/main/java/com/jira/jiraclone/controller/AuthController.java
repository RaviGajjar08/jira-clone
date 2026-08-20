package com.jira.jiraclone.controller;

import com.jira.jiraclone.dto.*;
import com.jira.jiraclone.response.ApiResponse;
import com.jira.jiraclone.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ApiResponse<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request
    ) {

        return ApiResponse.<AuthResponse>builder()
                .success(true)
                .message("User registered successfully")
                .data(authService.register(request))
                .build();
    }

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {

        return ApiResponse.<AuthResponse>builder()
                .success(true)
                .message("Login successful")
                .data(authService.login(request))
                .build();
    }

    @PutMapping("/change-password")
    public ApiResponse<?> changePassword(
            @Valid
            @RequestBody
            ChangePasswordRequest request,
            Authentication authentication
    ) {

        authService.changePassword(
                request,
                authentication.getName()
        );

        return ApiResponse.builder()
                .success(true)
                .message("Password changed successfully")
                .data(null)
                .build();
    }

    @RestController
    @RequestMapping("/api/admin")
    public class AdminController {

        @GetMapping("/dashboard")
        public String dashboard() {
            return "Admin Dashboard";
        }
    }

    @PostMapping("/forgot-password")
    public ApiResponse<?> forgotPassword(
            @Valid
            @RequestBody
            ForgotPasswordRequest request
    ) {

        authService.forgotPassword(request);

        return ApiResponse.builder()
                .success(true)
                .message(
                        "If an account exists, a reset email has been sent."
                )
                .build();
    }

    @PostMapping("/reset-password")
    public ApiResponse<?> resetPassword(
            @Valid
            @RequestBody
            ResetPasswordRequest request
    ) {

        authService.resetPassword(request);

        return ApiResponse.builder()
                .success(true)
                .message("Password reset successful")
                .build();
    }
}
