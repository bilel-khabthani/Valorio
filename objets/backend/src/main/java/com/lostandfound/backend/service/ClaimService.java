package com.lostandfound.backend.service;

import com.lostandfound.backend.dto.ClaimRequest;
import com.lostandfound.backend.dto.ClaimResponse;
import com.lostandfound.backend.entity.Claim;
import com.lostandfound.backend.entity.Item;
import com.lostandfound.backend.enums.ClaimStatus;
import com.lostandfound.backend.enums.ItemStatus;
import com.lostandfound.backend.repository.ClaimRepository;
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
public class ClaimService {

    private final ClaimRepository claimRepository;
    private final ItemRepository itemRepository;

    public List<ClaimResponse> getAll() {
        return claimRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public ClaimResponse getById(Long id) {
        return toResponse(findClaimById(id));
    }

    public List<ClaimResponse> getByItemId(Long itemId) {
        return claimRepository.findByItemId(itemId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public ClaimResponse create(ClaimRequest request) {
        Item item = itemRepository.findById(request.getItemId())
                .orElseThrow(() -> new EntityNotFoundException("Objet introuvable avec l'id: " + request.getItemId()));

        Claim claim = Claim.builder()
                .item(item)
                .claimantName(request.getClaimantName())
                .claimantContact(request.getClaimantContact())
                .description(request.getDescription())
                .status(ClaimStatus.PENDING)
                .build();

        // Mettre l'objet en statut CLAIMED
        item.setStatus(ItemStatus.CLAIMED);
        itemRepository.save(item);

        return toResponse(claimRepository.save(claim));
    }

    public ClaimResponse approve(Long id) {
        Claim claim = findClaimById(id);
        claim.setStatus(ClaimStatus.APPROVED);
        // Marquer l'objet comme RETURNED
        Item item = claim.getItem();
        item.setStatus(ItemStatus.RETURNED);
        itemRepository.save(item);
        return toResponse(claimRepository.save(claim));
    }

    public ClaimResponse reject(Long id) {
        Claim claim = findClaimById(id);
        claim.setStatus(ClaimStatus.REJECTED);
        // Remettre l'objet en ACTIVE si aucune autre réclamation n'est approuvée
        Item item = claim.getItem();
        boolean hasApprovedClaim = claimRepository.findByItemId(item.getId())
                .stream()
                .anyMatch(c -> c.getStatus() == ClaimStatus.APPROVED && !c.getId().equals(id));
        if (!hasApprovedClaim) {
            item.setStatus(ItemStatus.ACTIVE);
            itemRepository.save(item);
        }
        return toResponse(claimRepository.save(claim));
    }

    public void delete(Long id) {
        Claim claim = findClaimById(id);
        claimRepository.delete(claim);
    }

    // --- Mapper ---

    private Claim findClaimById(Long id) {
        return claimRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Réclamation introuvable avec l'id: " + id));
    }

    private ClaimResponse toResponse(Claim claim) {
        return ClaimResponse.builder()
                .id(claim.getId())
                .itemId(claim.getItem().getId())
                .itemTitle(claim.getItem().getTitle())
                .claimantName(claim.getClaimantName())
                .claimantContact(claim.getClaimantContact())
                .description(claim.getDescription())
                .status(claim.getStatus())
                .createdAt(claim.getCreatedAt())
                .updatedAt(claim.getUpdatedAt())
                .build();
    }
}
