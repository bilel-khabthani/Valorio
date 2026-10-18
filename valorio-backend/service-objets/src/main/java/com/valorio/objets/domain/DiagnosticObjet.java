package com.valorio.objets.domain;

import java.time.OffsetDateTime;
import java.util.List;

public record DiagnosticObjet(
        Long id,
        Long objetId,
        String panneDeclaree,
        List<String> problemes,
        String analyse,
        String diagnostic,
        OffsetDateTime date
) {
}
