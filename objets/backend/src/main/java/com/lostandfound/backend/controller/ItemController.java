package com.lostandfound.backend.controller;

import com.lostandfound.backend.dto.ItemRequest;
import com.lostandfound.backend.dto.ItemResponse;
import com.lostandfound.backend.enums.ItemStatus;
import com.lostandfound.backend.enums.ItemType;
import com.lostandfound.backend.service.FileStorageService;
import com.lostandfound.backend.service.ItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;
    private final FileStorageService fileStorageService;

    @GetMapping
    public ResponseEntity<List<ItemResponse>> getAll() {
        return ResponseEntity.ok(itemService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ItemResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(itemService.getById(id));
    }

    @GetMapping("/search")
    public ResponseEntity<List<ItemResponse>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) ItemType type,
            @RequestParam(required = false) ItemStatus status) {
        return ResponseEntity.ok(itemService.search(keyword, category, location, type, status));
    }

    @GetMapping("/categories")
    public ResponseEntity<List<String>> getCategories() {
        return ResponseEntity.ok(itemService.getCategories());
    }

    @GetMapping("/locations")
    public ResponseEntity<List<String>> getLocations() {
        return ResponseEntity.ok(itemService.getLocations());
    }

    // Création sans photo
    @PostMapping
    public ResponseEntity<ItemResponse> create(@Valid @RequestBody ItemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(itemService.create(request));
    }

    // Création avec photo (multipart)
    @PostMapping(value = "/with-photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ItemResponse> createWithPhoto(
            @RequestPart("item") @Valid ItemRequest request,
            @RequestPart(value = "photo", required = false) MultipartFile photo) {
        if (photo != null && !photo.isEmpty()) {
            String photoUrl = fileStorageService.store(photo);
            request.setPhotoUrl(photoUrl);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(itemService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ItemResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody ItemRequest request) {
        return ResponseEntity.ok(itemService.update(id, request));
    }

    // Mise à jour avec photo
    @PutMapping(value = "/{id}/with-photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ItemResponse> updateWithPhoto(
            @PathVariable Long id,
            @RequestPart("item") @Valid ItemRequest request,
            @RequestPart(value = "photo", required = false) MultipartFile photo) {
        if (photo != null && !photo.isEmpty()) {
            String photoUrl = fileStorageService.store(photo);
            request.setPhotoUrl(photoUrl);
        }
        return ResponseEntity.ok(itemService.update(id, request));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ItemResponse> updateStatus(
            @PathVariable Long id,
            @RequestParam ItemStatus status) {
        return ResponseEntity.ok(itemService.updateStatus(id, status));
    }

    @PatchMapping("/{id}/return")
    public ResponseEntity<ItemResponse> confirmReturn(@PathVariable Long id) {
        return ResponseEntity.ok(itemService.confirmReturn(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        itemService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
