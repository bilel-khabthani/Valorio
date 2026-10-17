package com.valorio.auth.repo;

import com.valorio.auth.domain.EmailVerificationToken;
import com.valorio.auth.domain.PasswordResetToken;
import com.valorio.auth.domain.RefreshToken;
import java.time.OffsetDateTime;
import java.util.Optional;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class TokenRepository {

    private final JdbcClient jdbcClient;

    private static final RowMapper<RefreshToken> REFRESH_MAPPER = (rs, i) -> new RefreshToken(
            rs.getLong("id"),
            rs.getLong("user_id"),
            rs.getString("token_hash"),
            rs.getBoolean("revoked"),
            rs.getObject("date_expiration", OffsetDateTime.class),
            rs.getObject("date_creation", OffsetDateTime.class)
    );

    private static final RowMapper<PasswordResetToken> RESET_MAPPER = (rs, i) -> new PasswordResetToken(
            rs.getLong("id"),
            rs.getLong("user_id"),
            rs.getString("token_hash"),
            rs.getBoolean("used"),
            rs.getObject("date_expiration", OffsetDateTime.class),
            rs.getObject("date_creation", OffsetDateTime.class)
    );

    private static final RowMapper<EmailVerificationToken> VERIF_MAPPER = (rs, i) -> new EmailVerificationToken(
            rs.getLong("id"),
            rs.getLong("user_id"),
            rs.getString("token_hash"),
            rs.getBoolean("used"),
            rs.getObject("date_expiration", OffsetDateTime.class),
            rs.getObject("date_creation", OffsetDateTime.class)
    );

    public TokenRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    // Refresh Token operations
    public void saveRefreshToken(Long userId, String tokenHash, OffsetDateTime expiration) {
        jdbcClient.sql("""
                INSERT INTO auth.refresh_token (user_id, token_hash, revoked, date_expiration, date_creation)
                VALUES (:userId, :tokenHash, FALSE, :expiration, now())
                """)
                .param("userId", userId)
                .param("tokenHash", tokenHash)
                .param("expiration", expiration)
                .update();
    }

    public Optional<RefreshToken> findRefreshToken(String tokenHash) {
        return jdbcClient.sql("""
                SELECT id, user_id, token_hash, revoked, date_expiration, date_creation
                FROM auth.refresh_token
                WHERE token_hash = :tokenHash
                """)
                .param("tokenHash", tokenHash)
                .query(REFRESH_MAPPER)
                .optional();
    }

    public void revokeRefreshToken(String tokenHash) {
        jdbcClient.sql("""
                UPDATE auth.refresh_token
                SET revoked = TRUE
                WHERE token_hash = :tokenHash
                """)
                .param("tokenHash", tokenHash)
                .update();
    }

    public void revokeAllUserRefreshTokens(Long userId) {
        jdbcClient.sql("""
                UPDATE auth.refresh_token
                SET revoked = TRUE
                WHERE user_id = :userId
                """)
                .param("userId", userId)
                .update();
    }

    // Password Reset operations
    public void savePasswordResetToken(Long userId, String tokenHash, OffsetDateTime expiration) {
        jdbcClient.sql("""
                INSERT INTO auth.password_reset_token (user_id, token_hash, used, date_expiration, date_creation)
                VALUES (:userId, :tokenHash, FALSE, :expiration, now())
                """)
                .param("userId", userId)
                .param("tokenHash", tokenHash)
                .param("expiration", expiration)
                .update();
    }

    public Optional<PasswordResetToken> findPasswordResetToken(String tokenHash) {
        return jdbcClient.sql("""
                SELECT id, user_id, token_hash, used, date_expiration, date_creation
                FROM auth.password_reset_token
                WHERE token_hash = :tokenHash
                """)
                .param("tokenHash", tokenHash)
                .query(RESET_MAPPER)
                .optional();
    }

    public void markPasswordResetTokenUsed(String tokenHash) {
        jdbcClient.sql("""
                UPDATE auth.password_reset_token
                SET used = TRUE
                WHERE token_hash = :tokenHash
                """)
                .param("tokenHash", tokenHash)
                .update();
    }

    // Email Verification operations
    public void saveEmailVerificationToken(Long userId, String tokenHash, OffsetDateTime expiration) {
        jdbcClient.sql("""
                INSERT INTO auth.email_verification_token (user_id, token_hash, used, date_expiration, date_creation)
                VALUES (:userId, :tokenHash, FALSE, :expiration, now())
                """)
                .param("userId", userId)
                .param("tokenHash", tokenHash)
                .param("expiration", expiration)
                .update();
    }

    public Optional<EmailVerificationToken> findEmailVerificationToken(String tokenHash) {
        return jdbcClient.sql("""
                SELECT id, user_id, token_hash, used, date_expiration, date_creation
                FROM auth.email_verification_token
                WHERE token_hash = :tokenHash
                """)
                .param("tokenHash", tokenHash)
                .query(VERIF_MAPPER)
                .optional();
    }

    public void markEmailVerificationTokenUsed(String tokenHash) {
        jdbcClient.sql("""
                UPDATE auth.email_verification_token
                SET used = TRUE
                WHERE token_hash = :tokenHash
                """)
                .param("tokenHash", tokenHash)
                .update();
    }
}
