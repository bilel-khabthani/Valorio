package com.valorio.auth.domain;

import java.time.OffsetDateTime;

public record RefreshToken(
        Long id,
        Long userId,
        String tokenHash,
        boolean revoked,
        OffsetDateTime dateExpiration,
        OffsetDateTime dateCreation
) {
    public boolean estExpire() {
        return OffsetDateTime.now().isAfter(dateExpiration);
    }

    public boolean estValide() {
        return !revoked && !estExpire();
    }
}
