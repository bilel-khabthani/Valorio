import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { MarketplaceService } from '../../services/marketplace.service';
import { Category, ItemCondition, ItemStatus, ItemType, MarketplaceItem, MarketplaceOffer, MarketplaceStats } from '../../models/marketplace.model';

@Component({
  selector: 'app-catalog',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  template: `
    <div class="catalog-page container">
      <!-- Hero Banner -->
      <section class="hero-section glass-card">
        <div class="hero-content">
          <div class="badge badge-ai mb-2">
            <i class="fa-solid fa-sparkles"></i> Module Marketplace & Économie Circulaire Solidaire
          </div>
          <h1 class="hero-title">Objets Trouvés non réclamés & Revalorisation par <span class="highlight">Intelligence Artificielle</span></h1>
          <p class="hero-subtitle">
            Donnez une seconde vie aux objets égarés après le délai légal de garde. Tarification équitable assistée par IA, modération automatique et acquisitions sécurisées.
          </p>
          <div class="hero-actions">
            <a routerLink="/create" class="btn btn-ai">
              <i class="fa-solid fa-wand-magic-sparkles"></i> Déposer une annonce avec l'IA
            </a>
            <a routerLink="/ai-lab" class="btn btn-secondary">
              <i class="fa-solid fa-flask-vial"></i> Voir les Tests & Détection d'anomalies
            </a>
          </div>
        </div>

        <div class="hero-metrics" *ngIf="stats">
          <div class="metric-card">
            <span class="metric-val">{{ stats.totalItems }}</span>
            <span class="metric-lbl">Total Objets</span>
          </div>
          <div class="metric-card">
            <span class="metric-val text-success">{{ stats.availableItems }}</span>
            <span class="metric-lbl">Disponibles</span>
          </div>
          <div class="metric-card">
            <span class="metric-val text-ai">{{ stats.averageSafetyScore }}%</span>
            <span class="metric-lbl">Score Sécurité IA</span>
          </div>
          <div class="metric-card">
            <span class="metric-val text-warning">{{ stats.pendingOffers }}</span>
            <span class="metric-lbl">Offres en cours</span>
          </div>
        </div>
      </section>

      <!-- Filter & Search Bar -->
      <section class="search-section glass-card">
        <div class="search-bar">
          <div class="search-input-wrapper">
            <i class="fa-solid fa-magnifying-glass search-icon"></i>
            <input 
              type="text" 
              class="search-input" 
              placeholder="Rechercher par objet, marque, tags (ex: iPhone, vélo, cuir, manteau)..."
              [(ngModel)]="searchQuery" 
              (keyup.enter)="loadItems()"
            />
          </div>
          <button class="btn btn-primary" (click)="loadItems()">
            <i class="fa-solid fa-filter"></i> Filtrer
          </button>
          <button class="btn btn-secondary" (click)="resetFilters()" *ngIf="hasActiveFilters()">
            <i class="fa-solid fa-rotate-left"></i> Réinitialiser
          </button>
        </div>

        <!-- Filter Chips -->
        <div class="filter-chips-row">
          <span class="filter-label"><i class="fa-solid fa-tags"></i> Type :</span>
          <button class="chip" [class.active]="!selectedType" (click)="setType(undefined)">Tous</button>
          <button class="chip" [class.active]="selectedType === 'SALE'" (click)="setType('SALE')">Vente revalorisée</button>
          <button class="chip" [class.active]="selectedType === 'DONATION'" (click)="setType('DONATION')">Dons Solidaires (0€)</button>
          <button class="chip" [class.active]="selectedType === 'REWARD_BOUNTY'" (click)="setType('REWARD_BOUNTY')">Avis de recherche / Récompense</button>
        </div>

        <div class="filter-chips-row">
          <span class="filter-label"><i class="fa-solid fa-layer-group"></i> Catégorie :</span>
          <button class="chip" [class.active]="!selectedCategory" (click)="setCategory(undefined)">Toutes</button>
          <button class="chip" [class.active]="selectedCategory === 'ELECTRONICS'" (click)="setCategory('ELECTRONICS')">Électronique</button>
          <button class="chip" [class.active]="selectedCategory === 'JEWELRY_WATCHES'" (click)="setCategory('JEWELRY_WATCHES')">Bijoux & Montres</button>
          <button class="chip" [class.active]="selectedCategory === 'BAGS_LUGGAGE'" (click)="setCategory('BAGS_LUGGAGE')">Sacs & Valises</button>
          <button class="chip" [class.active]="selectedCategory === 'CLOTHING_ACCESSORIES'" (click)="setCategory('CLOTHING_ACCESSORIES')">Vêtements</button>
          <button class="chip" [class.active]="selectedCategory === 'BICYCLES_VEHICLES'" (click)="setCategory('BICYCLES_VEHICLES')">Vélos & Mobilité</button>
          <button class="chip" [class.active]="selectedCategory === 'DOCUMENTS_KEYS'" (click)="setCategory('DOCUMENTS_KEYS')">Clés & Documents</button>
        </div>
      </section>

      <!-- Items Grid -->
      <section class="items-grid" *ngIf="!loading && items.length > 0">
        <div class="item-card glass-card" *ngFor="let item of items">
          <div class="item-image-wrapper">
            <img [src]="item.imageUrl || getDefaultImage(item.category)" [alt]="item.title" class="item-img" />
            <div class="item-badges-floating">
              <span class="badge" [ngClass]="getTypeBadgeClass(item.type)">
                <i [ngClass]="getTypeIcon(item.type)"></i> {{ getTypeLabel(item.type) }}
              </span>
              <span class="badge badge-ai" *ngIf="item.aiVerificationScore">
                <i class="fa-solid fa-shield-check"></i> {{ item.aiVerificationScore }}% Sûr
              </span>
            </div>
            <div class="unclaimed-tag" *ngIf="item.unclaimedDays">
              <i class="fa-regular fa-clock"></i> Non réclamé {{ item.unclaimedDays }}j
            </div>
          </div>

          <div class="item-body">
            <div class="item-category-line">
              <span class="category-name">{{ getCategoryLabel(item.category) }}</span>
              <span class="condition-badge">{{ getConditionLabel(item.condition) }}</span>
            </div>

            <h3 class="item-title" [title]="item.title">{{ item.title }}</h3>

            <div class="item-pricing">
              <div class="price-box">
                <span class="price-val" *ngIf="item.type !== 'DONATION'">{{ item.price | number:'1.2-2' }} €</span>
                <span class="price-free" *ngIf="item.type === 'DONATION'">Gratuit (Don)</span>
                <span class="price-sub" *ngIf="item.aiEstimatedPrice && item.type !== 'DONATION'">
                  Estimé IA : {{ item.aiEstimatedPrice | number:'1.0-0' }} €
                </span>
              </div>
              <span class="badge" [ngClass]="getFairnessBadgeClass(item.aiPriceFairness)">
                {{ getFairnessLabel(item.aiPriceFairness) }}
              </span>
            </div>

            <div class="item-meta">
              <div class="location-line" *ngIf="item.location">
                <i class="fa-solid fa-location-dot"></i> {{ item.location }}
              </div>
              <div class="views-line">
                <i class="fa-regular fa-eye"></i> {{ item.viewsCount || 0 }} vues
              </div>
            </div>

            <div class="item-actions">
              <a [routerLink]="['/items', item.id]" class="btn btn-secondary btn-sm flex-1">
                <i class="fa-regular fa-file-lines"></i> Détails
              </a>
              <button class="btn btn-primary btn-sm flex-1" (click)="openOfferModal(item)" [disabled]="item.status === 'SOLD'">
                <i class="fa-solid fa-handshake"></i> {{ item.type === 'DONATION' ? 'Demander' : 'Faire une offre' }}
              </button>
            </div>
          </div>
        </div>
      </section>

      <!-- Empty State -->
      <div class="empty-state glass-card" *ngIf="!loading && items.length === 0">
        <i class="fa-solid fa-box-open empty-icon"></i>
        <h3>Aucun objet trouvé correspondant aux filtres</h3>
        <p>Essayez d'ajuster vos critères de recherche ou réinitialisez les filtres.</p>
        <button class="btn btn-secondary mt-3" (click)="resetFilters()">Réinitialiser les filtres</button>
      </div>

      <!-- Loading State -->
      <div class="loading-state" *ngIf="loading">
        <div class="spinner"></div>
        <p>Chargement des objets du Marketplace...</p>
      </div>

      <!-- Modal Proposition d'offre / Demande -->
      <div class="modal-overlay" *ngIf="selectedItemForOffer" (click)="closeOfferModal()">
        <div class="modal-content" (click)="$event.stopPropagation()">
          <div class="modal-header">
            <h3><i class="fa-solid fa-handshake"></i> {{ selectedItemForOffer.type === 'DONATION' ? 'Demande d\'attribution solidaire' : 'Faire une proposition de rachat' }}</h3>
            <button class="close-btn" (click)="closeOfferModal()">&times;</button>
          </div>

          <div class="modal-item-summary">
            <strong>Objet :</strong> {{ selectedItemForOffer.title }}  
            <br>
            <strong>Prix affiché :</strong> {{ selectedItemForOffer.type === 'DONATION' ? 'Don Solidaire Gratuit (0€)' : (selectedItemForOffer.price + ' €') }}
          </div>

          <form (ngSubmit)="submitOffer()">
            <div class="form-group">
              <label class="form-label">Votre nom complet *</label>
              <input type="text" class="form-control" [(ngModel)]="offerForm.buyerName" name="buyerName" required placeholder="Ex: Jean Dupont" />
            </div>

            <div class="form-group">
              <label class="form-label">Votre adresse email *</label>
              <input type="email" class="form-control" [(ngModel)]="offerForm.buyerEmail" name="buyerEmail" required placeholder="Ex: jean.dupont@email.com" />
            </div>

            <div class="form-group">
              <label class="form-label">Téléphone (optionnel)</label>
              <input type="text" class="form-control" [(ngModel)]="offerForm.buyerPhone" name="buyerPhone" placeholder="Ex: +33 6 12 34 56 78" />
            </div>

            <div class="form-group" *ngIf="selectedItemForOffer.type !== 'DONATION'">
              <label class="form-label">Montant de votre offre (€) *</label>
              <input type="number" step="0.5" class="form-control" [(ngModel)]="offerForm.offeredPrice" name="offeredPrice" required />
              <small class="text-subtle">Fourchette estimée par l'IA : {{ (selectedItemForOffer.price * 0.8) | number:'1.0-0' }} € - {{ (selectedItemForOffer.price * 1.1) | number:'1.0-0' }} €</small>
            </div>

            <div class="form-group">
              <label class="form-label">Message d'accompagnement *</label>
              <textarea class="form-control" rows="3" [(ngModel)]="offerForm.message" name="message" required placeholder="Expliquez vos disponibilités pour le retrait de l'objet..."></textarea>
            </div>

            <div class="modal-actions">
              <button type="button" class="btn btn-secondary" (click)="closeOfferModal()">Annuler</button>
              <button type="submit" class="btn btn-primary" [disabled]="submittingOffer">
                <i class="fa-solid fa-paper-plane"></i> {{ submittingOffer ? 'Envoi en cours...' : 'Envoyer la proposition' }}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .catalog-page {
      padding: 30px 0 60px;
    }
    .hero-section {
      display: grid;
      grid-template-columns: 1.6fr 1fr;
      gap: 30px;
      align-items: center;
      margin-bottom: 24px;
      padding: 36px;
      background: linear-gradient(135deg, rgba(26, 36, 64, 0.7) 0%, rgba(17, 24, 39, 0.8) 100%);
    }
    .hero-title {
      font-size: 2.2rem;
      line-height: 1.25;
      margin: 10px 0 14px;
    }
    .hero-subtitle {
      font-size: 1.05rem;
      margin-bottom: 24px;
      max-width: 600px;
    }
    .hero-actions {
      display: flex;
      gap: 14px;
      flex-wrap: wrap;
    }
    .hero-metrics {
      display: grid;
      grid-template-columns: repeat(2, 1fr);
      gap: 14px;
    }
    .metric-card {
      background: rgba(255, 255, 255, 0.03);
      border: 1px solid var(--border-glass);
      border-radius: var(--radius-md);
      padding: 16px;
      text-align: center;
      display: flex;
      flex-direction: column;
      gap: 4px;
    }
    .metric-val {
      font-size: 1.8rem;
      font-weight: 800;
      font-family: var(--font-heading);
      color: #fff;
    }
    .text-success { color: #34d399; }
    .text-ai { color: #c084fc; }
    .text-warning { color: #fbbf24; }
    .metric-lbl {
      font-size: 0.8rem;
      color: var(--text-muted);
      font-weight: 500;
    }
    .search-section {
      margin-bottom: 28px;
      padding: 20px 24px;
    }
    .search-bar {
      display: flex;
      gap: 12px;
      margin-bottom: 16px;
    }
    .search-input-wrapper {
      position: relative;
      flex: 1;
    }
    .search-icon {
      position: absolute;
      left: 16px;
      top: 50%;
      transform: translateY(-50%);
      color: var(--text-subtle);
    }
    .search-input {
      width: 100%;
      padding: 12px 16px 12px 44px;
      background: rgba(17, 24, 39, 0.9);
      border: 1px solid var(--border-glass);
      border-radius: var(--radius-md);
      color: #fff;
      font-size: 0.95rem;
      outline: none;
      transition: all var(--transition-fast);
    }
    .search-input:focus {
      border-color: var(--primary);
      box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.2);
    }
    .filter-chips-row {
      display: flex;
      align-items: center;
      gap: 8px;
      flex-wrap: wrap;
      margin-top: 10px;
    }
    .filter-label {
      font-size: 0.8rem;
      font-weight: 600;
      color: var(--text-muted);
      margin-right: 4px;
    }
    .chip {
      padding: 5px 12px;
      font-size: 0.8rem;
      font-weight: 500;
      background: rgba(255, 255, 255, 0.04);
      color: var(--text-muted);
      border: 1px solid var(--border-glass);
      border-radius: var(--radius-full);
      cursor: pointer;
      transition: all var(--transition-fast);
    }
    .chip:hover {
      background: rgba(255, 255, 255, 0.08);
      color: #fff;
    }
    .chip.active {
      background: rgba(99, 102, 241, 0.2);
      border-color: var(--primary);
      color: #fff;
      font-weight: 600;
    }
    .items-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
      gap: 24px;
    }
    .item-card {
      padding: 0;
      overflow: hidden;
      display: flex;
      flex-direction: column;
      border-radius: var(--radius-lg);
    }
    .item-image-wrapper {
      position: relative;
      height: 200px;
      overflow: hidden;
      background: #1f2937;
    }
    .item-img {
      width: 100%;
      height: 100%;
      object-fit: cover;
      transition: transform 0.4s ease;
    }
    .item-card:hover .item-img {
      transform: scale(1.05);
    }
    .item-badges-floating {
      position: absolute;
      top: 12px;
      left: 12px;
      display: flex;
      gap: 6px;
      flex-wrap: wrap;
    }
    .unclaimed-tag {
      position: absolute;
      bottom: 10px;
      right: 12px;
      background: rgba(0, 0, 0, 0.65);
      backdrop-filter: blur(8px);
      padding: 3px 8px;
      border-radius: var(--radius-sm);
      font-size: 0.72rem;
      color: #e5e7eb;
    }
    .item-body {
      padding: 20px;
      display: flex;
      flex-direction: column;
      flex: 1;
    }
    .item-category-line {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 8px;
    }
    .category-name {
      font-size: 0.78rem;
      font-weight: 600;
      color: var(--primary);
      text-transform: uppercase;
      letter-spacing: 0.03em;
    }
    .condition-badge {
      font-size: 0.72rem;
      color: var(--text-subtle);
      background: rgba(255, 255, 255, 0.05);
      padding: 2px 8px;
      border-radius: 4px;
    }
    .item-title {
      font-size: 1.1rem;
      font-weight: 700;
      line-height: 1.35;
      margin-bottom: 14px;
      height: 44px;
      overflow: hidden;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
    }
    .item-pricing {
      display: flex;
      justify-content: space-between;
      align-items: flex-end;
      padding: 12px 0;
      border-top: 1px solid var(--border-glass);
      border-bottom: 1px solid var(--border-glass);
      margin-bottom: 14px;
    }
    .price-box {
      display: flex;
      flex-direction: column;
    }
    .price-val {
      font-size: 1.4rem;
      font-weight: 800;
      font-family: var(--font-heading);
      color: #fff;
    }
    .price-free {
      font-size: 1.15rem;
      font-weight: 700;
      color: #34d399;
    }
    .price-sub {
      font-size: 0.75rem;
      color: var(--text-subtle);
    }
    .item-meta {
      display: flex;
      justify-content: space-between;
      font-size: 0.78rem;
      color: var(--text-muted);
      margin-bottom: 16px;
    }
    .item-actions {
      display: flex;
      gap: 10px;
      margin-top: auto;
    }
    .flex-1 { flex: 1; }
    .empty-state {
      text-align: center;
      padding: 60px 20px;
    }
    .empty-icon {
      font-size: 3rem;
      color: var(--text-subtle);
      margin-bottom: 16px;
    }
    .loading-state {
      text-align: center;
      padding: 80px 20px;
    }
    .spinner {
      width: 44px;
      height: 44px;
      border: 3px solid rgba(255, 255, 255, 0.1);
      border-radius: 50%;
      border-top-color: var(--primary);
      animation: spin 0.8s linear infinite;
      margin: 0 auto 16px;
    }
    @keyframes spin {
      to { transform: rotate(360deg); }
    }
    .modal-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 20px;
    }
    .close-btn {
      background: none;
      border: none;
      font-size: 1.8rem;
      color: var(--text-muted);
      cursor: pointer;
    }
    .modal-item-summary {
      background: rgba(255, 255, 255, 0.04);
      padding: 12px 16px;
      border-radius: var(--radius-sm);
      margin-bottom: 20px;
      font-size: 0.9rem;
      line-height: 1.5;
    }
    .modal-actions {
      display: flex;
      justify-content: flex-end;
      gap: 12px;
      margin-top: 24px;
    }
    @media (max-width: 900px) {
      .hero-section { grid-template-columns: 1fr; }
    }
  `]
})
export class CatalogComponent implements OnInit {
  items: MarketplaceItem[] = [];
  stats: MarketplaceStats | null = null;
  loading = false;
  searchQuery = '';
  selectedCategory?: Category;
  selectedType?: ItemType;

