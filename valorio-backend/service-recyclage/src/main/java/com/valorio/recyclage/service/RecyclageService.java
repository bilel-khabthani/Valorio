package com.valorio.recyclage.service;

import com.valorio.recyclage.audit.AuditLog;
import com.valorio.recyclage.domain.AnalyseRecyclabilite;
import com.valorio.recyclage.domain.CentreRecyclage;
import com.valorio.recyclage.domain.DemandeCollecte;
import com.valorio.recyclage.domain.RegleRecyclabilite;
import com.valorio.recyclage.domain.StatutCollecte;
import com.valorio.recyclage.domain.TransitionInterditeException;
import com.valorio.recyclage.domain.VerdictRecyclabilite;
import com.valorio.recyclage.external.ObjetEvenementPort;
import com.valorio.recyclage.external.ObjetPort;
import com.valorio.recyclage.external.UtilisateurPort;
import com.valorio.recyclage.external.model.ObjetRef;
import com.valorio.recyclage.repo.RecyclageRepository;
import com.valorio.recyclage.util.Geo;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RecyclageService {

    private final RecyclageRepository repository;
    private final ObjetPort objets;
    private final UtilisateurPort utilisateurs;
    private final ObjetEvenementPort evenements;
    private final AuditLog audit;

    public RecyclageService(RecyclageRepository repository, ObjetPort objets, UtilisateurPort utilisateurs,
                            ObjetEvenementPort evenements, AuditLog audit) {
        this.repository = repository;
        this.objets = objets;
        this.utilisateurs = utilisateurs;
        this.evenements = evenements;
        this.audit = audit;
    }

    @Transactional(readOnly = true)
    public AnalyseRecyclabilite analyser(long objetId) {
        ObjetRef objet = chargerObjet(objetId);
        AnalyseRecyclabilite analyse = RegleRecyclabilite.analyser(objet);
        if (analyse.recyclable() == VerdictRecyclabilite.NON) {
            return analyse;
        }
        Set<Long> centres = new LinkedHashSet<>();
        for (String materiau : analyse.materiaux()) {
            repository.findCentres(materiau).forEach(centre -> centres.add(centre.id()));
        }
        return new AnalyseRecyclabilite(objetId, analyse.recyclable(), analyse.materiaux(), analyse.motifs(),
                List.copyOf(centres));
    }

    @Transactional(readOnly = true)
    public List<CentreRecyclage> rechercherCentres(String materiau, Double latitude, Double longitude,
                                                   double rayonKm) {
        List<CentreRecyclage> centres = repository.findCentres(materiau);
        boolean geolocalise = latitude != null && longitude != null;
        if (!geolocalise) {
            return centres;
        }
        return centres.stream()
                .filter(centre -> centre.latitude() != null && centre.longitude() != null)
                .map(centre -> centre.avecDistance(arrondi(
                        Geo.distanceKm(latitude, longitude, centre.latitude(), centre.longitude()))))
                .filter(centre -> centre.distanceKm() <= rayonKm)
                .sorted(Comparator.comparingDouble(CentreRecyclage::distanceKm))
                .toList();
    }

    @Transactional(readOnly = true)
    public CentreRecyclage detailCentre(long centreId) {
        return repository.findCentre(centreId)
                .orElseThrow(() -> new NoSuchElementException("Centre " + centreId + " introuvable"));
    }

    @Transactional
    public CentreRecyclage creerCentre(Long organismeId, String nom, String ville, String adresse, Double latitude,
                                       Double longitude, List<String> materiauxAcceptes, String horaires) {
        if (materiauxAcceptes == null || materiauxAcceptes.isEmpty()) {
            throw new IllegalArgumentException("Au moins un matériau accepté est requis");
        }
        long id = repository.creerCentre(organismeId, nom, ville, adresse, latitude, longitude,
                materiauxAcceptes, horaires);
        audit.enregistrer("CENTRE_RECYCLAGE_CREE", "centre_recyclage", id, "nom=" + nom);
        return detailCentre(id);
    }

    @Transactional
    public CentreRecyclage modifierCentre(long centreId, Long organismeId, String nom, String ville, String adresse,
                                          Double latitude, Double longitude, List<String> materiauxAcceptes,
                                          String horaires) {
        detailCentre(centreId);
        if (materiauxAcceptes == null || materiauxAcceptes.isEmpty()) {
            throw new IllegalArgumentException("Au moins un matériau accepté est requis");
        }
        repository.majCentre(centreId, organismeId, nom, ville, adresse, latitude, longitude,
                materiauxAcceptes, horaires);
        audit.enregistrer("CENTRE_RECYCLAGE_MODIFIE", "centre_recyclage", centreId, null);
        return detailCentre(centreId);
    }

    @Transactional
    public DemandeCollecte demanderCollecte(long objetId, long utilisateurId, Long centreId,
                                            OffsetDateTime datePrevue) {
        chargerObjet(objetId);
        utilisateurs.findById(utilisateurId)
                .orElseThrow(() -> new NoSuchElementException("Utilisateur " + utilisateurId + " introuvable"));

        AnalyseRecyclabilite analyse = analyser(objetId);
        if (analyse.recyclable() == VerdictRecyclabilite.NON) {
            throw new IllegalStateException("Objet non recyclable : " + String.join(" ; ", analyse.motifs()));
        }
        if (centreId != null) {
            detailCentre(centreId);
        }

        long id = repository.creerCollecte(objetId, utilisateurId, centreId, datePrevue);
        repository.ajouterHistorique(id, StatutCollecte.DEMANDE, "Demande de collecte");
        evenements.enregistrer(objetId, "RECYCLAGE", "Demande de collecte déposée (collecte " + id + ")",
                utilisateurId, null);
        audit.enregistrer("COLLECTE_DEMANDEE", "demande_collecte", id, "objet=" + objetId);
        return chargerCollecte(id);
    }

    @Transactional(readOnly = true)
    public List<DemandeCollecte> listerCollectes(StatutCollecte statut, Long utilisateurId, Long centreId) {
        return repository.findCollectes(statut, utilisateurId, centreId);
    }

    @Transactional
    public DemandeCollecte changerStatutCollecte(long collecteId, StatutCollecte cible, OffsetDateTime datePrevue,
                                                 String commentaire) {
        DemandeCollecte collecte = chargerCollecte(collecteId);
        if (!collecte.statut().peutTransitionnerVers(cible)) {
            throw new TransitionInterditeException("Transition interdite : " + collecte.statut() + " -> " + cible);
        }
        repository.majStatutCollecte(collecteId, cible, datePrevue);
        repository.ajouterHistorique(collecteId, cible, commentaire);
        if (cible == StatutCollecte.RECUE) {
            evenements.enregistrer(collecte.objetId(), "RECYCLAGE",
                    "Objet remis au centre de recyclage (collecte " + collecteId + ")",
                    collecte.utilisateurId(), "RECYCLE");
        }
        audit.enregistrer("COLLECTE_STATUT_MODIFIE", "demande_collecte", collecteId,
                collecte.statut() + " -> " + cible);
        return chargerCollecte(collecteId);
    }

    @Transactional(readOnly = true)
    public DemandeCollecte chargerCollecte(long collecteId) {
        DemandeCollecte collecte = repository.findCollecte(collecteId)
                .orElseThrow(() -> new NoSuchElementException("Collecte " + collecteId + " introuvable"));
        return new DemandeCollecte(collecte.id(), collecte.objetId(), collecte.utilisateurId(), collecte.centreId(),
                collecte.statut(), collecte.dateDemande(), collecte.datePrevue(), collecte.dateReception(),
                repository.findHistorique(collecteId));
    }

    private ObjetRef chargerObjet(long objetId) {
        return objets.findById(objetId)
                .orElseThrow(() -> new NoSuchElementException("Objet " + objetId + " introuvable"));
    }

    private static double arrondi(double valeur) {
        return Math.round(valeur * 100.0) / 100.0;
    }
}
