package com.valorio.auth.domain;

import java.time.OffsetDateTime;

public record PasswordResetToken(
        Long id,
        Long userId,
        String tokenHash,
        boolean used,
        OffsetDateTime dateExpiration,
        OffsetDateTime dateCreation
) {
    public boolean estExpire() {
        return OffsetDateTime.now().isAfter(dateExpiration);
    }

    public boolean estValide() {
        return !used && !estExpire();
    }
}
