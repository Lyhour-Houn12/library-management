package com.library.application.repository;

import com.library.application.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    @Query("""
            SELECT s FROM Subscription s WHERE s.user.id =:userId
            AND s.isActive = true
            AND s.startDate <= :today AND s.endDate >= :today
            ORDER BY s.endDate DESC
    """)
    Optional<Subscription> findActiveSubscriptionByUserId(@Param("userId") Long userId, @Param("today") LocalDate today);

    List<Subscription> findByUserIdOrderByCreatedAtDesc(Long userId);

    @Query("""
        SELECT CASE WHEN COUNT(s) > 0 THEN TRUE ELSE FALSE END FROM Subscription s
        WHERE s.user.id = :userId AND s.startDate <= :today AND s.endDate >= :today
    """)
    boolean hasActiveSubscriptionByUserId(@Param("userId") Long userId, @Param("today") LocalDate today);

    @Query("""
        SELECT s FROM Subscription s
        WHERE s.isActive = true AND s.endDate <= :today
        """)
    List<Subscription> findExpiredActiveSubscriptions(@Param("today") LocalDate today);
}
