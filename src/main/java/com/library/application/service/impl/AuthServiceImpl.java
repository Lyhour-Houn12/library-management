package com.library.application.service.impl;

import com.library.application.config.JwtProvider;
import com.library.application.domain.UserRole;
import com.library.application.entity.ResetPasswordToken;
import com.library.application.entity.User;
import com.library.application.exception.BadCredentialException;
import com.library.application.exception.UserException;
import com.library.application.mapper.UserMapper;
import com.library.application.payload.dto.UserDTO;
import com.library.application.payload.response.AuthResponse;
import com.library.application.repository.PasswordResetTokenRepository;
import com.library.application.repository.UserRepository;
import com.library.application.service.AuthService;
import com.library.application.service.EmailService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final JwtProvider jwtProvider;
    private final CustomUserServiceImpl customUserService;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailService emailService;

    @Value("${app.frontend.reset-url}")
    private String frontendUrl;

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
        createUser.setRole(UserRole.ROLE_USER);
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
    @Transactional
    public void createPasswordResetToken(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new UserException("User not found"));

        String token = UUID.randomUUID().toString();
        ResetPasswordToken resetPasswordToken = ResetPasswordToken.builder()
                .expiryDate(LocalDateTime.now().plusMinutes(5))
                .user(user)
                .token(token)
                .build();

        passwordResetTokenRepository.save(resetPasswordToken);

        String resetLink = frontendUrl + token;
        String subject = "Password Reset Token";
        String body = "Your requested to request your password. Use this link (valid 5 minutes): " + resetLink;

        emailService.sendEmail(user.getEmail(), subject, body);

    }

    @Override
    @Transactional
    public void resetPassword(String token, String newPassword) {
        Optional<ResetPasswordToken> resetPasswordToken = passwordResetTokenRepository.findByToken(token);
        if(resetPasswordToken.isEmpty()){
            throw new BadCredentialsException("Invalid or Expired Token");
        }

        ResetPasswordToken resetToken = resetPasswordToken.get();

        if(resetToken.isExpired()){
            // token expired - delete it
            passwordResetTokenRepository.delete(resetToken);
            throw new BadCredentialsException("Invalid or Expired Token");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // delete token after successful reset
        passwordResetTokenRepository.delete(resetToken);



    }
}
