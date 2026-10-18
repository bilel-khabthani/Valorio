package com.valorio.recommandation.dto;

import com.valorio.recommandation.domain.RouteValorisation;

public record AlternativeReco(
        RouteValorisation route,
        Double probabilite
) {
}
