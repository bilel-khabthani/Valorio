import { Component, computed, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { toSignal } from '@angular/core/rxjs-interop';
import {
  NavigationEnd,
  Router,
  RouterLink,
  RouterLinkActive,
  RouterOutlet,
} from '@angular/router';
import { filter, map, startWith } from 'rxjs';
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
    if (acteur === 'ADMIN') {
      modules.unshift({ path: '/admin', label: 'Administration' });
    }
    return modules;
  });

  private readonly router = inject(Router);

  protected readonly estEspaceAdmin = toSignal(
    this.router.events.pipe(
      filter((evenement): evenement is NavigationEnd => evenement instanceof NavigationEnd),
      map((evenement) => evenement.urlAfterRedirects.startsWith('/admin')),
      startWith(this.router.url.startsWith('/admin'))
    ),
    { initialValue: this.router.url.startsWith('/admin') }
  );

  onLogout(): void {
    this.auth.logout().subscribe();
  }
}
