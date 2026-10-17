package com.lostandfound.marketplace.config;

import com.lostandfound.marketplace.model.*;
import com.lostandfound.marketplace.repository.MarketplaceItemRepository;
import com.lostandfound.marketplace.repository.MarketplaceOfferRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final MarketplaceItemRepository itemRepository;
    private final MarketplaceOfferRepository offerRepository;

    public DataInitializer(MarketplaceItemRepository itemRepository, MarketplaceOfferRepository offerRepository) {
        this.itemRepository = itemRepository;
        this.offerRepository = offerRepository;
    }

    @Override
    public void run(String... args) {
        if (itemRepository.count() > 0) {
            log.info("Marketplace database already initialized.");
            return;
        }

        log.info("Seeding realistic Lost & Found Marketplace items...");

        // Item 1: iPhone 13
        MarketplaceItem item1 = new MarketplaceItem();
        item1.setTitle("Apple iPhone 13 (128 Go) Noir Minuit - Revalorisé");
        item1.setDescription("### Description de l'objet\nSmartphone trouvé dans la rame du métro Ligne 1, non réclamé au terme du délai de 90 jours légaux. Testé, réinitialisé en mode usine, batterie à 89% d'état de santé. Écran intact, légères micro-rayures d'usage au dos.\n\n- **Inclus :** Câble de charge Lightning certifié\n- **Contrôle IMEI :** Vérifié non blacklisté\n- **Retrait :** Bureau central des objets trouvés");
        item1.setCategory(Category.ELECTRONICS);
        item1.setCondition(ItemCondition.GOOD);
        item1.setType(ItemType.SALE);
        item1.setStatus(ItemStatus.AVAILABLE);
        item1.setPrice(new BigDecimal("290.00"));
        item1.setAiEstimatedPrice(new BigDecimal("295.00"));
        item1.setAiPriceFairness("FAIR_MARKET");
        item1.setAiVerificationScore(98.5);
        item1.setAiModerationNotes("Contenu approuvé. Conforme à la charte de sécurité. IMEI contrôlé.");
        item1.setLocation("Gare Centrale - Guichet Objets Trouvés");
        item1.setOriginalObjectId("OBJ-2024-8841");
        item1.setSellerName("Régie des Transports / Service Revalorisation");
        item1.setSellerEmail("revalorisation.transport@ville.fr");
        item1.setSellerPhone("+33 1 44 00 11 22");
        item1.setImageUrl("https://images.unsplash.com/photo-1592750475338-74b7b21085ab?w=600&auto=format&fit=crop&q=80");
        item1.setTags("smartphone,apple,iphone,high-tech,verifie-ia");
        item1.setUnclaimedDays(120);
        item1.setViewsCount(42);

        // Item 2: Casque Sony WH-1000XM4
        MarketplaceItem item2 = new MarketplaceItem();
        item2.setTitle("Casque Audio Sans Fil Sony WH-1000XM4 Réduction de Bruit");
        item2.setDescription("### Description de l'objet\nCasque à réduction active du bruit trouvé dans la bibliothèque universitaire. Parfait état esthétique et acoustique. Livré avec son étui rigide et le câble jack 3.5mm d'origine.\n\n- **Autonomie :** Testée supérieure à 25h\n- **Nettoyage :** Désinfection complète effectuée");
        item2.setCategory(Category.ELECTRONICS);
        item2.setCondition(ItemCondition.EXCELLENT);
        item2.setType(ItemType.SALE);
        item2.setStatus(ItemStatus.AVAILABLE);
        item2.setPrice(new BigDecimal("135.00"));
        item2.setAiEstimatedPrice(new BigDecimal("145.00"));
        item2.setAiPriceFairness("EXCELLENT_DEAL");
        item2.setAiVerificationScore(99.0);
        item2.setAiModerationNotes("Vérification IA validée. Aucune mention d'objet illicite.");
        item2.setLocation("Campus Universitaire - Accueil Général");
        item2.setOriginalObjectId("OBJ-2024-5219");
        item2.setSellerName("Pôle Logistique Campus");
        item2.setSellerEmail("objets-perdus@univ.fr");
        item2.setImageUrl("https://images.unsplash.com/photo-1546435770-a3e426bf472b?w=600&auto=format&fit=crop&q=80");
        item2.setTags("audio,sony,casque,musique,bluetooth");
        item2.setUnclaimedDays(95);
        item2.setViewsCount(28);

        // Item 3: Don Solidaire Manteau
        MarketplaceItem item3 = new MarketplaceItem();
        item3.setTitle("Manteau d'Hiver Chaud Imperméable (Taille L) - Don Solidaire");
        item3.setDescription("### Don Solidaire Gratuit\nManteau d'hiver noir imperméable oublié dans un amphithéâtre l'hiver dernier. Nettoyé au pressing solidaire partenaire. Mis à disposition gratuite en priorité pour les étudiants ou personnes dans le besoin.\n\n- **Taille :** L (Homme / Unisexe)\n- **Prix :** 0.00 € (Don solidaire gratuit)");
        item3.setCategory(Category.CLOTHING_ACCESSORIES);
        item3.setCondition(ItemCondition.GOOD);
        item3.setType(ItemType.DONATION);
        item3.setStatus(ItemStatus.AVAILABLE);
        item3.setPrice(BigDecimal.ZERO);
        item3.setAiEstimatedPrice(BigDecimal.ZERO);
        item3.setAiPriceFairness("SOLIDARITY_FREE");
        item3.setAiVerificationScore(100.0);
        item3.setAiModerationNotes("Don solidaire approuvé par le filtre éthique.");
        item3.setLocation("Maison de l'Étudiant - Pôle Épicerie Solidaire");
        item3.setOriginalObjectId("OBJ-2024-1044");
        item3.setSellerName("Association Étudiante Solidarité");
        item3.setSellerEmail("contact@solidarite-etudiant.org");
        item3.setImageUrl("https://images.unsplash.com/photo-1539533018447-63fcce667823?w=600&auto=format&fit=crop&q=80");
        item3.setTags("vetement,manteau,don,gratuit,hiver,solidaire");
        item3.setUnclaimedDays(140);
        item3.setViewsCount(67);

        // Item 4: VTT Rockrider
        MarketplaceItem item4 = new MarketplaceItem();
        item4.setTitle("Vélo Tout Terrain VTT Rockrider ST 100 Noir / Rouge");
        item4.setDescription("### Vélo Tout Terrain Reconditionné\nVélo retrouvé attaché à la grille de la gare sans propriétaire identifié après 6 mois de dépôt en fourrière municipale. Freins et transmission révisés, pneus en très bon état.\n\n- **Taille cadre :** M (1m65 - 1m75)\n- **Vitesses :** 21 vitesses Shimano");
        item4.setCategory(Category.BICYCLES_VEHICLES);
        item4.setCondition(ItemCondition.GOOD);
        item4.setType(ItemType.SALE);
        item4.setStatus(ItemStatus.AVAILABLE);
        item4.setPrice(new BigDecimal("85.00"));
        item4.setAiEstimatedPrice(new BigDecimal("90.00"));
        item4.setAiPriceFairness("FAIR_MARKET");
        item4.setAiVerificationScore(97.0);
        item4.setAiModerationNotes("Objet conforme au registre municipal.");
        item4.setLocation("Fourrière Municipale des Vélos");
        item4.setOriginalObjectId("OBJ-2024-3301");
        item4.setSellerName("Service Mobilité Urbaine");
        item4.setSellerEmail("mobilite@mairie-agglo.fr");
        item4.setImageUrl("https://images.unsplash.com/photo-1485965120184-e220f721d03e?w=600&auto=format&fit=crop&q=80");
        item4.setTags("velo,vtt,mobilite,sport,rockrider");
        item4.setUnclaimedDays(180);
        item4.setViewsCount(89);

        // Item 5: Sac Samsonite en cuir
        MarketplaceItem item5 = new MarketplaceItem();
        item5.setTitle("Sacoche Ordinateur & Sac à Dos Samsonite Cuir & Nylon");
        item5.setDescription("### Maroquinerie Professionnelle\nSacoche porte-ordinateur pour PC 15.6 pouces, finition cuir noir et toile balistique renforcée. Intérieur compartimenté et nettoyé, aucune déchirure.");
        item5.setCategory(Category.BAGS_LUGGAGE);
        item5.setCondition(ItemCondition.EXCELLENT);
        item5.setType(ItemType.SALE);
        item5.setStatus(ItemStatus.AVAILABLE);
        item5.setPrice(new BigDecimal("49.00"));
        item5.setAiEstimatedPrice(new BigDecimal("55.00"));
        item5.setAiPriceFairness("EXCELLENT_DEAL");
        item5.setAiVerificationScore(99.0);
        item5.setAiModerationNotes("Conforme.");
        item5.setLocation("Aéroport Terminal 2 - Bureau Objets Trouvés");
        item5.setOriginalObjectId("OBJ-2024-7712");
        item5.setSellerName("Comptoir Bagages Aéroport");
        item5.setSellerEmail("bagages-trouves@aeroport.com");
        item5.setImageUrl("https://images.unsplash.com/photo-1553062407-98eeb64c6a62?w=600&auto=format&fit=crop&q=80");
        item5.setTags("sac,samsonite,cuir,ordinateur,bagage");
        item5.setUnclaimedDays(100);
        item5.setViewsCount(31);

        // Item 6: Reward Bounty (Avis de recherche)
        MarketplaceItem item6 = new MarketplaceItem();
        item6.setTitle("Avis de Recherche : Clé de Voiture BMW + Badge avec Récompense");
        item6.setDescription("### Récompense Garantie pour Restitution\nTrousseau contenant une clé électronique BMW avec porte-clé en cuir marron et badge d'accès magnétique. Égaré entre le parking P3 et l'amphithéâtre.\n\n- **Récompense promise :** 80.00 € pour la personne qui le restitue ou le signale");
        item6.setCategory(Category.DOCUMENTS_KEYS);
        item6.setCondition(ItemCondition.GOOD);
        item6.setType(ItemType.REWARD_BOUNTY);
        item6.setStatus(ItemStatus.AVAILABLE);
        item6.setPrice(new BigDecimal("80.00"));
        item6.setAiEstimatedPrice(new BigDecimal("80.00"));
        item6.setAiPriceFairness("REWARD_OFFER");
        item6.setAiVerificationScore(96.0);
        item6.setAiModerationNotes("Avis de recherche vérifié et validé.");
        item6.setLocation("Parking P3 / Amphi Poincaré");
        item6.setOriginalObjectId("OBJ-2024-9904");
        item6.setSellerName("Thomas Mercier (Propriétaire)");
        item6.setSellerEmail("thomas.mercier@outlook.fr");
        item6.setSellerPhone("+33 6 12 34 56 78");
        item6.setImageUrl("https://images.unsplash.com/photo-1582139329536-e7284fece509?w=600&auto=format&fit=crop&q=80");
        item6.setTags("cle,voiture,recompense,bmw,perdu");
        item6.setUnclaimedDays(5);
        item6.setViewsCount(53);

        List<MarketplaceItem> savedItems = itemRepository.saveAll(List.of(item1, item2, item3, item4, item5, item6));

        // Create sample initial offers for transactions view
        MarketplaceOffer offer1 = new MarketplaceOffer();
        offer1.setItem(savedItems.get(0));
        offer1.setBuyerName("Lucas Moreau");
        offer1.setBuyerEmail("lucas.moreau@gmail.com");
        offer1.setBuyerPhone("+33 6 55 44 33 22");
        offer1.setOfferedPrice(new BigDecimal("270.00"));
        offer1.setMessage("Bonjour, je suis très intéressé par cet iPhone revalorisé. Serait-il possible de le récupérer ce vendredi après-midi au guichet ?");
        offer1.setStatus(OfferStatus.PENDING);

        MarketplaceOffer offer2 = new MarketplaceOffer();
        offer2.setItem(savedItems.get(1));
        offer2.setBuyerName("Sarah Benali");
        offer2.setBuyerEmail("sarah.benali@etudiant.univ.fr");
        offer2.setBuyerPhone("+33 7 88 99 00 11");
        offer2.setOfferedPrice(new BigDecimal("135.00"));
        offer2.setMessage("Offre au prix demandé. Je suis étudiante sur le campus et peux venir le chercher immédiatement.");
        offer2.setStatus(OfferStatus.ACCEPTED);

        MarketplaceOffer offer3 = new MarketplaceOffer();
        offer3.setItem(savedItems.get(2));
        offer3.setBuyerName("Karim Mansour");
        offer3.setBuyerEmail("karim.m@gmail.com");
        offer3.setOfferedPrice(BigDecimal.ZERO);
        offer3.setMessage("Demande d'attribution solidaire pour le manteau. Merci beaucoup pour cette initiative solidaire.");
        offer3.setStatus(OfferStatus.COMPLETED);

        offerRepository.saveAll(List.of(offer1, offer2, offer3));

        log.info("Successfully seeded 6 Marketplace items and 3 transactions/offers.");
    }
}
