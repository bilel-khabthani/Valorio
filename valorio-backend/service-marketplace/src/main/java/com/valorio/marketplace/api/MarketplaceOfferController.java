package com.valorio.marketplace.api;

import com.valorio.marketplace.domain.MarketplaceOffer;
import com.valorio.marketplace.domain.OfferStatus;
import com.valorio.marketplace.dto.OfferRequest;
import com.valorio.marketplace.service.MarketplaceOfferService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/marketplace/offers")
public class MarketplaceOfferController {

    private final MarketplaceOfferService service;

    public MarketplaceOfferController(MarketplaceOfferService service) {
        this.service = service;
    }

    @GetMapping
    public List<MarketplaceOffer> toutes() {
        return service.toutesLesOffres();
    }

    @GetMapping("/item/{itemId}")
    public List<MarketplaceOffer> parObjet(@PathVariable long itemId) {
        return service.offresParObjet(itemId);
    }

    @PostMapping("/item/{itemId}")
    @ResponseStatus(HttpStatus.CREATED)
    public MarketplaceOffer creer(@PathVariable long itemId, @Valid @RequestBody OfferRequest request) {
        return service.creer(itemId, request);
    }

    @PatchMapping("/{offerId}/status")
    public MarketplaceOffer changerStatut(@PathVariable long offerId, @RequestParam OfferStatus status) {
        return service.changerStatut(offerId, status);
    }

    @DeleteMapping("/{offerId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(@PathVariable long offerId) {
        service.supprimer(offerId);
    }
}
