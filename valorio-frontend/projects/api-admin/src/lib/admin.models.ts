import { Acteur, UserRole } from '@valorio/shared';

/* ------------------------------------------------------------------ */
/* Utilisateurs (module 1)                                            */
/* ------------------------------------------------------------------ */

export interface UtilisateurAdmin {
  id: number;
  email: string;
  nom: string;
  prenom: string;
  nomComplet: string;
  role: UserRole;
  acteur: Acteur;
  emailVerifie: boolean;
  actif: boolean;
  ville?: string | null;
  latitude?: number | null;
  longitude?: number | null;
  scoreReputation?: number | null;
}

export interface UserStats {
  total: number;
  actifs: number;
  inactifs: number;
  verifies: number;
  parActeur: Record<string, number>;
  parRole: Record<string, number>;
}

export interface UtilisateurFiltre {
  acteur?: Acteur;
  actif?: boolean;
}

/* ------------------------------------------------------------------ */
/* Signalements (module 1, sous-module 1.3)                           */
/* ------------------------------------------------------------------ */

export type CibleSignalement =
  | 'UTILISATEUR'
  | 'OBJET'
  | 'ANNONCE_DON'
  | 'ANNONCE_MARKETPLACE'
  | 'CENTRE'
  | 'AUTRE';

export type StatutSignalement = 'OUVERT' | 'EN_COURS' | 'TRAITE' | 'REJETE';

export const CIBLES_SIGNALEMENT: readonly CibleSignalement[] = [
  'UTILISATEUR',
  'OBJET',
  'ANNONCE_DON',
  'ANNONCE_MARKETPLACE',
  'CENTRE',
  'AUTRE',
];

export const LIBELLES_CIBLE_SIGNALEMENT: Record<CibleSignalement, string> = {
  UTILISATEUR: 'Utilisateur',
  OBJET: 'Objet',
  ANNONCE_DON: 'Annonce de don',
  ANNONCE_MARKETPLACE: 'Annonce marketplace',
  CENTRE: 'Centre de recyclage',
  AUTRE: 'Autre',
};

export const STATUTS_SIGNALEMENT: readonly StatutSignalement[] = [
  'OUVERT',
  'EN_COURS',
  'TRAITE',
  'REJETE',
];

export const LIBELLES_STATUT_SIGNALEMENT: Record<StatutSignalement, string> = {
  OUVERT: 'Ouvert',
  EN_COURS: 'En cours',
  TRAITE: 'Traité',
  REJETE: 'Rejeté',
};

export const MOTIFS_SIGNALEMENT: readonly string[] = [
  'Contenu inapproprié',
  'Contenu frauduleux',
  'Objet interdit',
  'Comportement abusif',
  'Informations trompeuses',
  'Spam',
  'Autre',
];

export interface Signalement {
  id: number;
  signaleurId: number;
  cibleType: CibleSignalement;
  cibleId?: number | null;
  motif: string;
  description?: string | null;
  statut: StatutSignalement;
  dateCreation: string;
  dateTraitement?: string | null;
  traitePar?: number | null;
  noteTraitement?: string | null;
}

export interface SignalementRequest {
  cibleType: CibleSignalement;
  cibleId?: number | null;
  motif: string;
  description?: string;
}

export interface TraitementSignalementRequest {
  statut: StatutSignalement;
  note?: string;
}

export interface SignalementStats {
  total: number;
  ouverts: number;
  enCours: number;
  traites: number;
  rejetes: number;
  parCible: Record<string, number>;
}

export interface SignalementFiltre {
  statut?: StatutSignalement;
  cibleType?: CibleSignalement;
}

/* ------------------------------------------------------------------ */
/* Payloads référentiels                                              */
/* ------------------------------------------------------------------ */

export interface CategoriePayload {
  nom: string;
  description?: string;
}

export interface CentrePayload {
  organismeId?: number;
  nom: string;
  ville?: string;
  adresse?: string;
  latitude?: number;
  longitude?: number;
  materiauxAcceptes: string[];
  horaires?: string;
}
