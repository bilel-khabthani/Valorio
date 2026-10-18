package com.valorio.objets.service;

import com.valorio.objets.audit.AuditLog;
import com.valorio.objets.domain.Categorie;
import com.valorio.objets.dto.CategorieRequest;
import com.valorio.objets.repo.CategorieRepository;
import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CategorieService {

    private final CategorieRepository repository;
    private final AuditLog audit;

    public CategorieService(CategorieRepository repository, AuditLog audit) {
        this.repository = repository;
        this.audit = audit;
    }

    @Transactional(readOnly = true)
    public List<Categorie> lister() {
        return repository.findCategories();
    }

    @Transactional(readOnly = true)
    public Categorie detail(long id) {
        return repository.findCategorie(id)
                .orElseThrow(() -> new NoSuchElementException("Catégorie " + id + " introuvable"));
    }

    @Transactional
    public Categorie creer(CategorieRequest request) {
        if (repository.existeNom(request.nom())) {
            throw new IllegalStateException("Une catégorie nommée « " + request.nom() + " » existe déjà");
        }
        long id = repository.creer(request.nom(), request.description());
        audit.enregistrer("CATEGORIE_CREEE", "categorie", id, request.nom());
        return detail(id);
    }

    @Transactional
    public Categorie modifier(long id, CategorieRequest request) {
        detail(id);
        if (repository.existeNomSauf(request.nom(), id)) {
            throw new IllegalStateException("Une catégorie nommée « " + request.nom() + " » existe déjà");
        }
        repository.maj(id, request.nom(), request.description());
        audit.enregistrer("CATEGORIE_MODIFIEE", "categorie", id, request.nom());
        return detail(id);
    }

    @Transactional
    public void supprimer(long id) {
        Categorie categorie = detail(id);
        if (repository.compteObjets(id) > 0) {
            throw new IllegalStateException(
                    "Cette catégorie est utilisée par des objets et ne peut pas être supprimée");
        }
        repository.supprimer(id);
        audit.enregistrer("CATEGORIE_SUPPRIMEE", "categorie", id, categorie.nom());
    }
}
