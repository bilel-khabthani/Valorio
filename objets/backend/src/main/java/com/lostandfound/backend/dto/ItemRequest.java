package com.lostandfound.backend.dto;

import com.lostandfound.backend.enums.ItemStatus;
import com.lostandfound.backend.enums.ItemType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ItemRequest {

    @NotBlank(message = "Le titre est obligatoire")
    private String title;

    private String description;

    @NotBlank(message = "La catégorie est obligatoire")
    private String category;

    @NotBlank(message = "La localisation est obligatoire")
    private String location;

    @NotNull(message = "La date est obligatoire")
    private LocalDate date;

    @NotNull(message = "Le type est obligatoire")
    private ItemType type;

    private ItemStatus status;

    private String reporterName;

    private String reporterContact;

    private String photoUrl;
}
