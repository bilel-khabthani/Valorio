import { Component, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { formaterDate, messageErreur } from '@valorio/shared';
import {
  LIBELLES_CIBLE_SIGNALEMENT,
  LIBELLES_STATUT_SIGNALEMENT,
  STATUTS_SIGNALEMENT,
  Signalement,
  SignalementStats,
  SignalementsApiService,
  StatutSignalement,
} from '@valorio/api-admin';

@Component({
  selector: 'app-admin-signalements',
  standalone: true,
  imports: [FormsModule],
  template: `
    <div class="page">
      <div class="page-header">
        <div>
          <h1>Signalements</h1>
          <p>Traiter les signalements envoyés par les utilisateurs et assurer la modération.</p>
        </div>
        <button class="btn secondary" type="button" (click)="charger()" [disabled]="chargement()">
          Rafraîchir
        </button>
      </div>

      @if (stats(); as s) {
        <div class="grid-cards">
          <div class="tile"><span class="kpi">{{ s.ouverts }}</span><span class="muted">Ouverts</span></div>
          <div class="tile"><span class="kpi">{{ s.enCours }}</span><span class="muted">En cours</span></div>
          <div class="tile">
            <span class="kpi">{{ s.traites }}</span><span class="muted">Traités</span>
          </div>
          <div class="tile">
            <span class="kpi">{{ s.rejetes }}</span>
            <span class="muted">Rejetés</span>
          </div>
        </div>
      }

      <div class="card">
        <div class="toolbar">
          <label class="field">
            <span>Statut</span>
            <select [ngModel]="filtreStatut()" (ngModelChange)="filtreStatut.set($event); charger()">
              <option value="">Tous</option>
              @for (st of statuts; track st) {
                <option [value]="st">{{ libellesStatut[st] }}</option>
              }
            </select>
          </label>
        </div>
      </div>

      @if (message(); as m) {
        <div class="alert success">{{ m }}</div>
      }
      @if (erreur(); as e) {
        <div class="alert error">{{ e }}</div>
      }

      @if (selection(); as sig) {
        <div class="card">
          <h2>Traiter le signalement #{{ sig.id }}</h2>
          <p class="muted">
            {{ libellesCible[sig.cibleType] }} · motif : {{ sig.motif }}
            @if (sig.cibleId) {
              (cible #{{ sig.cibleId }})
            }
          </p>
          @if (sig.description) {
            <p>{{ sig.description }}</p>
          }
          <div class="toolbar">
            <label class="field">
              <span>Nouveau statut</span>
              <select [(ngModel)]="statutTraitement">
                @for (st of statuts; track st) {
                  <option [value]="st">{{ libellesStatut[st] }}</option>
                }
              </select>            </label>
          </div>
          <label class="field">
            <span>Note de traitement</span>
            <textarea [(ngModel)]="noteTraitement" rows="3"></textarea>
          </label>
          <div class="actions">
            <button class="btn" type="button" (click)="traiter()" [disabled]="traitement()">
              Enregistrer
            </button>
            <button class="btn secondary" type="button" (click)="selection.set(null)">Annuler</button>
          </div>
        </div>
      }

      <div class="card">
        @if (chargement()) {
          <p class="muted">Chargement…</p>
        } @else if (signalements().length === 0) {
          <p class="empty">Aucun signalement.</p>
        } @else {
          <table>
            <thead>
              <tr>
                <th>#</th>
                <th>Cible</th>
                <th>Motif</th>
                <th>Statut</th>
                <th>Reçu le</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              @for (s of signalements(); track s.id) {
                <tr>
                  <td>{{ s.id }}</td>
                  <td>{{ libellesCible[s.cibleType] }}</td>
                  <td>{{ s.motif }}</td>
                  <td>
                    <span class="badge" [class]="classeStatut(s.statut)">
                      {{ libellesStatut[s.statut] }}
                    </span>
                  </td>
                  <td>{{ formater(s.dateCreation) }}</td>
                  <td>
                    <button class="btn small secondary" type="button" (click)="ouvrir(s)">
                      Traiter
                    </button>
                  </td>
                </tr>
              }
            </tbody>
          </table>
        }
      </div>
    </div>
  `,
})
export class AdminSignalements {
  private readonly signalementsApi = inject(SignalementsApiService);

  protected readonly formater = formaterDate;
  protected readonly libellesStatut = LIBELLES_STATUT_SIGNALEMENT;
  protected readonly libellesCible = LIBELLES_CIBLE_SIGNALEMENT;
  protected readonly statuts = STATUTS_SIGNALEMENT;

  protected readonly signalements = signal<Signalement[]>([]);
  protected readonly stats = signal<SignalementStats | null>(null);
  protected readonly chargement = signal(true);
  protected readonly traitement = signal(false);
  protected readonly erreur = signal<string | null>(null);
  protected readonly message = signal<string | null>(null);
  protected readonly filtreStatut = signal<string>('');
  protected readonly selection = signal<Signalement | null>(null);

  protected statutTraitement: StatutSignalement = 'TRAITE';
  protected noteTraitement = '';

  constructor() {
    this.charger();
  }

  protected classeStatut(statut: StatutSignalement): string {
    return statut.toLowerCase().replace('_', '-');
  }

  protected charger(): void {
    this.chargement.set(true);
    this.erreur.set(null);
    this.message.set(null);

    this.signalementsApi
      .lister({ statut: (this.filtreStatut() || undefined) as StatutSignalement | undefined })
      .subscribe({
        next: (liste) => {
          this.signalements.set(liste);
          this.chargement.set(false);
        },
        error: (err) => {
          this.erreur.set(messageErreur(err));
          this.chargement.set(false);
        },
      });

    this.signalementsApi.stats().subscribe({
      next: (s) => this.stats.set(s),
      error: () => this.stats.set(null),
    });
  }

  protected ouvrir(s: Signalement): void {
    this.selection.set(s);
    this.statutTraitement = s.statut === 'OUVERT' ? 'EN_COURS' : s.statut;
    this.noteTraitement = s.noteTraitement ?? '';
  }

  protected traiter(): void {
    const sig = this.selection();
    if (!sig) {
      return;
    }
    this.traitement.set(true);
    this.signalementsApi
      .traiter(sig.id, { statut: this.statutTraitement, note: this.noteTraitement || undefined })
      .subscribe({
        next: () => {
          this.message.set(`Signalement #${sig.id} traité.`);
          this.selection.set(null);
          this.traitement.set(false);
          this.charger();
        },
        error: (err) => {
          this.erreur.set(messageErreur(err));
          this.traitement.set(false);
        },
      });
  }
}
