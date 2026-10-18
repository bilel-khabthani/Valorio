package com.valorio.reparation.service;

import com.valorio.reparation.audit.AuditLog;
import com.valorio.reparation.domain.Devis;
import com.valorio.reparation.domain.DemandeReparation;
import com.valorio.reparation.domain.Reparateur;
import com.valorio.reparation.domain.StatutDemande;
import com.valorio.reparation.domain.StatutDevis;
import com.valorio.reparation.domain.TransitionInterditeException;
import com.valorio.reparation.external.ObjetEvenementPort;
import com.valorio.reparation.external.ObjetPort;
import com.valorio.reparation.external.UtilisateurPort;
import com.valorio.reparation.repo.ReparationRepository;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReparationService {

    private final ReparationRepository repository;
    private final ObjetPort objets;
    private final UtilisateurPort utilisateurs;
    private final ObjetEvenementPort evenements;
    private final AuditLog audit;

    public ReparationService(ReparationRepository repository,
                             ObjetPort objets,
                             UtilisateurPort utilisateurs,
                             ObjetEvenementPort evenements,
                             AuditLog audit) {
        this.repository = repository;
        this.objets = objets;
        this.utilisateurs = utilisateurs;
        this.evenements = evenements;
        this.audit = audit;
    }

    @Transactional
    public DemandeReparation creerDemande(long objetId, long utilisateurId, String descriptionPanne) {
        objets.findById(objetId).orElseThrow(() -> new NoSuchElementException("Objet " + objetId + " introuvable"));
        utilisateurs.findById(utilisateurId)
                .orElseThrow(() -> new NoSuchElementException("Utilisateur " + utilisateurId + " introuvable"));

        long id = repository.creerDemande(objetId, utilisateurId, descriptionPanne);
        repository.ajouterHistorique(id, StatutDemande.DEMANDE, "Création de la demande");
        evenements.enregistrer(objetId, "REPARATION", "Demande de réparation déposée (demande " + id + ")",
                utilisateurId, "EN_REPARATION");
        audit.enregistrer("DEMANDE_REPARATION_CREEE", "demande_reparation", id, "objet=" + objetId);
        return charger(id);
    }

    @Transactional
    public DemandeReparation changerStatut(long demandeId, StatutDemande cible, String commentaire) {
        DemandeReparation demande = repository.findDemande(demandeId)
                .orElseThrow(() -> new NoSuchElementException("Demande " + demandeId + " introuvable"));

        if (!demande.statut().peutTransitionnerVers(cible)) {
            throw new TransitionInterditeException(
                    "Transition interdite : " + demande.statut() + " -> " + cible);
        }
        if (cible == StatutDemande.ACCEPTEE && demande.reparateurId() == null) {
            throw new TransitionInterditeException("Aucun réparateur assigné à cette demande");
        }

        repository.majStatut(demandeId, cible);
        repository.ajouterHistorique(demandeId, cible, commentaire);
        if (cible == StatutDemande.TERMINEE) {
            evenements.enregistrer(demande.objetId(), "REPARATION",
                    "Réparation terminée (demande " + demandeId + ")",
                    demande.reparateurId(), "REPARE");
        }
        audit.enregistrer("DEMANDE_STATUT_MODIFIE", "demande_reparation", demandeId,
                demande.statut() + " -> " + cible);
        return charger(demandeId);
    }

    @Transactional(readOnly = true)
    public DemandeReparation charger(long demandeId) {
        DemandeReparation demande = repository.findDemande(demandeId)
                .orElseThrow(() -> new NoSuchElementException("Demande " + demandeId + " introuvable"));
        return new DemandeReparation(
                demande.id(),
                demande.objetId(),
                demande.utilisateurId(),
                demande.reparateurId(),
                demande.descriptionPanne(),
                demande.statut(),
                demande.dateIntervention(),
                demande.dateCreation(),
                demande.dateMaj(),
                repository.findHistorique(demandeId));
    }

    @Transactional(readOnly = true)
    public List<DemandeReparation> lister(StatutDemande statut, Long utilisateurId, Long reparateurId) {
        return repository.findDemandes(statut, utilisateurId, reparateurId);
    }

    @Transactional
    public DemandeReparation planifierIntervention(long demandeId, OffsetDateTime dateIntervention) {
        DemandeReparation demande = repository.findDemande(demandeId)
                .orElseThrow(() -> new NoSuchElementException("Demande " + demandeId + " introuvable"));
        if (dateIntervention == null) {
            throw new IllegalArgumentException("La date d'intervention est obligatoire");
        }
        if (!demande.statut().autorisePlanificationIntervention()) {
            throw new TransitionInterditeException(
                    "Impossible de planifier une intervention sur une demande " + demande.statut());
        }

        repository.majDateIntervention(demandeId, dateIntervention);
        repository.ajouterHistorique(demandeId, demande.statut(),
                "Intervention planifiée le " + dateIntervention);
        audit.enregistrer("INTERVENTION_PLANIFIEE", "demande_reparation", demandeId,
                "date=" + dateIntervention);
        return charger(demandeId);
    }

    @Transactional
    public Devis creerDevis(long demandeId, BigDecimal montant, Integer delaiJours, String commentaire,
                            Long reparateurId) {
        DemandeReparation demande = repository.findDemande(demandeId)
                .orElseThrow(() -> new NoSuchElementException("Demande " + demandeId + " introuvable"));
        if (demande.statut() != StatutDemande.DEMANDE) {
            throw new TransitionInterditeException("Un devis ne peut être proposé que sur une demande en attente");
        }
        if (reparateurId != null) {
            repository.findReparateur(reparateurId)
                    .orElseThrow(() -> new NoSuchElementException("Réparateur " + reparateurId + " introuvable"));
            repository.assignerReparateur(demandeId, reparateurId);
        }

        long id = repository.creerDevis(demandeId, montant, delaiJours, commentaire);
        audit.enregistrer("DEVIS_CREE", "devis", id, "demande=" + demandeId + " montant=" + montant);
        return repository.findDevis(id).orElseThrow();
    }

    @Transactional(readOnly = true)
    public Devis devisDeDemande(long demandeId) {
        return repository.findDevisParDemande(demandeId)
                .orElseThrow(() -> new NoSuchElementException("Aucun devis pour la demande " + demandeId));
    }

    @Transactional
    public Devis repondreDevis(long devisId, boolean accepte) {
        Devis devis = repository.findDevis(devisId)
                .orElseThrow(() -> new NoSuchElementException("Devis " + devisId + " introuvable"));
        if (devis.statut() != StatutDevis.EN_ATTENTE) {
            throw new TransitionInterditeException("Devis déjà traité : " + devis.statut());
        }

        repository.majStatutDevis(devisId, accepte ? StatutDevis.ACCEPTE : StatutDevis.REFUSE);
        if (accepte) {
            changerStatut(devis.demandeId(), StatutDemande.ACCEPTEE, "Devis accepté");
        }
        audit.enregistrer("DEVIS_REPONSE", "devis", devisId, accepte ? "ACCEPTE" : "REFUSE");
        return repository.findDevis(devisId).orElseThrow();
    }

    @Transactional(readOnly = true)
    public List<Reparateur> rechercherReparateurs(String categorie, String competence, String ville) {
        return repository.rechercherReparateurs(categorie, competence, ville);
    }

    @Transactional(readOnly = true)
    public Reparateur reparateur(long id) {
        return repository.findReparateur(id)
                .orElseThrow(() -> new NoSuchElementException("Réparateur " + id + " introuvable"));
    }

    @Transactional(readOnly = true)
    public List<Reparateur> listerReparateurs() {
        return repository.findReparateurs();
    }

    @Transactional
    public Reparateur basculerDisponibilite(long id, boolean disponible) {
        reparateur(id);
        repository.majDisponible(id, disponible);
        audit.enregistrer("REPARATEUR_DISPONIBILITE", "reparateur", id, String.valueOf(disponible));
        return reparateur(id);
    }

    @Transactional
    public void supprimerReparateur(long id) {
        reparateur(id);
        if (repository.compteDemandesDuReparateur(id) > 0) {
            throw new IllegalStateException(
                    "Ce réparateur est associé à des demandes et ne peut pas être supprimé");
        }
        repository.supprimerReparateur(id);
        audit.enregistrer("REPARATEUR_SUPPRIME", "reparateur", id, "suppression administrateur");
    }
}