  selectedItemForOffer: MarketplaceItem | null = null;
  submittingOffer = false;
  offerForm: {
    buyerName: string;
    buyerEmail: string;
    buyerPhone: string;
    offeredPrice: number;
    message: string;
  } = {
    buyerName: '',
    buyerEmail: '',
    buyerPhone: '',
    offeredPrice: 0,
    message: ''
  };

  constructor(private marketplaceService: MarketplaceService) {}

  ngOnInit(): void {
    this.loadStats();
    this.loadItems();
  }

  loadStats(): void {
    this.marketplaceService.getStats().subscribe({
      next: (data) => this.stats = data,
      error: (err) => console.error('Erreur chargement statistiques:', err)
    });
  }

  loadItems(): void {
    this.loading = true;
    this.marketplaceService.getItems({
      category: this.selectedCategory,
      type: this.selectedType,
      search: this.searchQuery.trim() || undefined
    }).subscribe({
      next: (data) => {
        this.items = data;
        this.loading = false;
      },
      error: (err) => {
        console.error('Erreur chargement objets:', err);
        this.loading = false;
      }
    });
  }

  setType(type?: ItemType): void {
    this.selectedType = type;
    this.loadItems();
  }

  setCategory(cat?: Category): void {
    this.selectedCategory = cat;
    this.loadItems();
  }

