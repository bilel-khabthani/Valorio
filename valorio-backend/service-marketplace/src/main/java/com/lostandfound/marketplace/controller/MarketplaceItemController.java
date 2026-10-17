package com.lostandfound.marketplace.controller;

import com.lostandfound.marketplace.dto.MarketplaceStatsResponse;
import com.lostandfound.marketplace.model.Category;
import com.lostandfound.marketplace.model.ItemStatus;
import com.lostandfound.marketplace.model.ItemType;
import com.lostandfound.marketplace.model.MarketplaceItem;
import com.lostandfound.marketplace.service.MarketplaceItemService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/marketplace/items")
@CrossOrigin(originPatterns = "*")
public class MarketplaceItemController {

    private final MarketplaceItemService itemService;

    public MarketplaceItemController(MarketplaceItemService itemService) {
        this.itemService = itemService;
    }

    @GetMapping
    public ResponseEntity<List<MarketplaceItem>> getItems(
            @RequestParam(required = false) Category category,
            @RequestParam(required = false) ItemType type,
            @RequestParam(required = false) ItemStatus status,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(itemService.searchItems(category, type, status, minPrice, maxPrice, search));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MarketplaceItem> getItemById(@PathVariable Long id) {
        return ResponseEntity.ok(itemService.getItemById(id));
    }

    @PostMapping
    public ResponseEntity<MarketplaceItem> createItem(@Valid @RequestBody MarketplaceItem item) {
        MarketplaceItem created = itemService.createItem(item);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MarketplaceItem> updateItem(@PathVariable Long id, @Valid @RequestBody MarketplaceItem item) {
        return ResponseEntity.ok(itemService.updateItem(id, item));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<MarketplaceItem> updateStatus(@PathVariable Long id, @RequestParam ItemStatus status) {
        return ResponseEntity.ok(itemService.updateStatus(id, status));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItem(@PathVariable Long id) {
        itemService.deleteItem(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/stats")
    public ResponseEntity<MarketplaceStatsResponse> getStatistics() {
        return ResponseEntity.ok(itemService.getStatistics());
    }
}
