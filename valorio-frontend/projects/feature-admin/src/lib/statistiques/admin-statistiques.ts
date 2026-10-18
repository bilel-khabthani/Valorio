import { CommonModule } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { Observable, catchError, forkJoin, of } from 'rxjs';
import { DashboardStats, LIBELLES_ACTEUR, Acteur, messageErreur } from '@valorio/shared';
import { LIBELLES_CATEGORIE, LIBELLES_TYPE, MarketplaceStats } from '@valorio/api-marketplace';
import {
  AdminApiService,
  SignalementStats,
  SignalementsApiService,
  UserStats,
} from '@valorio/api-admin';

interface Entree {
  cle: string;
  valeur: number;
}

const sansEchec = <T>(defaut: T) => catchError<T, Observable<T>>(() => of(defaut));
const entrees = (record: Record<string, number> | undefined): Entree[] =>
  Object.entries(record ?? {})
    .map(([cle, valeur]) => ({ cle, valeur }))
    .filter((e) => e.valeur > 0)
    .sort((a, b) => b.valeur - a.valeur);

@Component({
  selector: 'app-admin-statistiques',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="page">
      <div class="page-header">
        <div>
          <h1>Statistiques</h1>
          <p>Indicateurs consolidés de la plateforme Valorio.</p>
        </div>
        <button class="btn secondary" type="button" (click)="charger()" [disabled]="chargement()">
          Rafraîchir
        </button>
      </div>

      @if (erreur(); as e) {
        <div class="alert error">{{ e }}</div>
      }

      @if (chargement()) {
        <p class="muted">Chargement…</p>
      } @else {
        <div class="grid-cards">
          <div class="tile">
            <span class="kpi">{{ utilisateurs()?.total ?? 0 }}</span>
            <span class="muted">Utilisateurs</span>
          </div>
          <div class="tile">
            <span class="kpi">{{ objets()?.totalObjets ?? 0 }}</span>
            <span class="muted">Objets déclarés</span>
          </div>
          <div class="tile">
            <span class="kpi">{{ objets()?.totalObjets ? (objets()!.totalValeurEstimee | number: '1.0-0') : 0 }}</span>
            <span class="muted">Valeur estimée (€)</span>
          </div>
          <div class="tile">
            <span class="kpi">{{ marketplace()?.totalItems ?? 0 }}</span>
            <span class="muted">Articles marketplace</span>
          </div>
          <div class="tile">
            <span class="kpi">{{ signalements()?.total ?? 0 }}</span>
            <span class="muted">Signalements</span>
          </div>
        </div>

        <div class="grid grid-2">
          <div class="card">
            <h2>Utilisateurs par acteur</h2>
            @if (repartitionActeurs().length === 0) {
              <p class="empty">Aucune donnée.</p>
            } @else {
              <ul class="barres">
                @for (e of repartitionActeurs(); track e.cle) {
                  <li>
                    <span class="barre-label">{{ libelleActeur(e.cle) }}</span>
                    <span class="barre">
                      <span class="barre-remplie" [style.width.%]="pourcent(e.valeur, utilisateurs()?.total)"></span>
                    </span>
                    <span class="barre-valeur">{{ e.valeur }}</span>
                  </li>
                }
              </ul>
            }
          </div>

          <div class="card">
            <h2>Objets par statut</h2>
            @if (repartitionStatutObjets().length === 0) {
              <p class="empty">Aucune donnée.</p>
            } @else {
              <ul class="barres">
                @for (e of repartitionStatutObjets(); track e.cle) {
                  <li>
                    <span class="barre-label">{{ e.cle }}</span>
                    <span class="barre">
                      <span class="barre-remplie" [style.width.%]="pourcent(e.valeur, objets()?.totalObjets)"></span>
                    </span>
                    <span class="barre-valeur">{{ e.valeur }}</span>
                  </li>
                }
              </ul>
            }
          </div>

          <div class="card">
            <h2>Marketplace par catégorie</h2>
            @if (repartitionCategorieMarketplace().length === 0) {
              <p class="empty">Aucune donnée.</p>
            } @else {
              <ul class="barres">
                @for (e of repartitionCategorieMarketplace(); track e.cle) {
                  <li>
                    <span class="barre-label">{{ libelleCategorie(e.cle) }}</span>
                    <span class="barre">
                      <span class="barre-remplie" [style.width.%]="pourcent(e.valeur, marketplace()?.totalItems)"></span>
                    </span>
                    <span class="barre-valeur">{{ e.valeur }}</span>
                  </li>
                }
              </ul>
            }
          </div>

          <div class="card">
            <h2>Signalements par cible</h2>
            @if (repartitionCibleSignalements().length === 0) {
              <p class="empty">Aucune donnée.</p>
            } @else {
              <ul class="barres">
                @for (e of repartitionCibleSignalements(); track e.cle) {
                  <li>
                    <span class="barre-label">{{ e.cle }}</span>
                    <span class="barre">
                      <span class="barre-remplie" [style.width.%]="pourcent(e.valeur, signalements()?.total)"></span>
                    </span>
                    <span class="barre-valeur">{{ e.valeur }}</span>
                  </li>
                }
              </ul>
            }
          </div>
        </div>
      }
    </div>
  `,
  styles: `
    .kpi {
      font-size: 1.7rem;
      font-weight: 700;
      color: var(--primary);
      line-height: 1;
    }
    .barres {
      list-style: none;
      margin: 0;
      padding: 0;
      display: flex;
      flex-direction: column;
      gap: 0.55rem;
    }
    .barres li {
      display: grid;
      grid-template-columns: 9rem 1fr 2.5rem;
      align-items: center;
      gap: 0.6rem;
      font-size: 0.85rem;
    }
    .barre {
      background: #eef2f7;
      border-radius: 999px;
      height: 0.7rem;
      overflow: hidden;
    }
    .barre-remplie {
      display: block;
      height: 100%;
      background: var(--primary);
      border-radius: 999px;
    }
    .barre-label {
      color: var(--muted);
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
    .barre-valeur {
      text-align: right;
      font-weight: 600;
    }
  `,
})
export class AdminStatistiques {
  private readonly adminApi = inject(AdminApiService);
  private readonly signalementsApi = inject(SignalementsApiService);

  protected readonly chargement = signal(true);
  protected readonly erreur = signal<string | null>(null);

  protected readonly utilisateurs = signal<UserStats | null>(null);
  protected readonly objets = signal<DashboardStats | null>(null);
  protected readonly marketplace = signal<MarketplaceStats | null>(null);
  protected readonly signalements = signal<SignalementStats | null>(null);

  constructor() {
    this.charger();
  }

  protected repartitionActeurs(): Entree[] {
    return entrees(this.utilisateurs()?.parActeur);
  }
  protected repartitionStatutObjets(): Entree[] {
    return entrees(this.objets()?.parStatut);
  }
  protected repartitionCategorieMarketplace(): Entree[] {
    return entrees(this.marketplace()?.itemsByCategory);
  }
  protected repartitionCibleSignalements(): Entree[] {
    return entrees(this.signalements()?.parCible);
  }

  protected libelleActeur(cle: string): string {
    return LIBELLES_ACTEUR[cle as Acteur] ?? cle;
  }

  protected libelleCategorie(cle: string): string {
    return LIBELLES_CATEGORIE[cle as keyof typeof LIBELLES_CATEGORIE] ?? cle;
  }

  protected libelleType(cle: string): string {
    return LIBELLES_TYPE[cle as keyof typeof LIBELLES_TYPE] ?? cle;
  }

  protected pourcent(valeur: number, total: number | undefined): number {
    if (!total) {
      return 0;
    }
    return Math.round((valeur / total) * 100);
  }

  protected charger(): void {
    this.chargement.set(true);
    this.erreur.set(null);
    forkJoin({
      utilisateurs: this.adminApi.statsUtilisateurs().pipe(sansEchec<UserStats | null>(null)),
      objets: this.adminApi.statsObjets().pipe(sansEchec<DashboardStats | null>(null)),
      marketplace: this.adminApi.statsMarketplace().pipe(sansEchec<MarketplaceStats | null>(null)),
      signalements: this.signalementsApi.stats().pipe(sansEchec<SignalementStats | null>(null)),
    }).subscribe({
      next: (r) => {
        this.utilisateurs.set(r.utilisateurs);
        this.objets.set(r.objets);
        this.marketplace.set(r.marketplace);
        this.signalements.set(r.signalements);
        this.chargement.set(false);
      },
      error: (err) => {
        this.erreur.set(messageErreur(err));
        this.chargement.set(false);
      },
    });
  }
}
