package com.library.application.service;

import com.library.application.payload.dto.UserDTO;
import com.library.application.payload.response.AuthResponse;

public interface AuthService {

    AuthResponse  login(String username, String password);

    AuthResponse signup(UserDTO userDTO);

    AuthResponse logout();

    void createPasswordResetToken(String email);

    void resetPassword(String token, String newPassword);


}
