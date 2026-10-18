import { Component, computed, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { Acteur, AuthService, LIBELLES_ROLE, routeAccueil } from '@valorio/shared';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './app.html',
  styleUrl: './app.css',
})
export class App {
  protected readonly title = signal('Valorio');

  protected readonly auth = inject(AuthService);

  protected readonly libelleActeur = computed(() => {
    const acteur = this.auth.acteur();
    return acteur ? LIBELLES_ROLE[acteur] : '';
  });

  protected readonly accueil = computed(() => routeAccueil(this.auth.acteur()));

  protected readonly navModules = computed(() => {
    if (!this.auth.isAuthenticated()) {
      return [];
    }
    const acteur = this.auth.acteur();
    const autorise = (acteurs: Acteur[]) =>
      !acteur || acteur === 'ADMIN' || acteurs.includes(acteur);
    const modules: { path: string; label: string }[] = [];
    if (autorise(['UTILISATEUR', 'REPARATEUR'])) {
      modules.push({ path: '/reparation', label: 'Réparation' });
    }
    if (autorise(['UTILISATEUR', 'ACHETEUR'])) {
      modules.push({ path: '/don', label: 'Don' });
    }
    if (autorise(['UTILISATEUR', 'ORGANISME'])) {
      modules.push({ path: '/recyclage', label: 'Recyclage' });
    }
    modules.push({ path: '/objets', label: 'Objets' });
    if (autorise(['ACHETEUR'])) {
      modules.push({ path: '/marketplace', label: 'Marketplace' });
    }
    return modules;
  });

  onLogout(): void {
    this.auth.logout().subscribe();
  }
}
