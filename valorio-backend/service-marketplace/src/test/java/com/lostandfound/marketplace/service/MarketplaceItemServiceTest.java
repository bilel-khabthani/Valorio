package com.lostandfound.marketplace.service;

import com.lostandfound.marketplace.model.*;
import com.lostandfound.marketplace.repository.MarketplaceItemRepository;
import com.lostandfound.marketplace.repository.MarketplaceOfferRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests Unitaires de MarketplaceItemService avec Enrobage IA")
class MarketplaceItemServiceTest {

    @Mock
    private MarketplaceItemRepository itemRepository;

    @Mock
    private MarketplaceOfferRepository offerRepository;

    @Spy
    private AiService aiService = new AiService();

    @InjectMocks
    private MarketplaceItemService itemService;

    private MarketplaceItem sampleItem;

    @BeforeEach
    void setUp() {
        sampleItem = new MarketplaceItem();
        sampleItem.setId(1L);
        sampleItem.setTitle("Sac de voyage étanche");
        sampleItem.setDescription("Sac retrouvé en consigne de gare");
        sampleItem.setCategory(Category.BAGS_LUGGAGE);
        sampleItem.setCondition(ItemCondition.GOOD);
        sampleItem.setType(ItemType.SALE);
        sampleItem.setPrice(new BigDecimal("35.00"));
    }

    @Test
    @DisplayName("Création d'un objet avec enrichissement automatique par l'IA")
    void testCreateItemWithAiEnrichment() {
        when(itemRepository.save(any(MarketplaceItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MarketplaceItem created = itemService.createItem(sampleItem);

        assertNotNull(created);
        assertNotNull(created.getAiVerificationScore(), "Le score de vérification IA doit être calculé");
        assertNotNull(created.getAiEstimatedPrice(), "L'estimation IA du prix doit être initialisée");
        assertNotNull(created.getAiPriceFairness(), "Le badge d'équité de prix doit être déterminé");
        assertEquals(ItemStatus.AVAILABLE, created.getStatus());

        verify(itemRepository, times(1)).save(sampleItem);
    }

    @Test
    @DisplayName("Incrémentation des vues lors de la consultation d'un objet")
    void testGetItemByIdIncrementsViews() {
        sampleItem.setViewsCount(10);
        when(itemRepository.findById(1L)).thenReturn(Optional.of(sampleItem));
        when(itemRepository.save(any(MarketplaceItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MarketplaceItem result = itemService.getItemById(1L);

        assertEquals(11, result.getViewsCount());
        verify(itemRepository).save(sampleItem);
    }

    @Test
    @DisplayName("Mise à jour du statut d'un objet")
    void testUpdateStatus() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(sampleItem));
        when(itemRepository.save(any(MarketplaceItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MarketplaceItem updated = itemService.updateStatus(1L, ItemStatus.RESERVED);

        assertEquals(ItemStatus.RESERVED, updated.getStatus());
    }
}
