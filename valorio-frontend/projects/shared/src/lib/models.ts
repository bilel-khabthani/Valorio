/* ------------------------------------------------------------------ */
/* Module 2 - Gestion des objets (service-objets)                     */
/* ------------------------------------------------------------------ */

export type EtatObjet = 'NEUF' | 'BON' | 'ENDOMMAGE' | 'HORS_SERVICE';

export const ETATS_OBJET: EtatObjet[] = ['NEUF', 'BON', 'ENDOMMAGE', 'HORS_SERVICE'];

export type StatutObjet =
  | 'DECLARE'
  | 'DISPONIBLE'
  | 'EN_REPARATION'
  | 'REPARE'
  | 'VENDU'
  | 'DONNE'
  | 'RECYCLE';

export const STATUTS_OBJET: StatutObjet[] = [
  'DECLARE',
  'DISPONIBLE',
  'EN_REPARATION',
  'REPARE',
  'VENDU',
  'DONNE',
  'RECYCLE',
];

export type TypeEvenement =
  | 'DECLARATION'
  | 'MODIFICATION'
  | 'PHOTO'
  | 'DIAGNOSTIC'
  | 'STATUT'
  | 'REPARATION'
  | 'TRANSACTION'
  | 'DON'
  | 'RECYCLAGE';

export interface Objet {
  id: number;
  categorieId: number;
  intitule: string;
  description?: string | null;
  etat: EtatObjet;
  ageAnnees?: number;
  valeurEstimee?: number;
  ville?: string;
  latitude?: number | null;
  longitude?: number | null;
  materiaux?: string[];
  proprietaireId?: number;
  statut?: StatutObjet;
  photos?: string[];
  dateDeclaration?: string | null;
  dateMaj?: string | null;
}

export interface Utilisateur {
  id: number;
  nom: string;
  prenom?: string;
  email?: string;
  ville: string;
  latitude?: number;
  longitude?: number;
  scoreReputation?: number;
}

export interface Categorie {
  id: number;
  nom: string;
  description?: string | null;
}

export interface EvenementHistorique {
  id: number;
  objetId: number;
  type: TypeEvenement;
  libelle: string;
  acteurId?: number | null;
  statutCible?: StatutObjet | null;
  date: string;
}

export interface DiagnosticObjet {
  id: number;
  objetId: number;
  panneDeclaree?: string | null;
  problemes?: string[];
  analyse?: string | null;
  diagnostic?: string | null;
  date: string;
}

export interface RecommandationObjet {
  action: string;
  justification?: string | null;
  coutReparationEstime?: number | null;
  valeurApresReparationEstimee?: number | null;
  economieEstimee?: number | null;
}

export interface DashboardStats {
  totalObjets: number;
  totalValeurEstimee: number;
  parStatut: Record<string, number>;
  parEtat: Record<string, number>;
  totalDiagnostics: number;
  totalEvenements: number;
}

/* ------------------------------------------------------------------ */
/* Module 4 - Réparation                                              */
/* ------------------------------------------------------------------ */

export type StatutDemande =
  | 'DEMANDE'
  | 'ACCEPTEE'
  | 'DIAGNOSTIC'
  | 'EN_REPARATION'
  | 'TERMINEE'
  | 'LIVREE';

export type StatutDevis = 'EN_ATTENTE' | 'ACCEPTE' | 'REFUSE';

export interface HistoriqueStatut {
  statut: StatutDemande;
  date: string;
  commentaire?: string;
}

export interface DemandeReparation {
  id: number;
  objetId: number;
  utilisateurId: number;
  reparateurId?: number | null;
  descriptionPanne: string;
  statut: StatutDemande;
  dateIntervention?: string | null;
  dateCreation: string;
  dateMaj: string;
  historiqueStatuts?: HistoriqueStatut[];
}

export interface Devis {
  id: number;
  demandeId: number;
  montant: number;
  delaiJours?: number | null;
  commentaire?: string | null;
  statut: StatutDevis;
  dateCreation: string;
}

export interface Reparateur {
  id: number;
  utilisateurId?: number | null;
  ville?: string;
  disponible: boolean;
  noteMoyenne: number;
  nbAvis: number;
  competences?: string;
  categories?: string;
}

export const STATUTS_DEMANDE: StatutDemande[] = [
  'DEMANDE',
  'ACCEPTEE',
  'DIAGNOSTIC',
  'EN_REPARATION',
  'TERMINEE',
  'LIVREE',
];

export function prochainStatutDemande(statut: StatutDemande): StatutDemande | null {
  const index = STATUTS_DEMANDE.indexOf(statut);
  return index >= 0 && index < STATUTS_DEMANDE.length - 1 ? STATUTS_DEMANDE[index + 1] : null;
}

/* ------------------------------------------------------------------ */
/* Module 6 - Don                                                     */
/* ------------------------------------------------------------------ */

export type StatutAnnonceDon = 'PUBLIEE' | 'RESERVEE' | 'REMISE' | 'ANNULEE';

export type StatutDemandeDon =
  | 'DEMANDE'
  | 'ACCEPTEE'
  | 'REMISE_ORGANISEE'
  | 'CONFIRMEE'
  | 'REFUSEE';

