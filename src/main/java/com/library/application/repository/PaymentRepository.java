package com.library.application.repository;

import com.library.application.entity.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment,Long> {
    Optional<Payment> findByGatewayOrderId(String gatewayOrderId);

    Optional<Payment> findByTransactionId(String transactionId);

    Page<Payment> findByUserIdAndActiveTrue(Long userId, Pageable pageable);
}
