package com.lostandfound.backend.controller;

import com.lostandfound.backend.dto.ClaimRequest;
import com.lostandfound.backend.dto.ClaimResponse;
import com.lostandfound.backend.service.ClaimService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/claims")
@RequiredArgsConstructor
public class ClaimController {

    private final ClaimService claimService;

    @GetMapping
    public ResponseEntity<List<ClaimResponse>> getAll() {
        return ResponseEntity.ok(claimService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClaimResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(claimService.getById(id));
    }

    @GetMapping("/item/{itemId}")
    public ResponseEntity<List<ClaimResponse>> getByItemId(@PathVariable Long itemId) {
        return ResponseEntity.ok(claimService.getByItemId(itemId));
    }

    @PostMapping
    public ResponseEntity<ClaimResponse> create(@Valid @RequestBody ClaimRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(claimService.create(request));
    }

    @PatchMapping("/{id}/approve")
    public ResponseEntity<ClaimResponse> approve(@PathVariable Long id) {
        return ResponseEntity.ok(claimService.approve(id));
    }

    @PatchMapping("/{id}/reject")
    public ResponseEntity<ClaimResponse> reject(@PathVariable Long id) {
        return ResponseEntity.ok(claimService.reject(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        claimService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
