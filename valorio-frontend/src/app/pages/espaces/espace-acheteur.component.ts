import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { AuthService } from '@valorio/shared';

@Component({
  selector: 'app-espace-acheteur',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <section class="espace">
      <header class="espace-head">
        <h1>Espace acheteur</h1>
        <p>Bonjour {{ auth.userName() }}.</p>
      </header>

      <div class="carte">
        <span class="badge">Disponible</span>
        <h2>Marketplace solidaire</h2>
        <p>
          Découvrez les objets trouvés non réclamés revalorisés : tarification équitable assistée
          par IA, modération automatique, offres d'achat et demandes d'attribution solidaire.
        </p>
        <div class="actions">
          <a class="btn" routerLink="/marketplace">Parcourir la marketplace</a>
          <a class="btn secondaire" routerLink="/marketplace/ai-lab">Laboratoire IA</a>
        </div>
      </div>
    </section>
  `,
  styles: [
    `
      .espace { max-width: 960px; margin: 2rem auto; padding: 0 1rem; }
      .espace-head h1 { margin: 0 0 0.25rem; }
      .espace-head p { margin: 0 0 1.5rem; color: #5b6472; }
      .carte { padding: 2.5rem 1.5rem; border: 1px solid #e2e8f0; border-radius: 12px; text-align: center; background: #fafbfd; }
      .carte h2 { margin: 0.75rem 0 0.5rem; }
      .carte p { margin: 0 auto 1.25rem; max-width: 560px; color: #5b6472; }
      .actions { display: flex; gap: 0.6rem; justify-content: center; flex-wrap: wrap; }
      .btn { display: inline-block; padding: 0.55rem 1.1rem; border-radius: 8px; background: #3b4cca; color: #fff; font-weight: 600; text-decoration: none; }
      .btn.secondaire { background: #eef2ff; color: #3b4cca; }
      .badge { display: inline-block; padding: 0.15rem 0.6rem; border-radius: 999px; background: #eef2ff; color: #3b4cca; font-size: 0.75rem; font-weight: 600; }
    `,
  ],
})
export class EspaceAcheteurComponent {
  protected readonly auth = inject(AuthService);
}
