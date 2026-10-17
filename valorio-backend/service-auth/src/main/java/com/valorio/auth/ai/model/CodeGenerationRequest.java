package com.valorio.auth.ai.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CodeGenerationRequest(
        @NotBlank(message = "La tâche de développement est obligatoire")
        @Size(max = 2000, message = "La description de la tâche ne peut dépasser 2000 caractères")
        String task,

        @NotBlank(message = "Le langage cible est obligatoire")
        String language,

        @Size(max = 20000, message = "Le contexte technique ne peut dépasser 20000 caractères")
        String context
) {}
