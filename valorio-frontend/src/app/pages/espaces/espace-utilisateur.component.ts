import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { AuthService } from '@valorio/shared';

@Component({
  selector: 'app-espace-utilisateur',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <section class="espace">
      <header class="espace-head">
        <h1>Espace utilisateur</h1>
        <p>Bonjour {{ auth.userName() }}, gérez ici vos objets et vos opérations.</p>
      </header>

      <div class="espace-grid">
        <a class="carte" routerLink="/objets">
          <h2>Mes objets</h2>
          <p>Déclarer un objet, consulter son état et obtenir une recommandation.</p>
        </a>
        <a class="carte" routerLink="/reparation">
          <h2>Réparation</h2>
          <p>Demander une réparation et suivre son avancement.</p>
        </a>
        <a class="carte" routerLink="/don">
          <h2>Don</h2>
          <p>Donner un objet et suivre les demandes reçues.</p>
        </a>
        <a class="carte" routerLink="/recyclage">
          <h2>Recyclage</h2>
          <p>Demander la collecte d'un objet non réparable.</p>
        </a>
      </div>
    </section>
  `,
  styles: [
    `
      .espace { max-width: 960px; margin: 2rem auto; padding: 0 1rem; }
      .espace-head h1 { margin: 0 0 0.25rem; }
      .espace-head p { margin: 0 0 1.5rem; color: #5b6472; }
      .espace-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(220px, 1fr)); gap: 1rem; }
      .carte { display: block; padding: 1.25rem; border: 1px solid #e3e7ee; border-radius: 12px; background: #fff; text-decoration: none; color: inherit; transition: box-shadow 0.15s, transform 0.15s; }
      .carte:hover { box-shadow: 0 6px 18px rgba(20, 40, 80, 0.08); transform: translateY(-2px); }
      .carte h2 { margin: 0 0 0.35rem; font-size: 1.05rem; }
      .carte p { margin: 0; color: #5b6472; font-size: 0.9rem; }
    `,
  ],
})
export class EspaceUtilisateurComponent {
  protected readonly auth = inject(AuthService);
}
