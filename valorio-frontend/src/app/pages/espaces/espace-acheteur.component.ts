import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AuthService } from '@valorio/shared';

@Component({
  selector: 'app-espace-acheteur',
  standalone: true,
  imports: [CommonModule],
  template: `
    <section class="espace">
      <header class="espace-head">
        <h1>Espace acheteur</h1>
        <p>Bonjour {{ auth.userName() }}.</p>
      </header>

      <div class="placeholder">
        <span class="badge">Bientôt</span>
        <h2>Marketplace à venir</h2>
        <p>
          La recherche d'objets, la consultation des annonces, l'achat, la communication
          et l'évaluation seront disponibles dans une prochaine version.
        </p>
      </div>
    </section>
  `,
  styles: [
    `
      .espace { max-width: 960px; margin: 2rem auto; padding: 0 1rem; }
      .espace-head h1 { margin: 0 0 0.25rem; }
      .espace-head p { margin: 0 0 1.5rem; color: #5b6472; }
      .placeholder { padding: 2.5rem 1.5rem; border: 1px dashed #c8d0dc; border-radius: 12px; text-align: center; background: #fafbfd; }
      .placeholder h2 { margin: 0.75rem 0 0.5rem; }
      .placeholder p { margin: 0 auto; max-width: 520px; color: #5b6472; }
      .badge { display: inline-block; padding: 0.15rem 0.6rem; border-radius: 999px; background: #eef2ff; color: #3b4cca; font-size: 0.75rem; font-weight: 600; }
    `,
  ],
})
export class EspaceAcheteurComponent {
  protected readonly auth = inject(AuthService);
}