  hasActiveFilters(): boolean {
    return !!(this.selectedCategory || this.selectedType || this.searchQuery);
  }

  resetFilters(): void {
    this.selectedCategory = undefined;
    this.selectedType = undefined;
    this.searchQuery = '';
    this.loadItems();
  }

  openOfferModal(item: MarketplaceItem): void {
    this.selectedItemForOffer = item;
    this.offerForm = {
      buyerName: '',
      buyerEmail: '',
      buyerPhone: '',
      offeredPrice: item.type === 'DONATION' ? 0 : item.price,
      message: item.type === 'DONATION'
        ? "Bonjour, je sollicite l'attribution solidaire de cet objet et m'engage à le récupérer selon vos consignes."
        : `Bonjour, je suis intéressé par votre annonce pour ${item.title} et vous propose ${item.price} €.`
    };
  }

  closeOfferModal(): void {
    this.selectedItemForOffer = null;
  }

  submitOffer(): void {
    if (!this.selectedItemForOffer?.id) return;
    this.submittingOffer = true;

    const offer: MarketplaceOffer = {
      buyerName: this.offerForm.buyerName,
      buyerEmail: this.offerForm.buyerEmail,
      buyerPhone: this.offerForm.buyerPhone,
      offeredPrice: this.offerForm.offeredPrice,
      message: this.offerForm.message
    };

    this.marketplaceService.createOffer(this.selectedItemForOffer.id, offer).subscribe({
      next: () => {
        alert('Votre proposition a été transmise avec succès ! Elle est maintenant visible dans l\'onglet Négociations.');
        this.submittingOffer = false;
        this.closeOfferModal();
        this.loadStats();
      },
      error: (err) => {
        console.error('Erreur soumission offre:', err);
        alert('Erreur lors de l\'envoi de votre offre.');
        this.submittingOffer = false;
      }
    });
  }

