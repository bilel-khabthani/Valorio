package com.lostandfound.marketplace.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "marketplace_items")
public class MarketplaceItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le titre est obligatoire")
    @Column(nullable = false, length = 200)
    private String title;

    @NotBlank(message = "La description est obligatoire")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @NotNull(message = "La catégorie est obligatoire")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private Category category;

    @NotNull(message = "L'état de l'objet est obligatoire")
    @Enumerated(EnumType.STRING)
    @Column(name = "item_condition", nullable = false, length = 50)
    private ItemCondition condition;

    @NotNull(message = "Le type d'annonce est obligatoire")
    @Enumerated(EnumType.STRING)
    @Column(name = "item_type", nullable = false, length = 50)
    private ItemType type;

    @NotNull(message = "Le statut est obligatoire")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ItemStatus status = ItemStatus.AVAILABLE;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price = BigDecimal.ZERO;

    @Column(name = "ai_estimated_price", precision = 10, scale = 2)
    private BigDecimal aiEstimatedPrice;

    @Column(name = "ai_price_fairness", length = 50)
    private String aiPriceFairness = "FAIR";

    @Column(name = "ai_verification_score")
    private Double aiVerificationScore = 95.0;

    @Column(name = "ai_moderation_notes", length = 500)
    private String aiModerationNotes;

    @Column(name = "original_object_id", length = 100)
    private String originalObjectId;

    @Column(length = 200)
    private String location;

    @Column(name = "seller_id", length = 100)
    private String sellerId;

    @Column(name = "seller_name", length = 150)
    private String sellerName;

    @Column(name = "seller_email", length = 150)
    private String sellerEmail;

    @Column(name = "seller_phone", length = 50)
    private String sellerPhone;

    @Column(name = "image_url", length = 1000)
    private String imageUrl;

    @Column(length = 500)
    private String tags;

    @Column(name = "unclaimed_days")
    private Integer unclaimedDays = 90;

    @Column(name = "views_count")
    private Integer viewsCount = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public MarketplaceItem() {
    }

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.viewsCount == null) this.viewsCount = 0;
        if (this.status == null) this.status = ItemStatus.AVAILABLE;
        if (this.price == null) this.price = BigDecimal.ZERO;
        if (this.aiVerificationScore == null) this.aiVerificationScore = 95.0;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public ItemStatus getStatus() {
        return status;
    }

    public void setStatus(ItemStatus status) {
        this.status = status;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getAiEstimatedPrice() {
        return aiEstimatedPrice;
    }

    public void setAiEstimatedPrice(BigDecimal aiEstimatedPrice) {
        this.aiEstimatedPrice = aiEstimatedPrice;
    }

    public String getAiPriceFairness() {
        return aiPriceFairness;
    }

    public void setAiPriceFairness(String aiPriceFairness) {
        this.aiPriceFairness = aiPriceFairness;
    }

    public Double getAiVerificationScore() {
        return aiVerificationScore;
    }

    public void setAiVerificationScore(Double aiVerificationScore) {
        this.aiVerificationScore = aiVerificationScore;
    }

    public String getAiModerationNotes() {
        return aiModerationNotes;
    }

    public void setAiModerationNotes(String aiModerationNotes) {
        this.aiModerationNotes = aiModerationNotes;
    }

    public String getOriginalObjectId() {
        return originalObjectId;
    }

    public void setOriginalObjectId(String originalObjectId) {
        this.originalObjectId = originalObjectId;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getSellerId() {
        return sellerId;
    }

    public void setSellerId(String sellerId) {
        this.sellerId = sellerId;
    }

    public String getSellerName() {
        return sellerName;
    }

    public void setSellerName(String sellerName) {
        this.sellerName = sellerName;
    }

    public String getSellerEmail() {
        return sellerEmail;
    }

    public void setSellerEmail(String sellerEmail) {
        this.sellerEmail = sellerEmail;
    }

    public String getSellerPhone() {
        return sellerPhone;
    }

    public void setSellerPhone(String sellerPhone) {
        this.sellerPhone = sellerPhone;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public Integer getUnclaimedDays() {
        return unclaimedDays;
    }

    public void setUnclaimedDays(Integer unclaimedDays) {
        this.unclaimedDays = unclaimedDays;
    }

    public Integer getViewsCount() {
        return viewsCount;
    }

    public void setViewsCount(Integer viewsCount) {
        this.viewsCount = viewsCount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
