import { CommonModule } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { AnnonceDon, Objet, formaterDate, messageErreur } from '@valorio/shared';
import {
  ItemStatus,
  ItemType,
  LIBELLES_STATUT_ITEM,
  LIBELLES_TYPE,
  MarketplaceItem,
} from '@valorio/api-marketplace';
import { AdminApiService } from '@valorio/api-admin';
import { Observable, catchError, forkJoin, of } from 'rxjs';

type Onglet = 'don' | 'marketplace' | 'objets';

const sansEchec = <T>(defaut: T) => catchError<T, Observable<T>>(() => of(defaut));

@Component({
  selector: 'app-admin-annonces',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="page">
      <div class="page-header">
        <div>
          <h1>Annonces</h1>
          <p>Surveiller et modérer les annonces de don, marketplace et objets déclarés.</p>
        </div>
        <button class="btn secondary" type="button" (click)="charger()" [disabled]="chargement()">
          Rafraîchir
        </button>
      </div>

      <div class="card">
        <div class="actions">
          <button
            class="btn small"
            [class.secondary]="onglet() !== 'don'"
            type="button"
            (click)="onglet.set('don')"
          >
            Dons ({{ annonces().length }})
          </button>
          <button
            class="btn small"
            [class.secondary]="onglet() !== 'marketplace'"
            type="button"
            (click)="onglet.set('marketplace')"
          >
            Marketplace ({{ marketplace().length }})
          </button>
          <button
            class="btn small"
            [class.secondary]="onglet() !== 'objets'"
            type="button"
            (click)="onglet.set('objets')"
          >
            Objets ({{ objets().length }})
          </button>
        </div>
      </div>

      @if (message(); as m) {
        <div class="alert success">{{ m }}</div>
      }
      @if (erreur(); as e) {
        <div class="alert error">{{ e }}</div>
      }

      @if (chargement()) {
        <p class="muted">Chargement…</p>
      } @else if (onglet() === 'don') {
        <div class="card">
          @if (annonces().length === 0) {
            <p class="empty">Aucune annonce de don.</p>
          } @else {
            <table>
              <thead>
                <tr>
                  <th>Titre</th>
                  <th>Ville</th>
                  <th>Statut</th>
                  <th>Publiée le</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                @for (a of annonces(); track a.id) {
                  <tr>
                    <td>{{ a.titre }}</td>
                    <td>{{ a.ville || '—' }}</td>
                    <td><span class="badge">{{ a.statut }}</span></td>
                    <td>{{ formater(a.datePublication) }}</td>
                    <td>
                      <button class="btn small danger" type="button" (click)="supprimerDon(a)">
                        Supprimer
                      </button>
                    </td>
                  </tr>
                }
              </tbody>
            </table>
          }
        </div>
      } @else if (onglet() === 'marketplace') {
        <div class="card">
          @if (marketplace().length === 0) {
            <p class="empty">Aucun article marketplace.</p>
          } @else {
            <table>
              <thead>
                <tr>
                  <th>Titre</th>
                  <th>Type</th>
                  <th>Statut</th>
                  <th>Prix</th>
                  <th>Vendeur</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                @for (item of marketplace(); track item.id) {
                  <tr>
                    <td>{{ item.title }}</td>
                    <td>{{ libelleType(item.type) }}</td>
                    <td><span class="badge">{{ libelleStatut(item.status) }}</span></td>
                    <td>{{ item.price | number: '1.0-2' }} €</td>
                    <td>{{ item.sellerName || '—' }}</td>
                    <td>
                      <button
                        class="btn small danger"
                        type="button"
                        (click)="supprimerMarketplace(item)"
                      >
                        Supprimer
                      </button>
                    </td>
                  </tr>
                }
              </tbody>
            </table>
          }
        </div>
      } @else {
        <div class="card">
          @if (objets().length === 0) {
            <p class="empty">Aucun objet déclaré.</p>
          } @else {
            <table>
              <thead>
                <tr>
                  <th>Intitulé</th>
                  <th>État</th>
                  <th>Statut</th>
                  <th>Ville</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                @for (o of objets(); track o.id) {
                  <tr>
                    <td>{{ o.intitule }}</td>
                    <td>{{ o.etat }}</td>
                    <td><span class="badge">{{ o.statut }}</span></td>
                    <td>{{ o.ville || '—' }}</td>
                    <td>
                      <button class="btn small danger" type="button" (click)="supprimerObjet(o)">
                        Supprimer
                      </button>
                    </td>
                  </tr>
                }
              </tbody>
            </table>
          }
        </div>
      }
    </div>
  `,
})
export class AdminAnnonces {
  private readonly adminApi = inject(AdminApiService);

  protected readonly formater = formaterDate;

  protected readonly onglet = signal<Onglet>('don');
  protected readonly annonces = signal<AnnonceDon[]>([]);
  protected readonly marketplace = signal<MarketplaceItem[]>([]);
  protected readonly objets = signal<Objet[]>([]);
  protected readonly chargement = signal(true);
  protected readonly erreur = signal<string | null>(null);
  protected readonly message = signal<string | null>(null);

  constructor() {
    this.charger();
  }

  protected libelleStatut(statut: ItemStatus): string {
    return LIBELLES_STATUT_ITEM[statut];
  }

  protected libelleType(type: ItemType): string {
    return LIBELLES_TYPE[type];
  }

  protected charger(): void {
    this.chargement.set(true);
    this.erreur.set(null);
    this.message.set(null);

    forkJoin({
      annonces: this.adminApi.listerAnnoncesDon().pipe(sansEchec<AnnonceDon[]>([])),
      marketplace: this.adminApi.listerObjetsMarketplace().pipe(sansEchec<MarketplaceItem[]>([])),
      objets: this.adminApi.listerObjets().pipe(sansEchec<Objet[]>([])),
    }).subscribe({
      next: (r) => {
        this.annonces.set(r.annonces);
        this.marketplace.set(r.marketplace);
        this.objets.set(r.objets);
        this.chargement.set(false);
      },
      error: (err) => {
        this.erreur.set(messageErreur(err));
        this.chargement.set(false);
      },
    });
  }

  protected supprimerDon(a: AnnonceDon): void {
    if (!confirm(`Supprimer l'annonce « ${a.titre} » ?`)) {
      return;
    }
    this.adminApi.supprimerAnnonceDon(a.id).subscribe({
      next: () => {
        this.message.set(`Annonce « ${a.titre} » supprimée.`);
        this.charger();
      },
      error: (err) => this.erreur.set(messageErreur(err)),
    });
  }

  protected supprimerMarketplace(item: MarketplaceItem): void {
    if (item.id == null) {
      return;
    }
    if (!confirm(`Supprimer l'article « ${item.title} » ?`)) {
      return;
    }
    this.adminApi.supprimerObjetMarketplace(item.id).subscribe({
      next: () => {
        this.message.set(`Article « ${item.title} » supprimé.`);
        this.charger();
      },
      error: (err) => this.erreur.set(messageErreur(err)),
    });
  }

  protected supprimerObjet(o: Objet): void {
    if (!confirm(`Supprimer l'objet « ${o.intitule} » ?`)) {
      return;
    }
    this.adminApi.supprimerObjet(o.id).subscribe({
      next: () => {
        this.message.set(`Objet « ${o.intitule} » supprimé.`);
        this.charger();
      },
      error: (err) => this.erreur.set(messageErreur(err)),
    });
  }
}
