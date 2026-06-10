package com.library.application.service.impl;

import com.library.application.config.JwtProvider;
import com.library.application.domain.UserRole;
import com.library.application.entity.User;
import com.library.application.exception.UserException;
import com.library.application.mapper.UserMapper;
import com.library.application.payload.dto.UserDTO;
import com.library.application.payload.response.AuthResponse;
import com.library.application.repository.UserRepository;
import com.library.application.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collection;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final JwtProvider jwtProvider;
    private final CustomUserServiceImpl customUserService;
    @Override
    // @TODO improve phone number
    public AuthResponse signup(UserDTO userDTO) {
        // find user by email
        userRepository.findByEmail(userDTO.getEmail()).ifPresent(u -> {
            throw new UserException("Email already registered");
        });

        // implement user
        User createUser = new User();
        createUser.setEmail(userDTO.getEmail());
        createUser.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        createUser.setFullName(userDTO.getFullName());
        createUser.setPhone(userDTO.getPhone());
        createUser.setRole(UserRole.USER);
        createUser.setUsername(userDTO.getUsername());
        createUser.setLastLogin(LocalDateTime.now());
        createUser.setCreatedAt(LocalDateTime.now());

        // save user to db
        User savedUser = userRepository.save(createUser);
        UserDetails userDetails = customUserService.loadUserByUsername(userDTO.getEmail());
        Authentication auth = new UsernamePasswordAuthenticationToken(savedUser.getEmail(), savedUser.getPassword(), userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
        String jwt = jwtProvider.generateToken(auth);


        AuthResponse authResponse = new AuthResponse();
        authResponse.setTitle("Welcome " + createUser.getFullName());
        authResponse.setMessage("Registered Successfully");
        authResponse.setUserDTO(userMapper.toDTO(savedUser));
        authResponse.setToken(jwt);

        return authResponse;
    }

    @Override
    public AuthResponse login(String username, String password) {
        Authentication authentication = authenticate(username, password);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

        String roles = authorities.iterator().next().getAuthority(); // loop to get role || authorities
        String token = jwtProvider.generateToken(authentication);

        User user = userRepository.findByEmail(username).orElseThrow(() -> new UserException("User not found"));

        user.setLastLogin(LocalDateTime.now());


        AuthResponse authResponse = new AuthResponse();
        authResponse.setTitle("Login Successfully");
        authResponse.setMessage("Welcome Back" + user.getFullName());
        authResponse.setUserDTO(userMapper.toDTO(user));
        authResponse.setToken(token);

        return authResponse;
    }

    private Authentication authenticate(String email, String password){
        UserDetails userDetails = customUserService.loadUserByUsername(email);

        if(userDetails == null){
            throw new UserException("Email does not exist");
        }
        if(!passwordEncoder.matches(password, userDetails.getPassword())){
            throw new UserException("Wrong password");
        }
        return new UsernamePasswordAuthenticationToken(email, password, userDetails.getAuthorities());
    }

    @Override
    public AuthResponse logout() {
        return null;
    }

    @Override
    public void createPasswordResetToken(String email) {

    }

    @Override
    public void resetPassword(String token, String newPassword) {

    }
}
