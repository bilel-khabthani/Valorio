package com.valorio.auth.security;

import static org.assertj.core.api.Assertions.assertThat;

import com.valorio.auth.domain.Role;
import com.valorio.auth.domain.User;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

class RoleAuthorizationTest {

    @Test
    @DisplayName("UserPrincipal assigne l'autorité ROLE_USER pour un utilisateur standard")
    void assigneAutoriteRoleUser() {
        User user = new User(1L, "user@valorio.tn", "hash", "Nom", "Prenom", Role.USER, true, true, OffsetDateTime.now(), OffsetDateTime.now());
        UserPrincipal principal = new UserPrincipal(user);

        assertThat(principal.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_USER");
        assertThat(principal.isEnabled()).isTrue();
        assertThat(principal.isAccountNonLocked()).isTrue();
    }

    @Test
    @DisplayName("UserPrincipal assigne l'autorité ROLE_ADMIN pour un administrateur")
    void assigneAutoriteRoleAdmin() {
        User admin = new User(2L, "admin@valorio.tn", "hash", "Admin", "Super", Role.ADMIN, true, true, OffsetDateTime.now(), OffsetDateTime.now());
        UserPrincipal principal = new UserPrincipal(admin);

        assertThat(principal.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_ADMIN");
    }

    @Test
    @DisplayName("Désactive le compte si l'attribut actif est faux")
    void desactiveCompteInactif() {
        User userInactif = new User(3L, "inactif@valorio.tn", "hash", "Inactif", "Test", Role.USER, false, true, OffsetDateTime.now(), OffsetDateTime.now());
        UserPrincipal principal = new UserPrincipal(userInactif);

        assertThat(principal.isEnabled()).isFalse();
        assertThat(principal.isAccountNonLocked()).isFalse();
    }
}
