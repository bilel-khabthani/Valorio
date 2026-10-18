package com.valorio.auth.service;

import com.valorio.auth.domain.Acteur;
import com.valorio.auth.domain.Role;
import com.valorio.auth.domain.User;
import com.valorio.auth.dto.UserStats;
import com.valorio.auth.repo.UserRepository;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.TreeMap;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Opérations d'administration sur les comptes utilisateurs (module 1). */
@Service
public class UtilisateurAdminService {

    private final UserRepository repository;

    public UtilisateurAdminService(UserRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<User> lister(Acteur acteur, Boolean actif) {
        return repository.findAll().stream()
                .filter(u -> acteur == null || u.acteur() == acteur)
                .filter(u -> actif == null || u.actif() == actif)
                .toList();
    }

    @Transactional
    public User basculerActif(long id, boolean actif) {
        User user = charger(id);
        verifierModifiable(user);
        if (user.role() == Role.ADMIN) {
            throw new IllegalStateException("Le statut d'un compte administrateur ne peut pas être modifié");
        }
        repository.majActif(id, actif);
        return charger(id);
    }

    @Transactional
    public User changerActeur(long id, Acteur acteur) {
        User user = charger(id);
        verifierModifiable(user);
        if (acteur == Acteur.ADMIN) {
            throw new IllegalStateException("Le rôle administrateur ne peut pas être attribué depuis le tableau de bord");
        }
        repository.majActeur(id, acteur);
        return charger(id);
    }

    @Transactional
    public void supprimer(long id) {
        User user = charger(id);
        verifierModifiable(user);
        repository.supprimer(id);
    }

    @Transactional(readOnly = true)
    public UserStats statistiques() {
        List<User> tous = repository.findAll();
        long actifs = tous.stream().filter(User::actif).count();
        long verifies = tous.stream().filter(User::emailVerifie).count();
        TreeMap<String, Long> parActeur = new TreeMap<>();
        for (Acteur acteur : Acteur.values()) {
            parActeur.put(acteur.name(), 0L);
        }
        for (User u : tous) {
            parActeur.merge(u.acteur() == null ? Acteur.UTILISATEUR.name() : u.acteur().name(), 1L, Long::sum);
        }
        TreeMap<String, Long> parRole = new TreeMap<>();
        for (Role role : Role.values()) {
            parRole.put(role.name(), 0L);
        }
        for (User u : tous) {
            parRole.merge(u.role().name(), 1L, Long::sum);
        }
        return new UserStats(tous.size(), actifs, tous.size() - actifs, verifies, parActeur, parRole);
    }

    private void verifierModifiable(User user) {
        if (user.role() == Role.ADMIN) {
            throw new IllegalStateException("Les comptes administrateurs sont protégés");
        }
    }

    private User charger(long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Utilisateur " + id + " introuvable"));
    }
}
