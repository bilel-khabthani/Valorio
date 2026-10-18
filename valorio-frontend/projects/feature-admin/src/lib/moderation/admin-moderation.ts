import { CommonModule } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { messageErreur } from '@valorio/shared';
import {
  CATEGORIES,
  Category,
  ItemStatus,
  LIBELLES_CATEGORIE,
  LIBELLES_STATUT_ITEM,
  MarketplaceItem,
  ModerationResponse,
  STATUTS_ITEM,
} from '@valorio/api-marketplace';
import { AdminApiService } from '@valorio/api-admin';

@Component({
  selector: 'app-admin-moderation',
  standalone: true,
  imports: [FormsModule, CommonModule],
  template: `
    <div class="page">
      <div class="page-header">
        <div>
          <h1>Modération</h1>
          <p>Analyser les annonces avec l'IA et réguler les articles marketplace.</p>
        </div>
      </div>

      <div class="card">
        <h2>Analyse IA d'une annonce</h2>
        <div class="toolbar">
          <label class="field">
            <span>Titre</span>
            <input name="titre" [(ngModel)]="titre" />
          </label>
          <label class="field">
            <span>Catégorie</span>
            <select name="categorie" [(ngModel)]="categorie">
              <option [ngValue]="undefined">—</option>
              @for (c of categories; track c) {
                <option [ngValue]="c">{{ libellesCategorie[c] }}</option>
              }
            </select>
          </label>
          <label class="field">
            <span>Prix (indicatif)</span>
            <input name="prix" [(ngModel)]="prix" />
          </label>
        </div>
        <label class="field">
          <span>Description</span>
          <textarea name="description" [(ngModel)]="description" rows="4"></textarea>
        </label>
        <div class="actions">
          <button class="btn" type="button" (click)="analyser()" [disabled]="analyse()">
            Analyser avec l'IA
          </button>
        </div>

        @if (resultat(); as r) {
          <div class="resultat">
            <p>
              <span class="badge" [class.disponible]="r.approved" [class.non]="!r.approved">
                {{ r.approved ? 'Approuvé' : 'Refusé' }}
              </span>
              <span class="badge en_cours">Risque : {{ r.riskLevel }}</span>
              <span class="badge">Score de sûreté : {{ r.safetyScore | number: '1.0-1' }}</span>
            </p>
            @if (r.flaggedIssues.length > 0) {
              <div class="alert error">
                <strong>Points signalés :</strong>
                <ul>
                  @for (issue of r.flaggedIssues; track issue) {
                    <li>{{ issue }}</li>
                  }
                </ul>
              </div>
            }
            <p class="muted">{{ r.aiExplanation }}</p>
          </div>
        }
        @if (erreur(); as e) {
          <div class="alert error">{{ e }}</div>
        }
      </div>

      <div class="card">
        <div class="spread">
          <h2>Articles marketplace</h2>
          <button class="btn secondary small" type="button" (click)="chargerArticles()">
            Rafraîchir
          </button>
        </div>
        @if (chargementArticles()) {
          <p class="muted">Chargement…</p>
        } @else if (articles().length === 0) {
          <p class="empty">Aucun article marketplace.</p>
        } @else {
          <table>
            <thead>
              <tr>
                <th>Titre</th>
                <th>Vendeur</th>
                <th>Prix</th>
                <th>Statut</th>
                <th>Changer le statut</th>
              </tr>
            </thead>
            <tbody>
              @for (item of articles(); track item.id) {
                <tr>
                  <td>{{ item.title }}</td>
                  <td>{{ item.sellerName || '—' }}</td>
                  <td>{{ item.price | number: '1.0-2' }} €</td>
                  <td><span class="badge">{{ libellesStatut[item.status] }}</span></td>
                  <td>
                    <select
                      [ngModel]="item.status"
                      (ngModelChange)="changerStatut(item, $event)"
                    >
                      @for (st of statutsItem; track st) {
                        <option [ngValue]="st">{{ libellesStatut[st] }}</option>
                      }
                    </select>
                  </td>
                </tr>
              }
            </tbody>
          </table>
        }
        @if (message(); as m) {
          <div class="alert success">{{ m }}</div>
        }
      </div>
    </div>
  `,
  styles: `
    .resultat {
      margin-top: 1rem;
      display: flex;
      flex-direction: column;
      gap: 0.5rem;
    }
    .resultat ul {
      margin: 0.35rem 0 0;
      padding-left: 1.1rem;
    }
    table select {
      width: auto;
      padding: 0.25rem 0.4rem;
      font-size: 0.82rem;
    }
  `,
})
export class AdminModeration {
  private readonly adminApi = inject(AdminApiService);

  protected readonly categories = CATEGORIES;
  protected readonly libellesCategorie = LIBELLES_CATEGORIE;
  protected readonly statutsItem = STATUTS_ITEM;
  protected readonly libellesStatut = LIBELLES_STATUT_ITEM;

  protected titre = '';
  protected description = '';
  protected categorie?: Category;
  protected prix = '';

  protected readonly analyse = signal(false);
  protected readonly resultat = signal<ModerationResponse | null>(null);
  protected readonly erreur = signal<string | null>(null);

  protected readonly articles = signal<MarketplaceItem[]>([]);
  protected readonly chargementArticles = signal(true);
  protected readonly message = signal<string | null>(null);

  constructor() {
    this.chargerArticles();
  }

  protected analyser(): void {
    if (!this.titre.trim() || !this.description.trim()) {
      this.erreur.set('Renseignez au moins un titre et une description.');
      return;
    }
    this.analyse.set(true);
    this.erreur.set(null);
    this.resultat.set(null);
    this.adminApi
      .moderer({
        title: this.titre,
        description: this.description,
        category: this.categorie,
        price: this.prix || undefined,
      })
      .subscribe({
        next: (r) => {
          this.resultat.set(r);
          this.analyse.set(false);
        },
        error: (err) => {
          this.erreur.set(messageErreur(err));
          this.analyse.set(false);
        },
      });
  }

  protected chargerArticles(): void {
    this.chargementArticles.set(true);
    this.adminApi.listerObjetsMarketplace().subscribe({
      next: (liste) => {
        this.articles.set(liste);
        this.chargementArticles.set(false);
      },
      error: (err) => {
        this.erreur.set(messageErreur(err));
        this.chargementArticles.set(false);
      },
    });
  }

  protected changerStatut(item: MarketplaceItem, status: ItemStatus): void {
    if (item.id == null || status === item.status) {
      return;
    }
    this.adminApi.changerStatutMarketplace(item.id, status).subscribe({
      next: () => {
        this.articles.set(
          this.articles().map((a) => (a.id === item.id ? { ...a, status } : a)),
        );
        this.message.set(`Statut de « ${item.title} » mis à jour.`);
      },
      error: (err) => this.erreur.set(messageErreur(err)),
    });
  }
}
