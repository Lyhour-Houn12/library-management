package com.library.application.payload.response;


import com.library.application.payload.dto.UserDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String token;

    private String message;

    private String title;

    private UserDTO userDTO;
}
