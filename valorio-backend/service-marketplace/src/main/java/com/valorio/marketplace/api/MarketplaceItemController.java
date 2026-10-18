package com.valorio.marketplace.api;

import com.valorio.marketplace.domain.Category;
import com.valorio.marketplace.domain.ItemStatus;
import com.valorio.marketplace.domain.ItemType;
import com.valorio.marketplace.domain.MarketplaceItem;
import com.valorio.marketplace.dto.ItemRequest;
import com.valorio.marketplace.dto.MarketplaceStats;
import com.valorio.marketplace.service.MarketplaceItemService;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/marketplace/items")
public class MarketplaceItemController {

    private final MarketplaceItemService service;

    public MarketplaceItemController(MarketplaceItemService service) {
        this.service = service;
    }

    @GetMapping
    public List<MarketplaceItem> lister(@RequestParam(required = false) Category category,
                                        @RequestParam(required = false) ItemType type,
                                        @RequestParam(required = false) ItemStatus status,
                                        @RequestParam(required = false) BigDecimal minPrice,
                                        @RequestParam(required = false) BigDecimal maxPrice,
                                        @RequestParam(required = false) String search) {
        return service.rechercher(category, type, status, minPrice, maxPrice, search);
    }

    @GetMapping("/stats")
    public MarketplaceStats stats() {
        return service.statistiques();
    }

    @GetMapping("/{itemId}")
    public MarketplaceItem consulter(@PathVariable long itemId) {
        return service.consulter(itemId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MarketplaceItem creer(@Valid @RequestBody ItemRequest request) {
        return service.creer(request);
    }

    @PutMapping("/{itemId}")
    public MarketplaceItem modifier(@PathVariable long itemId, @Valid @RequestBody ItemRequest request) {
        return service.modifier(itemId, request);
    }

    @PatchMapping("/{itemId}/status")
    public MarketplaceItem changerStatut(@PathVariable long itemId, @RequestParam ItemStatus status) {
        return service.changerStatut(itemId, status);
    }

    @DeleteMapping("/{itemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(@PathVariable long itemId) {
        service.supprimer(itemId);
    }
}
