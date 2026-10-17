package com.lostandfound.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ClaimRequest {

    @NotNull(message = "L'ID de l'objet est obligatoire")
    private Long itemId;

    @NotBlank(message = "Le nom du réclamant est obligatoire")
    private String claimantName;

    @NotBlank(message = "Le contact du réclamant est obligatoire")
    private String claimantContact;

    private String description;
}