  // Label helpers
  getCategoryLabel(c: Category): string {
    const map: Record<Category, string> = {
      ELECTRONICS: 'Électronique',
      JEWELRY_WATCHES: 'Bijoux & Montres',
      BAGS_LUGGAGE: 'Sacs & Bagages',
      DOCUMENTS_KEYS: 'Documents & Clés',
      CLOTHING_ACCESSORIES: 'Vêtements',
      BICYCLES_VEHICLES: 'Vélos & Véhicules',
      SPORTS_LEISURE: 'Sports & Loisirs',
      OTHER: 'Autre'
    };
    return map[c] || c;
  }

  getConditionLabel(cond: ItemCondition): string {
    const map: Record<ItemCondition, string> = {
      NEW_UNOPENED: 'Neuf scellé',
      EXCELLENT: 'Comme neuf',
      GOOD: 'Bon état',
      FAIR: 'État moyen',
      FOR_PARTS: 'Pour pièces'
    };
    return map[cond] || cond;
  }

  getTypeLabel(t: ItemType): string {
    const map: Record<ItemType, string> = {
      SALE: 'Vente revalorisée',
      DONATION: 'Don solidaire',
      REWARD_BOUNTY: 'Récompense'
    };
    return map[t] || t;
  }

  getTypeBadgeClass(t: ItemType): string {
    if (t === 'DONATION') return 'badge-success';
    if (t === 'REWARD_BOUNTY') return 'badge-warning';
    return 'badge-info';
  }