export interface AnnonceDon {
  id: number;
  objetId: number;
  donneurId: number;
  titre: string;
  description?: string | null;
  conditions?: string | null;
  ville?: string;
  latitude?: number | null;
  longitude?: number | null;
  statut: StatutAnnonceDon;
  datePublication: string;
}

export interface HistoriqueStatutDon {
  statut: StatutDemandeDon;
  date: string;
  commentaire?: string | null;
}

export interface DemandeDon {
  id: number;
  annonceId: number;
  beneficiaireId: number;
  message?: string | null;
  statut: StatutDemandeDon;
  dateDemande: string;
  dateConfirmation?: string | null;
  historique?: HistoriqueStatutDon[];
}

export interface CorrespondanceDon {
  annonce: AnnonceDon;
  score: number;
  distanceKm: number;
}

export const STATUTS_DEMANDE_DON: StatutDemandeDon[] = [
  'DEMANDE',
  'ACCEPTEE',
  'REMISE_ORGANISEE',
  'CONFIRMEE',
];

export function prochainStatutDemandeDon(statut: StatutDemandeDon): StatutDemandeDon | null {
  const index = STATUTS_DEMANDE_DON.indexOf(statut);
  return index >= 0 && index < STATUTS_DEMANDE_DON.length - 1
    ? STATUTS_DEMANDE_DON[index + 1]
    : null;
}

/* ------------------------------------------------------------------ */
/* Module 7 - Recyclage                                               */
/* ------------------------------------------------------------------ */

export type StatutCollecte = 'DEMANDE' | 'PLANIFIEE' | 'EN_COURS' | 'RECUE' | 'ANNULEE';

export type VerdictRecyclabilite = 'OUI' | 'PARTIEL' | 'NON';

export interface CentreRecyclage {
  id: number;
  organismeId?: number | null;
  nom: string;
  ville?: string;
  adresse?: string;
  latitude?: number;
  longitude?: number;
  materiauxAcceptes: string[];
  horaires?: string;
  distanceKm?: number | null;
}

export interface AnalyseRecyclabilite {
  objetId: number;
  recyclable: VerdictRecyclabilite;
  materiaux: string[];
  motifs: string[];
  centresAppropries: number[];
}

export interface HistoriqueCollecte {
  statut: StatutCollecte;
  date: string;
  commentaire?: string;
}

export interface DemandeCollecte {
  id: number;
  objetId: number;
  utilisateurId: number;
  centreId?: number | null;
  statut: StatutCollecte;
  dateDemande: string;
  datePrevue?: string | null;
  dateReception?: string | null;
  historique?: HistoriqueCollecte[];
}

export const STATUTS_COLLECTE: StatutCollecte[] = [
  'DEMANDE',
  'PLANIFIEE',
  'EN_COURS',
  'RECUE',
];

export function prochainStatutCollecte(statut: StatutCollecte): StatutCollecte | null {
  const index = STATUTS_COLLECTE.indexOf(statut);
  return index >= 0 && index < STATUTS_COLLECTE.length - 1
    ? STATUTS_COLLECTE[index + 1]
    : null;
}

/* ------------------------------------------------------------------ */
/* Référentiels (entités externes) + libellés                         */
/* ------------------------------------------------------------------ */

export function indexParId<T extends { id: number }>(items: readonly T[]): Map<number, T> {
  return new Map(items.map((item) => [item.id, item]));
}

export function labelObjet(objet: Pick<Objet, 'intitule'> | null | undefined, repliId?: number): string {
  if (objet && objet.intitule) {
    return objet.intitule;
  }
  return repliId != null ? `Objet #${repliId}` : 'Objet inconnu';
}

export function labelUtilisateur(
  utilisateur: Pick<Utilisateur, 'nom' | 'prenom'> | null | undefined,
  repliId?: number,
): string {
  if (utilisateur && utilisateur.nom) {
    const complet = `${utilisateur.prenom ? utilisateur.prenom + ' ' : ''}${utilisateur.nom}`.trim();
    return complet || utilisateur.nom;
  }
  return repliId != null ? `Utilisateur #${repliId}` : 'Utilisateur inconnu';
}

export function labelCentre(
  centre: Pick<CentreRecyclage, 'nom'> | null | undefined,
  repliId?: number,
): string {
  if (centre && centre.nom) {
    return centre.nom;
  }
  return repliId != null ? `Centre #${repliId}` : 'Centre inconnu';
}

export function labelReparateur(
  reparateur: Pick<Reparateur, 'id' | 'competences' | 'ville'> | null | undefined,
  repliId?: number,
): string {
  if (reparateur) {
    const details = [reparateur.competences, reparateur.ville].filter(Boolean).join(' · ');
    return details ? `#${reparateur.id} · ${details}` : `Réparateur #${reparateur.id}`;
  }
  return repliId != null ? `Réparateur #${repliId}` : 'Réparateur inconnu';
}

/* ------------------------------------------------------------------ */
/* Helpers                                                            */
/* ------------------------------------------------------------------ */

export function libelleStatut(statut: string): string {
  return statut
    .split('_')
    .map((mot) => mot.charAt(0).toUpperCase() + mot.slice(1).toLowerCase())
    .join(' ');
}

export function formaterDate(iso?: string | null): string {
  if (!iso) {
    return '—';
  }
  const date = new Date(iso);
  return Number.isNaN(date.getTime()) ? '—' : date.toLocaleString('fr-FR');
}
