import { CommonModule } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { Reparateur, messageErreur } from '@valorio/shared';
import { AdminApiService } from '@valorio/api-admin';

@Component({
  selector: 'app-admin-reparateurs',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="page">
      <div class="page-header">
        <div>
          <h1>Réparateurs</h1>
          <p>Superviser les professionnels, leur disponibilité et leur activité.</p>
        </div>
        <button class="btn secondary" type="button" (click)="charger()" [disabled]="chargement()">
          Rafraîchir
        </button>
      </div>

      @if (message(); as m) {
        <div class="alert success">{{ m }}</div>
      }
      @if (erreur(); as e) {
        <div class="alert error">{{ e }}</div>
      }

      <div class="card">
        @if (chargement()) {
          <p class="muted">Chargement…</p>
        } @else if (reparateurs().length === 0) {
          <p class="empty">Aucun réparateur enregistré.</p>
        } @else {
          <table>
            <thead>
              <tr>
                <th>Réparateur</th>
                <th>Ville</th>
                <th>Compétences</th>
                <th>Note</th>
                <th>Disponibilité</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              @for (r of reparateurs(); track r.id) {
                <tr>
                  <td>#{{ r.id }}{{ r.utilisateurId ? ' · util. #' + r.utilisateurId : '' }}</td>
                  <td>{{ r.ville || '—' }}</td>
                  <td>{{ r.competences || '—' }}</td>
                  <td>{{ r.noteMoyenne | number: '1.1-1' }} ({{ r.nbAvis }} avis)</td>
                  <td>
                    <span class="badge" [class.disponible]="r.disponible" [class.non]="!r.disponible">
                      {{ r.disponible ? 'Disponible' : 'Indisponible' }}
                    </span>
                  </td>
                  <td>
                    <div class="actions">
                      <button class="btn small secondary" type="button" (click)="basculer(r)">
                        {{ r.disponible ? 'Basculer en indisponible' : 'Basculer en disponible' }}
                      </button>
                      <button class="btn small danger" type="button" (click)="supprimer(r)">
                        Supprimer
                      </button>
                    </div>
                  </td>
                </tr>
              }
            </tbody>
          </table>
        }
      </div>
    </div>
  `,
  styles: `
    table {
      min-width: 720px;
    }
  `,
})
export class AdminReparateurs {
  private readonly adminApi = inject(AdminApiService);

  protected readonly reparateurs = signal<Reparateur[]>([]);
  protected readonly chargement = signal(true);
  protected readonly erreur = signal<string | null>(null);
  protected readonly message = signal<string | null>(null);

  constructor() {
    this.charger();
  }

  protected charger(): void {
    this.chargement.set(true);
    this.erreur.set(null);
    this.message.set(null);
    this.adminApi.listerReparateurs().subscribe({
      next: (liste) => {
        this.reparateurs.set(liste);
        this.chargement.set(false);
      },
      error: (err) => {
        this.erreur.set(messageErreur(err));
        this.chargement.set(false);
      },
    });
  }

  protected basculer(r: Reparateur): void {
    this.adminApi.changerDisponibilite(r.id, !r.disponible).subscribe({
      next: () => {
        this.message.set(`Disponibilité du réparateur #${r.id} mise à jour.`);
        this.charger();
      },
      error: (err) => this.erreur.set(messageErreur(err)),
    });
  }

  protected supprimer(r: Reparateur): void {
    if (!confirm(`Supprimer le réparateur #${r.id} ?`)) {
      return;
    }
    this.adminApi.supprimerReparateur(r.id).subscribe({
      next: () => {
        this.message.set(`Réparateur #${r.id} supprimé.`);
        this.charger();
      },
      error: (err) => this.erreur.set(messageErreur(err)),
    });
  }
}
