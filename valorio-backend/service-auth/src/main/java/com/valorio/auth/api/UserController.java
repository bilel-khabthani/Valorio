package com.valorio.auth.api;

import com.valorio.auth.dto.UserResponse;
import com.valorio.auth.repo.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Annuaire des utilisateurs destiné aux appels serveur à serveur (service-objets,
 * service-don, service-recyclage, service-reparation) et le front-end. Les endpoints GET sont publics.
 */
@RestController
@RequestMapping("/api/users")
@Tag(name = "Utilisateurs", description = "Annuaire des utilisateurs (lecture)")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping
    @Operation(summary = "Liste des utilisateurs (appels inter-services)")
    public List<UserResponse> lister() {
        return userRepository.findAll().stream()
                .map(UserResponse::fromDomain)
                .toList();
    }

    @GetMapping("/{userId}")
    @Operation(summary = "Utilisateur par identifiant (appels inter-services)")
    public UserResponse consulter(@PathVariable long userId) {
        return userRepository.findById(userId)
                .map(UserResponse::fromDomain)
                .orElseThrow(() -> new NoSuchElementException("Utilisateur " + userId + " introuvable"));
    }
}