  getTypeIcon(t: ItemType): string {
    if (t === 'DONATION') return 'fa-solid fa-gift';
    if (t === 'REWARD_BOUNTY') return 'fa-solid fa-award';
    return 'fa-solid fa-tag';
  }

  getFairnessBadgeClass(fairness?: string): string {
    if (fairness === 'EXCELLENT_DEAL') return 'badge-success';
    if (fairness === 'SOLIDARITY_FREE') return 'badge-success';
    if (fairness === 'REWARD_OFFER') return 'badge-warning';
    return 'badge-ai';
  }

  getFairnessLabel(fairness?: string): string {
    if (fairness === 'EXCELLENT_DEAL') return 'Affaire certifiée';
    if (fairness === 'SOLIDARITY_FREE') return 'Solidaire (0€)';
    if (fairness === 'REWARD_OFFER') return 'Récompense';
    return 'Prix équitable IA';
  }

  getDefaultImage(c: Category): string {
    const map: Record<Category, string> = {
      ELECTRONICS: 'https://images.unsplash.com/photo-1550009158-9ebf69173e03?w=600&auto=format&fit=crop&q=80',
      JEWELRY_WATCHES: 'https://images.unsplash.com/photo-1524805444758-089113d48a6d?w=600&auto=format&fit=crop&q=80',
      BAGS_LUGGAGE: 'https://images.unsplash.com/photo-1553062407-98eeb64c6a62?w=600&auto=format&fit=crop&q=80',
      DOCUMENTS_KEYS: 'https://images.unsplash.com/photo-1582139329536-e7284fece509?w=600&auto=format&fit=crop&q=80',
      CLOTHING_ACCESSORIES: 'https://images.unsplash.com/photo-1539533018447-63fcce667823?w=600&auto=format&fit=crop&q=80',
      BICYCLES_VEHICLES: 'https://images.unsplash.com/photo-1485965120184-e220f721d03e?w=600&auto=format&fit=crop&q=80',
      SPORTS_LEISURE: 'https://images.unsplash.com/photo-1510915361894-db8b60106cb1?w=600&auto=format&fit=crop&q=80',
      OTHER: 'https://images.unsplash.com/photo-1586769852044-692d6e3703f0?w=600&auto=format&fit=crop&q=80'
    };
    return map[c] || map.OTHER;
  }
}
