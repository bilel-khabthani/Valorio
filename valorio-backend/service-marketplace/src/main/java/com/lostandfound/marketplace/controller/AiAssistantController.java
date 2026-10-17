package com.lostandfound.marketplace.controller;

import com.lostandfound.marketplace.dto.*;
import com.lostandfound.marketplace.model.MarketplaceItem;
import com.lostandfound.marketplace.service.AiService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/marketplace/ai")
@CrossOrigin(originPatterns = "*")
public class AiAssistantController {

    private final AiService aiService;

    public AiAssistantController(AiService aiService) {
        this.aiService = aiService;
    }

    @PostMapping("/generate-listing")
    public ResponseEntity<GenerateListingResponse> generateListing(@Valid @RequestBody GenerateListingRequest request) {
        return ResponseEntity.ok(aiService.generateListing(request));
    }

    @PostMapping("/estimate-price")
    public ResponseEntity<EstimatePriceResponse> estimatePrice(@RequestBody EstimatePriceRequest request) {
        return ResponseEntity.ok(aiService.estimatePrice(request));
    }

    @PostMapping("/moderate")
    public ResponseEntity<ModerationResponse> moderateContent(@RequestBody ModerationRequest request) {
        return ResponseEntity.ok(aiService.moderateContent(request));
    }

    @PostMapping("/detect-anomaly")
    public ResponseEntity<AnomalyDetectionResult> detectAnomaly(@RequestBody MarketplaceItem item) {
        return ResponseEntity.ok(aiService.detectAnomaly(item));
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> getAiHealth() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "engine", "Lost & Found Hybrid AI Intelligence Engine",
                "capabilities", new String[]{"Smart Listing Generation", "Predictive Price Estimation", "Real-Time Moderation", "Outlier Anomaly Detection"},
                "version", "2.4.0",
                "operationalMode", "Hybrid (Local Neural-Heuristic + Cloud LLM Bridge)"
        ));
    }
}
