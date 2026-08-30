package com.library.application.repository;

import com.library.application.entity.ResetPasswordToken;
import com.library.application.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<ResetPasswordToken, Long> {
    Optional<ResetPasswordToken> findByToken(String token);

    void deleteByUser(User user);
}
