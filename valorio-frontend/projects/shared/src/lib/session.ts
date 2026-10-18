import { Injectable, computed, signal } from '@angular/core';
import { ACTEURS, Acteur, LIBELLES_ACTEUR } from './auth-models';

/**
 * Acteurs metier du CDC Valorio (5 acteurs) : Utilisateur, Réparateur,
 * Acheteur, Organisme de recyclage, Administrateur. La session est alimentee
 * par l'authentification reelle (AuthService).
 */
export type RoleActeur = Acteur;

export const ROLES_ACTEUR: readonly RoleActeur[] = ACTEURS;

export const LIBELLES_ROLE: Record<RoleActeur, string> = LIBELLES_ACTEUR;

/** Page d'accueil dediee a chaque acteur (routes des espaces). */
export const ACCUEIL_PAR_ACTEUR: Record<Acteur, string> = {
  UTILISATEUR: '/utilisateur',
  REPARATEUR: '/reparateur',
  ACHETEUR: '/acheteur',
  ORGANISME: '/organisme',
  ADMIN: '/admin',
};

export function routeAccueil(acteur: Acteur | null | undefined): string {
  return ACCUEIL_PAR_ACTEUR[acteur ?? 'UTILISATEUR'] ?? '/utilisateur';
}

export interface IdentiteSession {
  role: RoleActeur;
  label: string;
  utilisateurId?: number;
  reparateurId?: number;
  centreId?: number;
}

const IDENTITE_DEFAUT: IdentiteSession = { role: 'UTILISATEUR', label: 'Utilisateur' };

const STORAGE_KEY = 'valorio.session';

@Injectable({ providedIn: 'root' })
export class SessionService {
  private readonly _identite = signal<IdentiteSession>(IDENTITE_DEFAUT);

  readonly identite = this._identite.asReadonly();
  readonly role = computed(() => this._identite().role);
  readonly label = computed(() => this._identite().label);
  readonly utilisateurId = computed(() => this._identite().utilisateurId ?? null);
  readonly reparateurId = computed(() => this._identite().reparateurId ?? null);
  readonly centreId = computed(() => this._identite().centreId ?? null);
  readonly estAdmin = computed(() => this._identite().role === 'ADMIN');

  constructor() {
    this.restaurer();
  }

  connecter(identite: IdentiteSession): void {
    this._identite.set(identite);
    this.sauvegarder();
  }

  est(...roles: RoleActeur[]): boolean {
    return roles.includes(this._identite().role);
  }

  private sauvegarder(): void {
    try {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(this._identite()));
    } catch {
      /* stockage indisponible : la session reste en memoire */
    }
  }

  private restaurer(): void {
    try {
      const brut = localStorage.getItem(STORAGE_KEY);
      if (!brut) {
        return;
      }
      const parse = JSON.parse(brut) as IdentiteSession;
      if (parse && parse.role && ROLES_ACTEUR.includes(parse.role)) {
        this._identite.set(parse);
      }
    } catch {
      /* donnees invalides : on garde la session par defaut */
    }
  }
}
