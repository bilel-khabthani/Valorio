package com.lostandfound.backend.service;

import com.lostandfound.backend.dto.ItemRequest;
import com.lostandfound.backend.dto.ItemResponse;
import com.lostandfound.backend.entity.Item;
import com.lostandfound.backend.enums.ItemStatus;
import com.lostandfound.backend.enums.ItemType;
import com.lostandfound.backend.repository.ItemRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ItemService {

    private final ItemRepository itemRepository;

    // --- CRUD ---

    public List<ItemResponse> getAll() {
        return itemRepository.findAllSorted().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public ItemResponse getById(Long id) {
        return toResponse(findItemById(id));
    }

    public ItemResponse create(ItemRequest request) {
        Item item = Item.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .category(request.getCategory())
                .location(request.getLocation())
                .date(request.getDate())
                .type(request.getType())
                .status(request.getStatus() != null ? request.getStatus() : ItemStatus.ACTIVE)
                .reporterName(request.getReporterName())
                .reporterContact(request.getReporterContact())
                .photoUrl(request.getPhotoUrl())
                .build();
        return toResponse(itemRepository.save(item));
    }

    public ItemResponse update(Long id, ItemRequest request) {
        Item item = findItemById(id);
        item.setTitle(request.getTitle());
        item.setDescription(request.getDescription());
        item.setCategory(request.getCategory());
        item.setLocation(request.getLocation());
        item.setDate(request.getDate());
        item.setType(request.getType());
        if (request.getStatus() != null) {
            item.setStatus(request.getStatus());
        }
        item.setReporterName(request.getReporterName());
        item.setReporterContact(request.getReporterContact());
        if (request.getPhotoUrl() != null && !request.getPhotoUrl().isBlank()) {
            item.setPhotoUrl(request.getPhotoUrl());
        }
        return toResponse(itemRepository.save(item));
    }

    public void delete(Long id) {
        Item item = findItemById(id);
        itemRepository.delete(item);
    }

    // --- Recherche (filtrage en Java pour éviter les problèmes de types Enum en JPQL) ---

    public List<ItemResponse> search(String keyword, String category, String location,
                                     ItemType type, ItemStatus status) {
        // Charger tous les items puis filtrer en mémoire
        List<Item> all = itemRepository.findAllSorted();

        return all.stream()
                .filter(i -> keyword == null || keyword.isBlank()
                        || i.getTitle().toLowerCase().contains(keyword.toLowerCase())
                        || (i.getDescription() != null && i.getDescription().toLowerCase().contains(keyword.toLowerCase())))
                .filter(i -> category == null || category.isBlank()
                        || i.getCategory().equalsIgnoreCase(category))
                .filter(i -> location == null || location.isBlank()
                        || i.getLocation().toLowerCase().contains(location.toLowerCase()))
                .filter(i -> type == null || i.getType() == type)
                .filter(i -> status == null || i.getStatus() == status)
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // --- Statut ---

    public ItemResponse confirmReturn(Long id) {
        Item item = findItemById(id);
        item.setStatus(ItemStatus.RETURNED);
        return toResponse(itemRepository.save(item));
    }

    public ItemResponse updateStatus(Long id, ItemStatus status) {
        Item item = findItemById(id);
        item.setStatus(status);
        return toResponse(itemRepository.save(item));
    }

    // --- Métadonnées ---

    public List<String> getCategories() {
        return itemRepository.findAllCategories();
    }

    public List<String> getLocations() {
        return itemRepository.findAllLocations();
    }

    // --- Helpers ---

    private Item findItemById(Long id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Objet introuvable avec l'id: " + id));
    }

    public ItemResponse toResponse(Item item) {
        return ItemResponse.builder()
                .id(item.getId())
                .title(item.getTitle())
                .description(item.getDescription())
                .category(item.getCategory())
                .location(item.getLocation())
                .date(item.getDate())
                .photoUrl(item.getPhotoUrl())
                .type(item.getType())
                .status(item.getStatus())
                .reporterName(item.getReporterName())
                .reporterContact(item.getReporterContact())
                .createdAt(item.getCreatedAt())
                .updatedAt(item.getUpdatedAt())
                .claimsCount(item.getClaims() != null ? item.getClaims().size() : 0)
                .build();
    }
}
