import { Component, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Observable, catchError, forkJoin, of } from 'rxjs';
import { messageErreur } from '@valorio/shared';
import {
  AdminApiService,
  SignalementStats,
  SignalementsApiService,
  UserStats,
} from '@valorio/api-admin';

interface Kpi {
  label: string;
  valeur: number;
  detail: string;
  path: string;
}

interface Donnees {
  utilisateurs: UserStats | null;
  signalements: SignalementStats | null;
  reparateurs: number;
  centres: number;
  annonces: number;
  categories: number;
}

const sansEchec = <T>(defaut: T) =>
  catchError<T, Observable<T>>(() => of(defaut));

@Component({
  selector: 'app-admin-overview',
  standalone: true,
  imports: [RouterLink],
  template: `
    <div class="page">
      <div class="page-header">
        <div>
          <h1>Vue d'ensemble</h1>
          <p>Pilotage de la plateforme Valorio · gestion, modération et statistiques.</p>
        </div>
        <button class="btn secondary" type="button" (click)="charger()" [disabled]="chargement()">
          Rafraîchir
        </button>
      </div>

      @if (erreur(); as message) {
        <div class="alert error">{{ message }}</div>
      }

      @if (chargement()) {
        <p class="muted">Chargement des indicateurs…</p>
      } @else {
        <div class="grid-cards">
          @for (kpi of indicateurs(); track kpi.path) {
            <a class="tile admin-tile" [routerLink]="kpi.path">
              <span class="tile-value">{{ kpi.valeur }}</span>
              <span class="tile-label">{{ kpi.label }}</span>
              <span class="muted">{{ kpi.detail }}</span>
            </a>
          }
        </div>

        @if (signalementsEnAttente() > 0) {
          <div class="card">
            <h2>Modération à traiter</h2>
            <p class="muted">
              {{ signalementsEnAttente() }} signalement(s) en attente de traitement.
            </p>
            <div class="actions">
              <a class="btn" routerLink="/admin/signalements">Traiter les signalements</a>
            </div>
          </div>
        }
      }
    </div>
  `,
  styles: `
    .admin-tile {
      text-decoration: none;
      color: inherit;
      transition: border-color 0.15s ease, box-shadow 0.15s ease, transform 0.15s ease;
    }
    .admin-tile:hover {
      border-color: var(--primary);
      box-shadow: 0 4px 14px rgba(37, 99, 235, 0.12);
      transform: translateY(-2px);
    }
    .tile-value {
      font-size: 1.9rem;
      font-weight: 700;
      color: var(--primary);
      line-height: 1;
    }
    .tile-label {
      font-weight: 600;
    }
  `,
})
export class AdminOverview {
  private readonly adminApi = inject(AdminApiService);
  private readonly signalementsApi = inject(SignalementsApiService);

  protected readonly chargement = signal(true);
  protected readonly erreur = signal<string | null>(null);
  private readonly donnees = signal<Donnees | null>(null);

  protected readonly indicateurs = computed<Kpi[]>(() => {
    const d = this.donnees();
    if (!d) {
      return [];
    }
    return [
      {
        label: 'Utilisateurs',
        valeur: d.utilisateurs?.total ?? 0,
        detail: `${d.utilisateurs?.actifs ?? 0} actifs`,
        path: '/admin/utilisateurs',
      },
      {
        label: 'Réparateurs',
        valeur: d.reparateurs,
        detail: 'Professionnels enregistrés',
        path: '/admin/reparateurs',
      },
      {
        label: 'Organismes',
        valeur: d.centres,
        detail: 'Centres de recyclage',
        path: '/admin/organismes',
      },
      {
        label: 'Catégories',
        valeur: d.categories,
        detail: "Référentiel d'objets",
        path: '/admin/categories',
      },
      {
        label: 'Annonces',
        valeur: d.annonces,
        detail: 'Annonces de don publiées',
        path: '/admin/annonces',
      },
      {
        label: 'Signalements',
        valeur: d.signalements?.total ?? 0,
        detail: `${d.signalements?.ouverts ?? 0} ouvert(s)`,
        path: '/admin/signalements',
      },
    ];
  });

  protected readonly signalementsEnAttente = computed(() => {
    const s = this.donnees()?.signalements;
    return s ? s.ouverts + s.enCours : 0;
  });

  constructor() {
    this.charger();
  }

  protected charger(): void {
    this.chargement.set(true);
    this.erreur.set(null);

    forkJoin({
      utilisateurs: this.adminApi.statsUtilisateurs().pipe(sansEchec<UserStats | null>(null)),
      signalements: this.signalementsApi.stats().pipe(sansEchec<SignalementStats | null>(null)),
      reparateurs: this.adminApi.listerReparateurs().pipe(sansEchec<unknown[]>([])),
      centres: this.adminApi.listerCentres().pipe(sansEchec<unknown[]>([])),
      annonces: this.adminApi.listerAnnoncesDon().pipe(sansEchec<unknown[]>([])),
      categories: this.adminApi.listerCategories().pipe(sansEchec<unknown[]>([])),
    }).subscribe({
      next: (r) => {
        this.donnees.set({
          utilisateurs: r.utilisateurs,
          signalements: r.signalements,
          reparateurs: r.reparateurs.length,
          centres: r.centres.length,
          annonces: r.annonces.length,
          categories: r.categories.length,
        });
        this.chargement.set(false);
      },
      error: (err) => {
        this.erreur.set(messageErreur(err));
        this.chargement.set(false);
      },
    });
  }
}
