import { Injectable, computed, signal } from '@angular/core';
import { CentreRecyclage, Reparateur, Utilisateur, labelReparateur, labelUtilisateur } from './models';

/**
 * Acteurs metier du CDC Valorio. Chaque session simule une connexion
 * (Module 1 - Authentification n'est pas encore implemente).
 */
export type RoleActeur =
  | 'UTILISATEUR'
  | 'REPARATEUR'
  | 'DONNEUR'
  | 'BENEFICIAIRE'
  | 'ORGANISME'
  | 'ADMIN';

export const ROLES_ACTEUR: readonly RoleActeur[] = [
  'UTILISATEUR',
  'REPARATEUR',
  'DONNEUR',
  'BENEFICIAIRE',
  'ORGANISME',
  'ADMIN',
];

export const LIBELLES_ROLE: Record<RoleActeur, string> = {
  UTILISATEUR: 'Utilisateur',
  REPARATEUR: 'Réparateur',
  DONNEUR: 'Donneur',
  BENEFICIAIRE: 'Bénéficiaire',
  ORGANISME: 'Organisme de recyclage',
  ADMIN: 'Administrateur',
};

export interface IdentiteSession {
  role: RoleActeur;
  label: string;
  utilisateurId?: number;
  reparateurId?: number;
  centreId?: number;
}

export const IDENTITE_ADMIN: IdentiteSession = { role: 'ADMIN', label: 'Administrateur' };

const IDENTITE_DEFAUT: IdentiteSession = { role: 'UTILISATEUR', label: 'Utilisateur' };

const STORAGE_KEY = 'valorio.session';

export function identiteUtilisateur(utilisateur: Utilisateur, role: RoleActeur): IdentiteSession {
  return {
    role,
    label: labelUtilisateur(utilisateur),
    utilisateurId: utilisateur.id,
  };
}

export function identiteReparateur(reparateur: Reparateur): IdentiteSession {
  return {
    role: 'REPARATEUR',
    label: labelReparateur(reparateur),
    utilisateurId: reparateur.utilisateurId ?? undefined,
    reparateurId: reparateur.id,
  };
}

export function identiteCentre(centre: CentreRecyclage): IdentiteSession {
  return {
    role: 'ORGANISME',
    label: centre.nom,
    centreId: centre.id,
  };
}

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
