package com.valorio.auth.dto;

import java.util.Map;

public record UserStats(
        long total,
        long actifs,
        long inactifs,
        long verifies,
        Map<String, Long> parActeur,
        Map<String, Long> parRole
) {
}
