export type Category =
  | 'ELECTRONICS'
  | 'JEWELRY_WATCHES'
  | 'BAGS_LUGGAGE'
  | 'DOCUMENTS_KEYS'
  | 'CLOTHING_ACCESSORIES'
  | 'BICYCLES_VEHICLES'
  | 'SPORTS_LEISURE'
  | 'OTHER';

export type ItemCondition = 'NEW_UNOPENED' | 'EXCELLENT' | 'GOOD' | 'FAIR' | 'FOR_PARTS';

export type ItemType = 'SALE' | 'DONATION' | 'REWARD_BOUNTY';

export type ItemStatus = 'AVAILABLE' | 'RESERVED' | 'SOLD' | 'ARCHIVED';

export type OfferStatus = 'PENDING' | 'ACCEPTED' | 'REJECTED' | 'COMPLETED';

export const CATEGORIES: readonly Category[] = [
  'ELECTRONICS',
  'JEWELRY_WATCHES',
  'BAGS_LUGGAGE',
  'DOCUMENTS_KEYS',
  'CLOTHING_ACCESSORIES',
  'BICYCLES_VEHICLES',
  'SPORTS_LEISURE',
  'OTHER',
];

export const CONDITIONS: readonly ItemCondition[] = [
  'NEW_UNOPENED',
  'EXCELLENT',
  'GOOD',
  'FAIR',
  'FOR_PARTS',
];

export const TYPES: readonly ItemType[] = ['SALE', 'DONATION', 'REWARD_BOUNTY'];

export const STATUTS_ITEM: readonly ItemStatus[] = ['AVAILABLE', 'RESERVED', 'SOLD', 'ARCHIVED'];

export const LIBELLES_CATEGORIE: Record<Category, string> = {
  ELECTRONICS: 'Électronique',
  JEWELRY_WATCHES: 'Bijoux & Montres',
  BAGS_LUGGAGE: 'Sacs & Bagages',
  DOCUMENTS_KEYS: 'Documents & Clés',
  CLOTHING_ACCESSORIES: 'Vêtements',
  BICYCLES_VEHICLES: 'Vélos & Véhicules',
  SPORTS_LEISURE: 'Sports & Loisirs',
  OTHER: 'Autre',
};

export const LIBELLES_ETAT: Record<ItemCondition, string> = {
  NEW_UNOPENED: 'Neuf scellé',
  EXCELLENT: 'Comme neuf',
  GOOD: 'Bon état',
  FAIR: 'État moyen',
  FOR_PARTS: 'Pour pièces',
};

export const LIBELLES_TYPE: Record<ItemType, string> = {
  SALE: 'Vente revalorisée',
  DONATION: 'Don solidaire',
  REWARD_BOUNTY: 'Récompense',
};

export const LIBELLES_STATUT_ITEM: Record<ItemStatus, string> = {
  AVAILABLE: 'Disponible',
  RESERVED: 'Réservé',
  SOLD: 'Vendu',
  ARCHIVED: 'Archivé',
};

export const LIBELLES_STATUT_OFFRE: Record<OfferStatus, string> = {
  PENDING: 'En attente',
  ACCEPTED: 'Acceptée',
  REJECTED: 'Refusée',
  COMPLETED: 'Finalisée',
};

export interface MarketplaceItem {
  id?: number;
  title: string;
  description: string;
  category: Category;
  condition: ItemCondition;
  type: ItemType;
  status: ItemStatus;
  price: number;
  aiEstimatedPrice?: number;
  aiPriceFairness?: string;
  aiVerificationScore?: number;
  aiModerationNotes?: string;
  originalObjectId?: string;
  location?: string;
  sellerId?: number;
  sellerName?: string;
  sellerEmail?: string;
  sellerPhone?: string;
  imageUrl?: string;
  tags?: string;
  unclaimedDays?: number;
  viewsCount?: number;
  createdAt?: string;
  updatedAt?: string;
}

export interface MarketplaceOffer {
  id?: number;
  item?: MarketplaceItem;
  buyerName: string;
  buyerEmail: string;
  buyerPhone?: string;
  offeredPrice: number;
  message?: string;
  status?: OfferStatus;
  createdAt?: string;
}

export interface GenerateListingRequest {
  prompt: string;
  category?: Category;
  condition?: ItemCondition;
  type?: ItemType;
  brand?: string;
  location?: string;
}

export interface GenerateListingResponse {
  title: string;
  description: string;
  suggestedCategory: Category;
  suggestedCondition: ItemCondition;
  suggestedPrice: number;
  minPrice: number;
  maxPrice: number;
  tags: string[];
  aiSummary: string;
  confidenceScore: number;
}

export interface EstimatePriceRequest {
  title?: string;
  category?: Category;
  condition?: ItemCondition;
  originalRetailPrice?: number;
  ageInMonths?: number;
  brand?: string;
}

export interface EstimatePriceResponse {
  recommendedPrice: number;
  minFairPrice: number;
  maxFairPrice: number;
  estimatedDepreciationPercent: number;
  pricingTier: string;
  rationale: string;
  marketFactors: string[];
  confidenceScore: number;
}

export interface ModerationRequest {
  title: string;
  description: string;
  category?: Category;
  price?: string;
}

export interface ModerationResponse {
  approved: boolean;
  safetyScore: number;
  riskLevel: string;
  flaggedIssues: string[];
  aiExplanation: string;
}

export interface AnomalyDetectionResult {
  anomalyDetected: boolean;
  anomalyType: string;
  anomalyScore: number;
  expectedMin: number;
  expectedMax: number;
  actualPrice: number;
  diagnosticMessage: string;
  recommendations: string[];
}

export interface MarketplaceStats {
  totalItems: number;
  availableItems: number;
  soldItems: number;
  pendingOffers: number;
  averageSafetyScore: number;
  aiAssistedPercentage: number;
  itemsByCategory: Record<string, number>;
  itemsByType: Record<string, number>;
}
