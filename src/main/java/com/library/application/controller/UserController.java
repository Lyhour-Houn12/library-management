package com.library.application.controller;

import com.library.application.payload.response.UserStatResponse;
import com.library.application.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/api/users/profile")
    public ResponseEntity<?> getUserProfile(@RequestHeader("Authorization") String jwt) {
        return ResponseEntity.ok(userService.getUserByJwtToken(jwt));
    }

    @GetMapping("/users/list")
    public ResponseEntity<?> getUsers(){
        return ResponseEntity.ok(userService.listUsers());
    }


    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserById(@PathVariable("userId") Long userId) {
        return ResponseEntity.ok(userService.getUserById(userId));
    }

    @GetMapping("/users/statistics")
    @PreAuthorize("hasRole(ADMIN)")
    public ResponseEntity<?> getUserStatistics() {
        long totalUser = userService.getTotalUserCount();
        return ResponseEntity.ok(new UserStatResponse(totalUser));
    }



}
