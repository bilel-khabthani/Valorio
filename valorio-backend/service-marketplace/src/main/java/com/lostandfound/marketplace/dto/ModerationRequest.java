package com.lostandfound.marketplace.dto;

import com.lostandfound.marketplace.model.Category;

public class ModerationRequest {

    private String title;
    private String description;
    private Category category;
    private String price;

    public ModerationRequest() {}

    public ModerationRequest(String title, String description, Category category, String price) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.price = price;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }
}
