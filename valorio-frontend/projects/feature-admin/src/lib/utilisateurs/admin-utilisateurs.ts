import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ACTEURS, Acteur, LIBELLES_ACTEUR, messageErreur } from '@valorio/shared';
import { AdminApiService, UserStats, UtilisateurAdmin } from '@valorio/api-admin';

@Component({
  selector: 'app-admin-utilisateurs',
  standalone: true,
  imports: [FormsModule],
  template: `
    <div class="page">
      <div class="page-header">
        <div>
          <h1>Utilisateurs</h1>
          <p>Gérer les comptes, leurs rôles et leur accès à la plateforme.</p>
        </div>
      </div>

      @if (stats(); as s) {
        <div class="grid-cards">
          <div class="tile">
            <span class="kpi">{{ s.total }}</span>
            <span class="muted">Comptes au total</span>
          </div>
          <div class="tile">
            <span class="kpi">{{ s.actifs }}</span>
            <span class="muted">Actifs</span>
          </div>
          <div class="tile">
            <span class="kpi">{{ s.inactifs }}</span>
            <span class="muted">Désactivés</span>
          </div>
          <div class="tile">
            <span class="kpi">{{ s.verifies }}</span>
            <span class="muted">E-mail vérifié</span>
          </div>
        </div>
      }

      <div class="card">
        <div class="toolbar">
          <label class="field">
            <span>Acteur</span>
            <select [ngModel]="filtreActeur()" (ngModelChange)="filtreActeur.set($event); charger()">
              <option value="">Tous</option>
              @for (a of acteurs; track a) {
                <option [value]="a">{{ libellesActeur[a] }}</option>
              }
            </select>
          </label>
          <label class="field">
            <span>Statut</span>
            <select [ngModel]="filtreActif()" (ngModelChange)="filtreActif.set($event); charger()">
              <option value="">Tous</option>
              <option value="true">Actifs</option>
              <option value="false">Désactivés</option>
            </select>
          </label>
          <button class="btn secondary" type="button" (click)="charger()" [disabled]="chargement()">
            Rafraîchir
          </button>
        </div>
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
        } @else if (utilisateurs().length === 0) {
          <p class="empty">Aucun utilisateur ne correspond aux filtres.</p>
        } @else {
          <table>
            <thead>
              <tr>
                <th>Nom</th>
                <th>E-mail</th>
                <th>Acteur</th>
                <th>Vérifié</th>
                <th>Statut</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              @for (u of utilisateurs(); track u.id) {
                <tr>
                  <td>{{ u.nomComplet }}</td>
                  <td>{{ u.email }}</td>
                  <td>
                    <span class="badge">{{ libellesActeur[u.acteur] }}</span>
                    @if (u.role === 'ADMIN') {
                      <span class="badge en_cours">Admin</span>
                    }
                  </td>
                  <td>{{ u.emailVerifie ? 'Oui' : 'Non' }}</td>
                  <td>
                    <span class="badge" [class.non]="!u.actif">{{ u.actif ? 'Actif' : 'Désactivé' }}</span>
                  </td>
                  <td>
                    @if (u.role === 'ADMIN') {
                      <span class="muted">Compte protégé</span>
                    } @else {
                      <div class="actions">
                        <button class="btn small secondary" type="button" (click)="basculerActif(u)">
                          {{ u.actif ? 'Désactiver' : 'Activer' }}
                        </button>
                        <select
                          class="acteur-select"
                          [ngModel]="u.acteur"
                          (ngModelChange)="changerActeur(u, $event)"
                        >
                          @for (a of acteurs; track a) {
                            <option [value]="a">{{ libellesActeur[a] }}</option>
                          }
                        </select>
                        <button class="btn danger small" type="button" (click)="supprimer(u)">
                          Supprimer
                        </button>
                      </div>
                    }
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
    .kpi {
      font-size: 1.6rem;
      font-weight: 700;
      color: var(--primary);
      line-height: 1;
    }
    table select {
      width: auto;
      padding: 0.25rem 0.4rem;
      font-size: 0.82rem;
    }
  `,
})
export class AdminUtilisateurs {
  private readonly adminApi = inject(AdminApiService);

  protected readonly acteurs = ACTEURS;
  protected readonly libellesActeur = LIBELLES_ACTEUR;

  protected readonly utilisateurs = signal<UtilisateurAdmin[]>([]);
  protected readonly stats = signal<UserStats | null>(null);
  protected readonly chargement = signal(true);
  protected readonly erreur = signal<string | null>(null);
  protected readonly message = signal<string | null>(null);
  protected readonly filtreActeur = signal<string>('');
  protected readonly filtreActif = signal<string>('');

  constructor() {
    this.charger();
  }

  protected charger(): void {
    this.chargement.set(true);
    this.erreur.set(null);
    this.message.set(null);

    const actif = this.filtreActif() === '' ? undefined : this.filtreActif() === 'true';
    this.adminApi
      .listerUtilisateurs({
        acteur: (this.filtreActeur() || undefined) as Acteur | undefined,
        actif,
      })
      .subscribe({
        next: (liste) => {
          this.utilisateurs.set(liste);
          this.chargement.set(false);
        },
        error: (err) => {
          this.erreur.set(messageErreur(err));
          this.chargement.set(false);
        },
      });

    this.adminApi.statsUtilisateurs().subscribe({
      next: (s) => this.stats.set(s),
      error: () => this.stats.set(null),
    });
  }

  protected basculerActif(u: UtilisateurAdmin): void {
    this.adminApi.changerActif(u.id, !u.actif).subscribe({
      next: () => {
        this.message.set(`${u.nomComplet} est désormais ${u.actif ? 'désactivé' : 'actif'}.`);
        this.charger();
      },
      error: (err) => this.erreur.set(messageErreur(err)),
    });
  }

  protected changerActeur(u: UtilisateurAdmin, acteur: string): void {
    if (acteur === u.acteur) {
      return;
    }
    this.adminApi.changerActeur(u.id, acteur).subscribe({
      next: () => {
        this.message.set(`Rôle de ${u.nomComplet} mis à jour.`);
        this.charger();
      },
      error: (err) => this.erreur.set(messageErreur(err)),
    });
  }

  protected supprimer(u: UtilisateurAdmin): void {
    if (!confirm(`Supprimer définitivement le compte de ${u.nomComplet} ?`)) {
      return;
    }
    this.adminApi.supprimerUtilisateur(u.id).subscribe({
      next: () => {
        this.message.set(`Compte de ${u.nomComplet} supprimé.`);
        this.charger();
      },
      error: (err) => this.erreur.set(messageErreur(err)),
    });
  }
}
