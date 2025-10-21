package com.conectaai.repository;

import com.conectaai.domain.PasswordResetToken;
import com.conectaai.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByToken(String token);

    List<PasswordResetToken> findByUser(User user);

    @Query("SELECT prt FROM PasswordResetToken prt WHERE prt.token = :token " +
           "AND prt.usedAt IS NULL " +
           "AND prt.expiresAt > :now")
    Optional<PasswordResetToken> findValidToken(
            @Param("token") String token,
            @Param("now") Instant now
    );

    @Query("SELECT COUNT(prt) > 0 FROM PasswordResetToken prt WHERE prt.user = :user " +
           "AND prt.usedAt IS NULL " +
           "AND prt.expiresAt > :now")
    boolean existsValidTokenForUser(
            @Param("user") User user,
            @Param("now") Instant now
    );

    @Modifying
    @Query("UPDATE PasswordResetToken prt SET prt.usedAt = CURRENT_TIMESTAMP " +
           "WHERE prt.user.id = :userId AND prt.usedAt IS NULL")
    void revokeAllByUserId(@Param("userId") Long userId);

    @Modifying
    @Query("UPDATE PasswordResetToken prt SET prt.usedAt = :now " +
           "WHERE prt.user = :user AND prt.usedAt IS NULL")
    void invalidateAllPendingTokensByUser(
            @Param("user") User user,
            @Param("now") Instant now
    );

    @Modifying
    @Query("DELETE FROM PasswordResetToken prt WHERE prt.expiresAt < :now")
    void deleteExpiredTokens(@Param("now") Instant now);

    @Modifying
    @Query("DELETE FROM PasswordResetToken prt WHERE prt.usedAt < :cutoffDate")
    void deleteUsedTokensOlderThan(@Param("cutoffDate") Instant cutoffDate);

    @Query("SELECT prt FROM PasswordResetToken prt WHERE prt.user = :user " +
           "ORDER BY prt.createdAt DESC LIMIT 1")
    Optional<PasswordResetToken> findLatestByUser(@Param("user") User user);
}
