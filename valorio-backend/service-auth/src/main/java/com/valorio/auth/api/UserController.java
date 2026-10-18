package com.valorio.auth.api;

import com.valorio.auth.domain.Acteur;
import com.valorio.auth.dto.UserResponse;
import com.valorio.auth.dto.UserStats;
import com.valorio.auth.repo.UserRepository;
import com.valorio.auth.service.UtilisateurAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Annuaire des utilisateurs destiné aux appels serveur à serveur (service-objets,
 * service-don, service-recyclage, service-reparation) et le front-end. Les endpoints GET sont publics.
 * Les opérations d'administration (activation, rôle, suppression) sont réservées aux administrateurs.
 */
@RestController
@RequestMapping("/api/users")
@Tag(name = "Utilisateurs", description = "Annuaire des utilisateurs et administration des comptes")
public class UserController {

    private final UserRepository userRepository;
    private final UtilisateurAdminService adminService;

    public UserController(UserRepository userRepository, UtilisateurAdminService adminService) {
        this.userRepository = userRepository;
        this.adminService = adminService;
    }

    @GetMapping
    @Operation(summary = "Liste des utilisateurs (appels inter-services), filtrable pour l'administration")
    public List<UserResponse> lister(@RequestParam(required = false) Acteur acteur,
                                     @RequestParam(required = false) Boolean actif) {
        if (acteur == null && actif == null) {
            return userRepository.findAll().stream()
                    .map(UserResponse::fromDomain)
                    .toList();
        }
        return adminService.lister(acteur, actif).stream()
                .map(UserResponse::fromDomain)
                .toList();
    }

    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Statistiques des comptes utilisateurs (administration)")
    public UserStats statistiques() {
        return adminService.statistiques();
    }

    @GetMapping("/{userId}")
    @Operation(summary = "Utilisateur par identifiant (appels inter-services)")
    public UserResponse consulter(@PathVariable long userId) {
        return userRepository.findById(userId)
                .map(UserResponse::fromDomain)
                .orElseThrow(() -> new NoSuchElementException("Utilisateur " + userId + " introuvable"));
    }

    @PutMapping("/{userId}/actif")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Activer ou désactiver un compte (administration)")
    public UserResponse changerActif(@PathVariable long userId, @RequestParam boolean actif) {
        return UserResponse.fromDomain(adminService.basculerActif(userId, actif));
    }

    @PutMapping("/{userId}/acteur")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Modifier l'acteur métier d'un compte (administration)")
    public UserResponse changerActeur(@PathVariable long userId, @RequestParam Acteur acteur) {
        return UserResponse.fromDomain(adminService.changerActeur(userId, acteur));
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Supprimer un compte (administration)")
    public void supprimer(@PathVariable long userId) {
        adminService.supprimer(userId);
    }
}
