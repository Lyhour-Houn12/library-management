package com.library.application.service.impl;

import com.library.application.config.JwtProvider;
import com.library.application.domain.UserRole;
import com.library.application.entity.User;
import com.library.application.exception.UserException;
import com.library.application.mapper.UserMapper;
import com.library.application.payload.dto.UserDTO;
import com.library.application.repository.UserRepository;
import com.library.application.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final JwtProvider jwtProvider;


    @Override
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserException("User not found"));

    }

    @Override
    public User getUserByJwtToken(String jwt) {
        String email = jwtProvider.getEmailFromJwtToken(jwt);
        return getUserByEmail(email);
    }

    @Override
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserException("User not found"));

    }

    @Override
    public Set<User> getUserByRole(UserRole role) {
        return userRepository.findByRole(role);
    }

    @Override
    public String getCurrentUserEmail() {
        try{
            Authentication  authentication = SecurityContextHolder.getContext().getAuthentication();
            if(authentication != null && authentication.isAuthenticated()) {
                return authentication.getName();
            }
        }catch (Exception e){
            log.warn("Could not get authenticated user", e);
            throw new UserException("Could not get authenticated user");
        }
        return "system";
    }

    @Override
    public List<UserDTO> listUsers() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(userMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserException("User not found"));

    }

    @Override
    public long getTotalUserCount() {
        return userRepository.count();
    }



}
