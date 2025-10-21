package com.conectaai.repository;

import com.conectaai.domain.RefreshToken;
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
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    List<RefreshToken> findByUser(User user);

    @Query("SELECT rt FROM RefreshToken rt WHERE rt.user = :user " +
           "AND rt.revokedAt IS NULL " +
           "AND rt.expiresAt > :now")
    List<RefreshToken> findValidTokensByUser(
            @Param("user") User user,
            @Param("now") Instant now
    );

    @Query("SELECT COUNT(rt) > 0 FROM RefreshToken rt WHERE rt.token = :token " +
           "AND rt.revokedAt IS NULL " +
           "AND rt.expiresAt > :now")
    boolean existsByTokenAndValid(
            @Param("token") String token,
            @Param("now") Instant now
    );

    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.revokedAt = :now WHERE rt.token = :token")
    void revokeByToken(
            @Param("token") String token,
            @Param("now") Instant now
    );

    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.revokedAt = :now WHERE rt.user = :user AND rt.revokedAt IS NULL")
    void revokeAllByUser(
            @Param("user") User user,
            @Param("now") Instant now
    );

    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiresAt < :now")
    void deleteExpiredTokens(@Param("now") Instant now);

    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.revokedAt < :cutoffDate")
    void deleteRevokedTokensOlderThan(@Param("cutoffDate") Instant cutoffDate);

    @Query("SELECT COUNT(rt) FROM RefreshToken rt WHERE rt.user = :user " +
           "AND rt.revokedAt IS NULL " +
           "AND rt.expiresAt > :now")
    long countValidTokensByUser(
            @Param("user") User user,
            @Param("now") Instant now
    );

    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.revokedAt = :now WHERE rt.user.id = :userId AND rt.revokedAt IS NULL")
    void revokeAllByUserId(@Param("userId") Long userId, @Param("now") Instant now);
}

