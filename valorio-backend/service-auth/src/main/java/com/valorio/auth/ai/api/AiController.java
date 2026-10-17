package com.valorio.auth.ai.api;

import com.valorio.auth.ai.model.CodeGenerationRequest;
import com.valorio.auth.ai.model.CodeGenerationResponse;
import com.valorio.auth.ai.model.CodeReviewRequest;
import com.valorio.auth.ai.model.CodeReviewResponse;
import com.valorio.auth.ai.model.QualityReportRequest;
import com.valorio.auth.ai.model.QualityReportResponse;
import com.valorio.auth.ai.model.TestGenerationRequest;
import com.valorio.auth.ai.model.TestGenerationResponse;
import com.valorio.auth.ai.service.AiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
@Tag(name = "Intelligence Artificielle & QA", description = "Endpoints d'assistance au code, génération de tests et assurance qualité")
public class AiController {

    private final AiService aiService;

    public AiController(AiService aiService) {
        this.aiService = aiService;
    }

    @PostMapping("/code-generation")
    @Operation(summary = "Génération de code assistée par IA avec explications et risques")
    public ResponseEntity<CodeGenerationResponse> generateCode(@Valid @RequestBody CodeGenerationRequest request) {
        CodeGenerationResponse response = aiService.generateCode(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/tests/generate")
    @Operation(summary = "Génération de suites de tests unitaires et détection des cas limites")
    public ResponseEntity<TestGenerationResponse> generateTests(@Valid @RequestBody TestGenerationRequest request) {
        TestGenerationResponse response = aiService.generateTests(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/code-review")
    @Operation(summary = "Revue critique automatisée du code avec score et recommandations")
    public ResponseEntity<CodeReviewResponse> reviewCode(@Valid @RequestBody CodeReviewRequest request) {
        CodeReviewResponse response = aiService.reviewCode(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/quality-report")
    @Operation(summary = "Génération d'un rapport complet d'assurance qualité corrélant analyse statique et IA")
    public ResponseEntity<QualityReportResponse> generateQualityReport(@Valid @RequestBody QualityReportRequest request) {
        QualityReportResponse response = aiService.generateQualityReport(request);
        return ResponseEntity.ok(response);
    }
}
