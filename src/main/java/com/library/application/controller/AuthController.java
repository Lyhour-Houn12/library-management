package com.library.application.controller;

import com.library.application.payload.dto.UserDTO;
import com.library.application.payload.request.ForgotPasswordRequest;
import com.library.application.payload.request.LoginRequest;
import com.library.application.payload.request.ResetPasswordRequest;
import com.library.application.payload.response.ApiResponse;
import com.library.application.payload.response.AuthResponse;
import com.library.application.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signupAccount(@Valid @RequestBody UserDTO userDTO){
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.signup(userDTO));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request){
        return ResponseEntity.ok(authService.login(request.getEmail(), request.getPassword()));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequest request){
        authService.createPasswordResetToken(request.getEmail());
        return ResponseEntity.ok(new ApiResponse("A Reset link was sent to your email.", true) );
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request){
        authService.resetPassword(request.getToken(),  request.getNewPassword());
        return ResponseEntity.ok(new ApiResponse("Password reset successful", true));
    }
}
