package com.library.application.service;

import com.library.application.domain.UserRole;
import com.library.application.entity.User;
import com.library.application.payload.dto.UserDTO;

import java.util.List;
import java.util.Set;

public interface UserService {
    User getUserByEmail(String email);
    User getUserByJwtToken(String jwt);
    UserDTO getUserById(Long userId);
    Set<User> getUserByRole(UserRole role);
    String getCurrentUserEmail();
    List<UserDTO> listUsers();


    long getTotalUserCount();
}
