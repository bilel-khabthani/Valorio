package com.valorio.objets.dto;

import java.util.List;

public record DiagnosticRequest(
        String panneDeclaree,
        List<String> problemes
) {
}
