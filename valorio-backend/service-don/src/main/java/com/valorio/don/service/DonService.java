package com.valorio.don.service;

import com.valorio.don.audit.AuditLog;
import com.valorio.don.domain.AnnonceDon;
import com.valorio.don.domain.DemandeDon;
import com.valorio.don.domain.StatutAnnonceDon;
import com.valorio.don.domain.StatutDemandeDon;
import com.valorio.don.domain.TransitionInterditeException;
import com.valorio.don.external.ObjetPort;
import com.valorio.don.external.UtilisateurPort;
import com.valorio.don.external.model.ObjetRef;
import com.valorio.don.repo.DonRepository;
import com.valorio.don.util.Geo;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DonService {

    private final DonRepository repository;
    private final ObjetPort objets;
    private final UtilisateurPort utilisateurs;
    private final AuditLog audit;

    public DonService(DonRepository repository, ObjetPort objets, UtilisateurPort utilisateurs, AuditLog audit) {
        this.repository = repository;
        this.objets = objets;
        this.utilisateurs = utilisateurs;
        this.audit = audit;
    }

    public record Correspondance(AnnonceDon annonce, double score, double distanceKm) {
    }

    @Transactional
    public AnnonceDon publier(long objetId, long donneurId, String titre, String description, String conditions,
                              String ville, Double latitude, Double longitude) {
        objets.findById(objetId).orElseThrow(() -> new NoSuchElementException("Objet " + objetId + " introuvable"));
        utilisateurs.findById(donneurId)
                .orElseThrow(() -> new NoSuchElementException("Utilisateur " + donneurId + " introuvable"));

        long id = repository.creerAnnonce(objetId, donneurId, titre, description, conditions, ville,
                latitude, longitude);
        audit.enregistrer("ANNONCE_DON_PUBLIEE", "annonce_don", id, "objet=" + objetId);
        return repository.findAnnonce(id).orElseThrow();
    }

    @Transactional(readOnly = true)
    public List<AnnonceDon> lister(Long categorieId, String ville, StatutAnnonceDon statut) {
        List<AnnonceDon> annonces = repository.findAnnonces(ville, statut);
        if (categorieId == null) {
            return annonces;
        }
        return annonces.stream()
                .filter(annonce -> appartientALaCategorie(annonce, categorieId))
                .toList();
    }

    @Transactional(readOnly = true)
    public AnnonceDon detail(long annonceId) {
        return repository.findAnnonce(annonceId)
                .orElseThrow(() -> new NoSuchElementException("Annonce " + annonceId + " introuvable"));
    }

    @Transactional
    public AnnonceDon modifier(long annonceId, String titre, String description, String conditions, String ville,
                               Double latitude, Double longitude) {
        AnnonceDon annonce = detail(annonceId);
        if (annonce.statut() == StatutAnnonceDon.ANNULEE || annonce.statut() == StatutAnnonceDon.REMISE) {
            throw new TransitionInterditeException("Une annonce " + annonce.statut() + " ne peut plus être modifiée");
        }
        repository.majAnnonce(annonceId, titre, description, conditions, ville, latitude, longitude);
        audit.enregistrer("ANNONCE_DON_MODIFIEE", "annonce_don", annonceId, null);
        return detail(annonceId);
    }

    @Transactional
    public void supprimer(long annonceId, Long donneurId) {
        AnnonceDon annonce = detail(annonceId);
        if (donneurId != null && !donneurId.equals(annonce.donneurId())) {
            throw new TransitionInterditeException("Seul le donneur peut supprimer son annonce");
        }
        repository.supprimerAnnonce(annonceId);
        audit.enregistrer("ANNONCE_DON_SUPPRIMEE", "annonce_don", annonceId, null);
    }

    @Transactional
    public AnnonceDon annuler(long annonceId, Long donneurId) {
        AnnonceDon annonce = detail(annonceId);
        if (donneurId != null && !donneurId.equals(annonce.donneurId())) {
            throw new TransitionInterditeException("Seul le donneur peut annuler son annonce");
        }
        if (!annonce.statut().peutTransitionnerVers(StatutAnnonceDon.ANNULEE)) {
            throw new TransitionInterditeException(
                    "Une annonce " + annonce.statut() + " ne peut pas être annulée");
        }
        repository.majStatutAnnonce(annonceId, StatutAnnonceDon.ANNULEE);
        audit.enregistrer("ANNONCE_DON_ANNULEE", "annonce_don", annonceId, "donneur=" + donneurId);
        return detail(annonceId);
    }

    @Transactional(readOnly = true)
    public List<Correspondance> matching(Long categorieId, Double latitude, Double longitude, double rayonKm) {
        List<AnnonceDon> disponibles = repository.findAnnonces(null, StatutAnnonceDon.PUBLIEE).stream()
                .filter(annonce -> categorieId == null || appartientALaCategorie(annonce, categorieId))
                .toList();

        if (latitude == null || longitude == null) {
            return disponibles.stream()
                    .map(annonce -> new Correspondance(annonce, 1.0, 0.0))
                    .toList();
        }

        return disponibles.stream()
                .filter(annonce -> annonce.latitude() != null && annonce.longitude() != null)
                .map(annonce -> {
                    double distance = Geo.distanceKm(latitude, longitude, annonce.latitude(), annonce.longitude());
                    double score = Math.max(0.0, 1.0 - distance / rayonKm);
                    return new Correspondance(annonce, arrondi(score), arrondi(distance));
                })
                .filter(correspondance -> correspondance.distanceKm() <= rayonKm)
                .sorted(Comparator.comparingDouble(Correspondance::score).reversed())
                .toList();
    }

    @Transactional
    public DemandeDon demander(long annonceId, long beneficiaireId, String message) {
        AnnonceDon annonce = detail(annonceId);
        if (annonce.statut() != StatutAnnonceDon.PUBLIEE) {
            throw new TransitionInterditeException("L'annonce n'est pas disponible : " + annonce.statut());
        }
        utilisateurs.findById(beneficiaireId)
                .orElseThrow(() -> new NoSuchElementException("Utilisateur " + beneficiaireId + " introuvable"));

        long id = repository.creerDemande(annonceId, beneficiaireId, message);
        repository.ajouterHistorique(id, StatutDemandeDon.DEMANDE, "Demande de don");
        audit.enregistrer("DEMANDE_DON_CREEE", "demande_don", id, "annonce=" + annonceId);
        return chargerDemande(id);
    }

    @Transactional(readOnly = true)
    public List<DemandeDon> demandesDeAnnonce(long annonceId) {
        detail(annonceId);
        return repository.findDemandesParAnnonce(annonceId);
    }

    @Transactional
    public DemandeDon changerStatutDemande(long demandeId, StatutDemandeDon cible, String commentaire) {
        DemandeDon demande = chargerDemande(demandeId);
        if (!demande.statut().peutTransitionnerVers(cible)) {
            throw new TransitionInterditeException("Transition interdite : " + demande.statut() + " -> " + cible);
        }

        repository.majStatutDemande(demandeId, cible);
        repository.ajouterHistorique(demandeId, cible, commentaire);

        if (cible == StatutDemandeDon.ACCEPTEE) {
            repository.majStatutAnnonce(demande.annonceId(), StatutAnnonceDon.RESERVEE);
        } else if (cible == StatutDemandeDon.REFUSEE) {
            repository.majStatutAnnonce(demande.annonceId(), StatutAnnonceDon.PUBLIEE);
        } else if (cible == StatutDemandeDon.CONFIRMEE) {
            repository.majStatutAnnonce(demande.annonceId(), StatutAnnonceDon.REMISE);
        }

        audit.enregistrer("DEMANDE_DON_STATUT_MODIFIE", "demande_don", demandeId,
                demande.statut() + " -> " + cible);
        return chargerDemande(demandeId);
    }

    @Transactional(readOnly = true)
    public DemandeDon chargerDemande(long demandeId) {
        DemandeDon demande = repository.findDemande(demandeId)
                .orElseThrow(() -> new NoSuchElementException("Demande " + demandeId + " introuvable"));
        return new DemandeDon(demande.id(), demande.annonceId(), demande.beneficiaireId(), demande.message(),
                demande.statut(), demande.dateDemande(), demande.dateConfirmation(),
                repository.findHistorique(demandeId));
    }

    private boolean appartientALaCategorie(AnnonceDon annonce, Long categorieId) {
        return objets.findById(annonce.objetId())
                .map(ObjetRef::categorieId)
                .filter(categorieId::equals)
                .isPresent();
    }

    private static double arrondi(double valeur) {
        return Math.round(valeur * 100.0) / 100.0;
    }
}
