package com.lostandfound.marketplace.dto;

import com.lostandfound.marketplace.model.Category;
import com.lostandfound.marketplace.model.ItemCondition;
import java.math.BigDecimal;

public class EstimatePriceRequest {

    private String title;
    private Category category;
    private ItemCondition condition;
    private BigDecimal originalRetailPrice;
    private Integer ageInMonths;
    private String brand;

    public EstimatePriceRequest() {}

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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

    public BigDecimal getOriginalRetailPrice() {
        return originalRetailPrice;
    }

    public void setOriginalRetailPrice(BigDecimal originalRetailPrice) {
        this.originalRetailPrice = originalRetailPrice;
    }

    public Integer getAgeInMonths() {
        return ageInMonths;
    }

    public void setAgeInMonths(Integer ageInMonths) {
        this.ageInMonths = ageInMonths;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }
}
