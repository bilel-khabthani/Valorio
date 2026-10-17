package com.valorio.auth.repo;

import com.valorio.auth.domain.Role;
import com.valorio.auth.domain.User;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepository {

    private final JdbcClient jdbcClient;

    private static final RowMapper<User> USER_MAPPER = (rs, i) -> new User(
            rs.getLong("id"),
            rs.getString("email"),
            rs.getString("password_hash"),
            rs.getString("nom"),
            rs.getString("prenom"),
            Role.valueOf(rs.getString("role")),
            rs.getBoolean("actif"),
            rs.getBoolean("email_verifie"),
            rs.getObject("date_creation", OffsetDateTime.class),
            rs.getObject("date_maj", OffsetDateTime.class)
    );

    public UserRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public Optional<User> findById(Long id) {
        return jdbcClient.sql("""
                SELECT id, email, password_hash, nom, prenom, role, actif, email_verifie, date_creation, date_maj
                FROM auth.users
                WHERE id = :id
                """)
                .param("id", id)
                .query(USER_MAPPER)
                .optional();
    }

    public Optional<User> findByEmail(String email) {
        if (email == null) {
            return Optional.empty();
        }
        return jdbcClient.sql("""
                SELECT id, email, password_hash, nom, prenom, role, actif, email_verifie, date_creation, date_maj
                FROM auth.users
                WHERE LOWER(email) = LOWER(:email)
                """)
                .param("email", email.trim())
                .query(USER_MAPPER)
                .optional();
    }

    public boolean existsByEmail(String email) {
        if (email == null) {
            return false;
        }
        Integer count = jdbcClient.sql("""
                SELECT COUNT(*) FROM auth.users WHERE LOWER(email) = LOWER(:email)
                """)
                .param("email", email.trim())
                .query(Integer.class)
                .single();
        return count != null && count > 0;
    }

    public User save(User user) {
        if (user.id() == null) {
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcClient.sql("""
                    INSERT INTO auth.users (email, password_hash, nom, prenom, role, actif, email_verifie, date_creation, date_maj)
                    VALUES (:email, :passwordHash, :nom, :prenom, :role, :actif, :emailVerifie, now(), now())
                    """)
                    .param("email", user.email().trim().toLowerCase())
                    .param("passwordHash", user.passwordHash())
                    .param("nom", user.nom().trim())
                    .param("prenom", user.prenom().trim())
                    .param("role", user.role().name())
                    .param("actif", user.actif())
                    .param("emailVerifie", user.emailVerifie())
                    .update(keyHolder, "id");
            Number key = keyHolder.getKey();
            Long id = key != null ? key.longValue() : null;
            return findById(id).orElseThrow();
        } else {
            jdbcClient.sql("""
                    UPDATE auth.users
                    SET nom = :nom, prenom = :prenom, actif = :actif, email_verifie = :emailVerifie, date_maj = now()
                    WHERE id = :id
                    """)
                    .param("id", user.id())
                    .param("nom", user.nom().trim())
                    .param("prenom", user.prenom().trim())
                    .param("actif", user.actif())
                    .param("emailVerifie", user.emailVerifie())
                    .update();
            return findById(user.id()).orElseThrow();
        }
    }

    public void updatePassword(Long userId, String passwordHash) {
        jdbcClient.sql("""
                UPDATE auth.users
                SET password_hash = :passwordHash, date_maj = now()
                WHERE id = :id
                """)
                .param("id", userId)
                .param("passwordHash", passwordHash)
                .update();
    }

    public void verifyEmail(Long userId) {
        jdbcClient.sql("""
                UPDATE auth.users
                SET email_verifie = TRUE, date_maj = now()
                WHERE id = :id
                """)
                .param("id", userId)
                .update();
    }

    public List<User> findAll() {
        return jdbcClient.sql("""
                SELECT id, email, password_hash, nom, prenom, role, actif, email_verifie, date_creation, date_maj
                FROM auth.users
                ORDER BY id ASC
                """)
                .query(USER_MAPPER)
                .list();
    }
}
