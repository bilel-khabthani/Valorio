export type RouteValorisation = 'REPARATION' | 'DON' | 'RECYCLAGE' | 'REVENTE';
export type TypeActeur = 'REPARATEUR' | 'BENEFICIAIRE' | 'CENTRE_RECYCLAGE';
export type SourcePrediction = 'ML' | 'HEURISTIQUE';
export type EquitePrix = 'EXCELLENT_DEAL' | 'FAIR_MARKET' | 'OVERPRICED' | 'UNDERPRICED';

export interface ObjetAnalyseInput {
  categorieId?: number;
  categorie?: string;
  intitule?: string;
  etat?: 'NEUF' | 'EXCELLENT' | 'BON' | 'ENDOMMAGE' | 'HORS_SERVICE';
  ageAnnees?: number;
  valeurEstimee?: number;
  coutReparationEstime?: number;
  ville?: string;
  latitude?: number;
  longitude?: number;
  materiaux?: string[];
  tauxRecyclabilite?: number;
}

export interface ValorisationRequest {
  objetId?: number;
  objet?: ObjetAnalyseInput;
}

export interface RaisonReco {
  feature?: string;
  valeur?: string;
  contribution?: number;
  libelle?: string;
}

export interface AlternativeReco {
  route: RouteValorisation;
  probabilite: number;
}

export interface RecoValorisationResponse {
  id: number;
  routeRecommandee: RouteValorisation;
  probabilite: number;
  alternatives?: AlternativeReco[];
  raisons?: RaisonReco[];
  confidenceScore?: number;
  source: SourcePrediction;
  modeleVersion?: string;
}

export interface CandidatActeur {
  id: number;
  ville?: string;
  latitude?: number;
  longitude?: number;
  noteMoyenne?: number;
  nbAvis?: number;
  disponible?: boolean;
  competences?: string;
  categories?: string;
}

export interface ActeursRequest {
  typeActeur: TypeActeur;
  objet?: ObjetAnalyseInput;
  latitude?: number;
  longitude?: number;
  rayonKm?: number;
  candidats?: CandidatActeur[];
}

export interface ActeurClasse {
  candidatId: number;
  score: number;
  probabiliteAcceptation: number;
  delaiJoursEstime?: number;
  distanceKm?: number;
  raisons?: string[];
}

export interface RecoActeursResponse {
  typeActeur: TypeActeur;
  classement?: ActeurClasse[];
  source: SourcePrediction;
  modeleVersion?: string;
}

export interface ArticleMarche {
  id?: number;
  titre?: string;
  categorie?: string;
  etat?: string;
  prix?: number;
  description?: string;
}

export interface MarcheRequest {
  requete: ArticleMarche;
  candidats?: ArticleMarche[];
  topK?: number;
}

export interface ArticleClasse {
  articleId: number;
  titre?: string;
  similarite: number;
  scoreDeal: number;
  prix?: number;
  prixRecommande?: number;
  equitePrix: EquitePrix;
  raisons?: string[];
}

export interface RecoMarcheResponse {
  articles?: ArticleClasse[];
  source: SourcePrediction;
  modeleVersion?: string;
}

export interface FeedbackRequest {
  recommandationId: number;
  utile: boolean;
  commentaire?: string;
}

export interface FeedbackResponse {
  id: number;
  recommandationId: number;
  utile: boolean;
  enregistreLe: string;
}

export interface ModeleMetadata {
  actif: boolean;
  source: SourcePrediction;
  versionValorisation?: string;
  versionActeurs?: string;
  versionMarche?: string;
  metriques?: Record<string, number>;
  topFeatures?: string[];
}

export type TypeReco = 'VALORISATION' | 'ACTEURS' | 'MARCHE';

export interface HistoriqueReco {
  id: number;
  objetId?: number;
  typeReco: TypeReco;
  chargeUtile?: string;
  resultat?: string;
  source: SourcePrediction;
  creeLe: string;
}

export interface ExplicationResponse {
  recommandationId: number;
  explication: string;
  source: SourcePrediction;
}

export const LIBELLE_ROUTE: Record<RouteValorisation, string> = {
  REPARATION: 'Réparation',
  DON: 'Don',
  RECYCLAGE: 'Recyclage',
  REVENTE: 'Revente',
};

export const LIBELLE_ACTEUR: Record<TypeActeur, string> = {
  REPARATEUR: 'Réparateur',
  BENEFICIAIRE: 'Bénéficiaire',
  CENTRE_RECYCLAGE: 'Centre de recyclage',
};

export const libelleEquite = (equite: EquitePrix): string => {
  switch (equite) {
    case 'EXCELLENT_DEAL':
      return 'Excellent deal';
    case 'FAIR_MARKET':
      return 'Prix juste';
    case 'OVERPRICED':
      return 'Surévalué';
    case 'UNDERPRICED':
      return 'Sous-évalué';
    default:
      return equite;
  }
};
