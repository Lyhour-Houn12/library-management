package com.library.application.payload.request;

import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class LoginRequest {

    @Email(message = "Email has to be valid")
    private String email;
    private String password;
}
