package com.lostandfound.marketplace.controller;

import com.lostandfound.marketplace.model.MarketplaceOffer;
import com.lostandfound.marketplace.model.OfferStatus;
import com.lostandfound.marketplace.service.MarketplaceOfferService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/marketplace/offers")
@CrossOrigin(originPatterns = "*")
public class MarketplaceOfferController {

    private final MarketplaceOfferService offerService;

    public MarketplaceOfferController(MarketplaceOfferService offerService) {
        this.offerService = offerService;
    }

    @GetMapping
    public ResponseEntity<List<MarketplaceOffer>> getAllOffers() {
        return ResponseEntity.ok(offerService.getAllOffers());
    }

    @GetMapping("/item/{itemId}")
    public ResponseEntity<List<MarketplaceOffer>> getOffersByItem(@PathVariable Long itemId) {
        return ResponseEntity.ok(offerService.getOffersByItem(itemId));
    }

    @PostMapping("/item/{itemId}")
    public ResponseEntity<MarketplaceOffer> createOffer(
            @PathVariable Long itemId,
            @Valid @RequestBody MarketplaceOffer offer) {
        MarketplaceOffer created = offerService.createOffer(itemId, offer);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PatchMapping("/{offerId}/status")
    public ResponseEntity<MarketplaceOffer> updateOfferStatus(
            @PathVariable Long offerId,
            @RequestParam OfferStatus status) {
        return ResponseEntity.ok(offerService.updateOfferStatus(offerId, status));
    }

    @DeleteMapping("/{offerId}")
    public ResponseEntity<Void> deleteOffer(@PathVariable Long offerId) {
        offerService.deleteOffer(offerId);
        return ResponseEntity.noContent().build();
    }
}
