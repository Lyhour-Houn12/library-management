package com.library.application.service.impl;

import com.library.application.domain.UserRole;
import com.library.application.entity.User;
import com.library.application.exception.UserException;
import com.library.application.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
@Component
@RequiredArgsConstructor
public class DataInitializationComponent implements CommandLineRunner {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    @Override
    public void run(String... args) throws Exception {
        initializeUsers();
    }

    private void initializeUsers() {

        String adminEmail = "lyhourhoun.406@gmail.com";

        if (userRepository.findByEmail(adminEmail).isPresent()) {
            return;
        }

        User admin = new User();
        admin.setUsername("admin");
        admin.setEmail(adminEmail);
        admin.setFullName("Houn Lyhour");
        admin.setPassword(passwordEncoder.encode("lyhour1234"));
        admin.setRole(UserRole.ROLE_ADMIN);

        userRepository.save(admin);
    }
}
