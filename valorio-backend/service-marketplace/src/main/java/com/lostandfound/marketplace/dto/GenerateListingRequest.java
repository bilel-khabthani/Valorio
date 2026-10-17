package com.lostandfound.marketplace.dto;

import com.lostandfound.marketplace.model.Category;
import com.lostandfound.marketplace.model.ItemCondition;
import com.lostandfound.marketplace.model.ItemType;
import jakarta.validation.constraints.NotBlank;

public class GenerateListingRequest {

    @NotBlank(message = "Les mots-clés ou description brute sont obligatoires")
    private String prompt;

    private Category category;
    private ItemCondition condition;
    private ItemType type;
    private String brand;
    private String location;

    public GenerateListingRequest() {}

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public ItemCondition getCondition() {
        return condition;
    }

    public void setCondition(ItemCondition condition) {
        this.condition = condition;
    }

    public ItemType getType() {
        return type;
    }

    public void setType(ItemType type) {
        this.type = type;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
