package com.valorio.recyclage.domain;

import java.util.List;

public record AnalyseRecyclabilite(
        Long objetId,
        VerdictRecyclabilite recyclable,
        List<String> materiaux,
        List<String> motifs,
        List<Long> centresAppropries
) {
}
