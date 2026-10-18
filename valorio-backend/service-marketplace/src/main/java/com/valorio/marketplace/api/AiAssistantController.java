package com.valorio.marketplace.api;

import com.valorio.marketplace.domain.MarketplaceItem;
import com.valorio.marketplace.dto.AnomalyDetectionResult;
import com.valorio.marketplace.dto.EstimatePriceRequest;
import com.valorio.marketplace.dto.EstimatePriceResponse;
import com.valorio.marketplace.dto.GenerateListingRequest;
import com.valorio.marketplace.dto.GenerateListingResponse;
import com.valorio.marketplace.dto.ModerationRequest;
import com.valorio.marketplace.dto.ModerationResponse;
import com.valorio.marketplace.service.AiService;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/marketplace/ai")
public class AiAssistantController {

    private final AiService ia;

    public AiAssistantController(AiService ia) {
        this.ia = ia;
    }

    @PostMapping("/generate-listing")
    public GenerateListingResponse genererAnnonce(@Valid @RequestBody GenerateListingRequest request) {
        return ia.generateListing(request);
    }

    @PostMapping("/estimate-price")
    public EstimatePriceResponse estimerPrix(@RequestBody EstimatePriceRequest request) {
        return ia.estimatePrice(request);
    }

    @PostMapping("/moderate")
    public ModerationResponse moderer(@RequestBody ModerationRequest request) {
        return ia.moderateContent(request);
    }

    @PostMapping("/detect-anomaly")
    public AnomalyDetectionResult detecterAnomalie(@RequestBody MarketplaceItem item) {
        return ia.detectAnomaly(item);
    }

    @GetMapping("/health")
    public Map<String, Object> sante() {
        return Map.of(
                "status", "UP",
                "engine", "Valorio Marketplace Hybrid AI Intelligence Engine",
                "capabilities", new String[]{
                        "Smart Listing Generation",
                        "Predictive Price Estimation",
                        "Real-Time Moderation",
                        "Outlier Anomaly Detection"},
                "version", "2.4.0",
                "operationalMode", "Hybrid (Local Heuristic Engine + Cloud LLM Bridge)");
    }
}
