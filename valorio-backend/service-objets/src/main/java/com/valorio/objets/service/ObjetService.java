package com.valorio.objets.service;

import com.valorio.objets.audit.AuditLog;
import com.valorio.objets.domain.EvenementHistorique;
import com.valorio.objets.domain.Objet;
import com.valorio.objets.domain.StatutObjet;
import com.valorio.objets.domain.TypeEvenement;
import com.valorio.objets.dto.ObjetRequest;
import com.valorio.objets.external.UtilisateurPort;
import com.valorio.objets.repo.CategorieRepository;
import com.valorio.objets.repo.HistoriqueRepository;
import com.valorio.objets.repo.ObjetRepository;
import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ObjetService {

    private final ObjetRepository repository;
    private final CategorieRepository categories;
    private final HistoriqueRepository historique;
    private final UtilisateurPort utilisateurs;
    private final FileStorageService fichiers;
    private final AuditLog audit;

    public ObjetService(ObjetRepository repository,
                        CategorieRepository categories,
                        HistoriqueRepository historique,
                        UtilisateurPort utilisateurs,
                        FileStorageService fichiers,
                        AuditLog audit) {
        this.repository = repository;
        this.categories = categories;
        this.historique = historique;
        this.utilisateurs = utilisateurs;
        this.fichiers = fichiers;
        this.audit = audit;
    }

    @Transactional(readOnly = true)
    public List<Objet> lister(Long categorieId, com.valorio.objets.domain.EtatObjet etat, String ville,
                              StatutObjet statut, String q) {
        return repository.findObjets(categorieId, etat, ville, statut, q);
    }

    @Transactional(readOnly = true)
    public Objet detail(long id) {
        return repository.findObjet(id)
                .orElseThrow(() -> new NoSuchElementException("Objet " + id + " introuvable"));
    }

    @Transactional
    public Objet creer(ObjetRequest request) {
        validerReferentiels(request);
        long id = repository.creer(request.intitule(), request.description(), request.categorieId(), request.etat(),
                request.ageAnnees(), request.valeurEstimee(), request.ville(), request.latitude(),
                request.longitude(), request.materiaux(), request.proprietaireId());
        historique.ajouter(id, TypeEvenement.DECLARATION, "Objet déclaré : " + request.intitule(),
                request.proprietaireId(), StatutObjet.DECLARE);
        audit.enregistrer("OBJET_DECLARE", "objet", id, "categorie=" + request.categorieId());
        return detail(id);
    }

    @Transactional
    public Objet modifier(long id, ObjetRequest request) {
        Objet objet = detail(id);
        validerReferentiels(request);
        repository.maj(id, request.intitule(), request.description(), request.categorieId(), request.etat(),
                request.ageAnnees(), request.valeurEstimee(), request.ville(), request.latitude(),
                request.longitude(), request.materiaux(), request.proprietaireId());
        historique.ajouter(id, TypeEvenement.MODIFICATION, "Fiche objet modifiée", request.proprietaireId(), null);
        audit.enregistrer("OBJET_MODIFIE", "objet", id, "statut=" + objet.statut());
        return detail(id);
    }

    @Transactional
    public void supprimer(long id) {
        Objet objet = detail(id);
        objet.photos().forEach(fichiers::delete);
        repository.supprimer(id);
        audit.enregistrer("OBJET_SUPPRIME", "objet", id, objet.intitule());
    }

    @Transactional
    public Objet changerStatut(long id, StatutObjet cible, String commentaire, Long acteurId) {
        Objet objet = detail(id);
        if (!objet.statut().peutTransitionnerVers(cible)) {
            throw new IllegalStateException(
                    "Transition interdite : " + objet.statut() + " -> " + cible);
        }
        repository.majStatut(id, cible);
        String libelle = commentaire == null || commentaire.isBlank()
                ? "Statut : " + objet.statut() + " -> " + cible
                : commentaire;
        historique.ajouter(id, TypeEvenement.STATUT, libelle, acteurId, cible);
        audit.enregistrer("OBJET_STATUT_MODIFIE", "objet", id, objet.statut() + " -> " + cible);
        return detail(id);
    }

    @Transactional
    public Objet ajouterPhotos(long id, List<MultipartFile> photos) {
        detail(id);
        if (photos == null) {
            return detail(id);
        }
        int ajoutees = 0;
        for (MultipartFile photo : photos) {
            String url = fichiers.store(photo);
            if (url != null) {
                repository.ajouterPhoto(id, url);
                ajoutees++;
            }
        }
        if (ajoutees > 0) {
            historique.ajouter(id, TypeEvenement.PHOTO, ajoutees + " photo(s) ajoutée(s)", null, null);
            audit.enregistrer("OBJET_PHOTO_AJOUTEE", "objet", id, "photos=" + ajoutees);
        }
        return detail(id);
    }

    @Transactional
    public Objet supprimerPhoto(long id, long photoId) {
        detail(id);
        if (!repository.supprimerPhoto(id, photoId)) {
            throw new NoSuchElementException("Photo " + photoId + " introuvable pour l'objet " + id);
        }
        historique.ajouter(id, TypeEvenement.PHOTO, "Photo supprimée", null, null);
        return detail(id);
    }

    @Transactional(readOnly = true)
    public List<String> villes() {
        return repository.listerVilles();
    }

    @Transactional(readOnly = true)
    public List<EvenementHistorique> historique(long id) {
        return historique.findEvenements(id);
    }

    /**
     * Point d'entrée de la collaboration inter-services : les services don,
     * recyclage et réparation remontent ici leurs événements (vente, don,
     * recyclage, réparation). Le statut n'est appliqué que si la transition est
     * autorisée, sinon l'événement est simplement journalisé.
     */
    @Transactional
    public EvenementHistorique enregistrerEvenement(long id, TypeEvenement type, String libelle,
                                                    Long acteurId, StatutObjet statutCible) {
        Objet objet = detail(id);
        StatutObjet applique = null;
        if (statutCible != null && objet.statut().peutTransitionnerVers(statutCible)) {
            repository.majStatut(id, statutCible);
            applique = statutCible;
        }
        long evenementId = historique.ajouter(id, type, libelle, acteurId, applique);
        return historique.findEvenements(id).stream()
                .filter(evenement -> evenement.id() == evenementId)
                .findFirst()
                .orElseThrow();
    }

    private void validerReferentiels(ObjetRequest request) {
        if (!categories.findCategorie(request.categorieId()).isPresent()) {
            throw new NoSuchElementException("Catégorie " + request.categorieId() + " introuvable");
        }
        utilisateurs.findById(request.proprietaireId())
                .orElseThrow(() -> new NoSuchElementException(
                        "Utilisateur " + request.proprietaireId() + " introuvable"));
    }
}
