package com.library.application.payload.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.library.application.domain.UserRole;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {

    private String fullName;

    @Email(message = "Email has to be valid")
    private String email;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    private UserRole role;

    private String phone;
    private String username;

    private LocalDateTime lastLogin;
}
