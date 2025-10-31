package com.example.user.repository;

import com.example.user.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    
    Optional<PasswordResetToken> findByToken(String token);
    
    @Query("SELECT t FROM PasswordResetToken t WHERE t.user.id = :userId AND t.used = false AND t.expiryDate > :now")
    List<PasswordResetToken> findValidTokensByUserId(@Param("userId") Long userId, @Param("now") LocalDateTime now);
    
    @Query("SELECT t FROM PasswordResetToken t WHERE t.used = false AND t.expiryDate < :now")
    List<PasswordResetToken> findExpiredTokens(@Param("now") LocalDateTime now);
}
