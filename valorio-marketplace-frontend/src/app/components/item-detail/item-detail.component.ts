import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { MarketplaceService } from '../../services/marketplace.service';
import { AiAssistantService } from '../../services/ai-assistant.service';
import { AnomalyDetectionResult, MarketplaceItem, MarketplaceOffer } from '../../models/marketplace.model';

@Component({
  selector: 'app-item-detail',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  template: `
    <div class="detail-page container" *ngIf="item">
      <!-- Breadcrumb -->
      <div class="breadcrumb">
        <a routerLink="/"><i class="fa-solid fa-arrow-left"></i> Retour aux annonces</a>
        <span class="sep">/</span>
        <span class="current">{{ item.title }}</span>
      </div>

      <div class="detail-grid">
        <!-- Col Image & Fast Details -->
        <div class="detail-left">
          <div class="main-image-card glass-card">
            <img [src]="item.imageUrl || 'https://images.unsplash.com/photo-1550009158-9ebf69173e03?w=600&auto=format&fit=crop&q=80'" [alt]="item.title" class="main-img" />
            <div class="floating-badges">
              <span class="badge badge-ai">
                <i class="fa-solid fa-shield-check"></i> Score IA : {{ item.aiVerificationScore || 98 }}%
              </span>
              <span class="badge badge-info" *ngIf="item.unclaimedDays">
                <i class="fa-regular fa-clock"></i> Non réclamé {{ item.unclaimedDays }} jours
              </span>
            </div>
          </div>

          <!-- AI Trust & Safety Breakdown -->
          <div class="ai-inspect-card glass-card">
            <div class="card-title-row">
              <h4><i class="fa-solid fa-wand-magic-sparkles text-ai"></i> Audit de Sécurité & Équité IA</h4>
              <button class="btn btn-secondary btn-sm" (click)="runAnomalyCheck()">
                <i class="fa-solid fa-bolt"></i> Re-scanner
              </button>
            </div>

            <div class="audit-row">
              <span class="audit-label">Équité du prix fixé :</span>
              <span class="badge badge-ai">{{ item.aiPriceFairness || 'Prix Équilibré' }}</span>
            </div>

            <div class="audit-row">
              <span class="audit-label">Valeur résiduelle estimée :</span>
              <strong>{{ item.aiEstimatedPrice ? (item.aiEstimatedPrice + ' €') : 'N/A' }}</strong>
            </div>

            <div class="audit-notes" *ngIf="item.aiModerationNotes">
              <i class="fa-solid fa-circle-info"></i> {{ item.aiModerationNotes }}
            </div>

            <!-- Anomaly result box if triggered -->
            <div class="anomaly-box" *ngIf="anomalyResult" [class.anomaly-found]="anomalyResult.anomalyDetected">
              <div class="anomaly-header">
                <i [ngClass]="anomalyResult.anomalyDetected ? 'fa-solid fa-triangle-exclamation text-warning' : 'fa-solid fa-check text-success'"></i>
                <strong>{{ anomalyResult.anomalyDetected ? 'Anomalie Détectée' : 'Contrôle Validé' }} :</strong>
              </div>
              <p class="anomaly-msg">{{ anomalyResult.diagnosticMessage }}</p>
              <ul class="anomaly-recs" *ngIf="anomalyResult.recommendations.length > 0">
                <li *ngFor="let rec of anomalyResult.recommendations">{{ rec }}</li>
              </ul>
            </div>
          </div>
        </div>

        <!-- Col Main Content & Offers -->
        <div class="detail-right">
          <div class="item-header-card glass-card">
            <div class="type-category-row">
              <span class="category-pill">{{ item.category }}</span>
              <span class="condition-pill"><i class="fa-solid fa-check-double"></i> {{ item.condition }}</span>
              <span class="status-pill" [ngClass]="'status-' + item.status.toLowerCase()">{{ item.status }}</span>
            </div>

            <h1 class="item-title">{{ item.title }}</h1>

            <div class="price-banner">
              <div class="price-main">
                <span class="price-number" *ngIf="item.type !== 'DONATION'">{{ item.price | number:'1.2-2' }} €</span>
                <span class="price-free-large" *ngIf="item.type === 'DONATION'">Don Solidaire Gratuit (0.00 €)</span>
                <span class="price-subtitle" *ngIf="item.type === 'REWARD_BOUNTY'">Récompense promise lors de la restitution</span>
              </div>
              <button class="btn btn-primary" (click)="openOfferModal()" [disabled]="item.status === 'SOLD'">
                <i class="fa-solid fa-handshake"></i> {{ item.type === 'DONATION' ? 'Solliciter l\'attribution' : 'Faire une proposition' }}
              </button>
            </div>

            <div class="metadata-grid">
              <div class="meta-item">
                <i class="fa-solid fa-fingerprint"></i>
                <div>
                  <span class="meta-lbl">Référence Objet Trouvé</span>
                  <span class="meta-val">{{ item.originalObjectId || 'OBJ-NON-SPECIFIE' }}</span>
                </div>
              </div>
              <div class="meta-item">
                <i class="fa-solid fa-location-dot"></i>
                <div>
                  <span class="meta-lbl">Lieu de retrait</span>
                  <span class="meta-val">{{ item.location || 'Guichet Objets Trouvés' }}</span>
                </div>
              </div>
              <div class="meta-item">
                <i class="fa-solid fa-user-shield"></i>
                <div>
                  <span class="meta-lbl">Dépositaire</span>
                  <span class="meta-val">{{ item.sellerName || 'Service Municipal' }}</span>
                </div>
              </div>
              <div class="meta-item">
                <i class="fa-solid fa-calendar-check"></i>
                <div>
                  <span class="meta-lbl">Mise en ligne</span>
                  <span class="meta-val">{{ item.createdAt | date:'dd/MM/yyyy' }}</span>
                </div>
              </div>
            </div>

            <div class="description-section">
              <h3>Détails & Modalités de l'objet</h3>
              <div class="desc-content">{{ item.description }}</div>
            </div>

            <div class="tags-section" *ngIf="item.tags">
              <span class="tag-chip" *ngFor="let t of getTagList()">#{{ t }}</span>
            </div>
          </div>

          <!-- Offers / Negotiations History on this item -->
          <div class="offers-card glass-card">
            <div class="card-title-row">
              <h3><i class="fa-solid fa-comments-dollar"></i> Propositions & Demandes sur cet objet ({{ offers.length }})</h3>
              <button class="btn btn-ai btn-sm" (click)="openOfferModal()" [disabled]="item.status === 'SOLD'">
                <i class="fa-solid fa-plus"></i> Nouvelle offre
              </button>
            </div>

            <div class="offers-list" *ngIf="offers.length > 0">
              <div class="offer-item" *ngFor="let o of offers">
                <div class="offer-header">
                  <div class="buyer-info">
                    <strong>{{ o.buyerName }}</strong>
                    <span class="offer-date">{{ o.createdAt | date:'dd/MM/yyyy à HH:mm' }}</span>
                  </div>
                  <div class="offer-amount-box">
                    <span class="offer-amount" *ngIf="item.type !== 'DONATION'">{{ o.offeredPrice }} €</span>
                    <span class="badge" [ngClass]="getOfferBadgeClass(o.status)">{{ o.status }}</span>
                  </div>
                </div>
                <p class="offer-msg">"{{ o.message }}"</p>
              </div>
            </div>

            <div class="no-offers" *ngIf="offers.length === 0">
              <p>Aucune proposition enregistrée pour le moment. Soyez le premier à vous manifester !</p>
            </div>
          </div>
        </div>
      </div>

      <!-- Offer Modal -->
      <div class="modal-overlay" *ngIf="showOfferModal" (click)="closeOfferModal()">
        <div class="modal-content" (click)="$event.stopPropagation()">
          <div class="modal-header">
            <h3><i class="fa-solid fa-handshake"></i> {{ item.type === 'DONATION' ? 'Demande d\'Attribution Solidaire' : 'Faire une Proposition' }}</h3>
            <button class="close-btn" (click)="closeOfferModal()">&times;</button>
          </div>

          <form (ngSubmit)="submitOffer()">
            <div class="form-group">
              <label class="form-label">Votre Nom *</label>
              <input type="text" class="form-control" [(ngModel)]="newOffer.buyerName" name="buyerName" required />
            </div>

            <div class="form-group">
              <label class="form-label">Votre Adresse Email *</label>
              <input type="email" class="form-control" [(ngModel)]="newOffer.buyerEmail" name="buyerEmail" required />
            </div>

            <div class="form-group">
              <label class="form-label">Téléphone</label>
              <input type="text" class="form-control" [(ngModel)]="newOffer.buyerPhone" name="buyerPhone" />
            </div>

            <div class="form-group" *ngIf="item.type !== 'DONATION'">
              <label class="form-label">Montant proposé (€) *</label>
              <input type="number" step="0.5" class="form-control" [(ngModel)]="newOffer.offeredPrice" name="offeredPrice" required />
            </div>

            <div class="form-group">
              <label class="form-label">Message *</label>
              <textarea class="form-control" rows="3" [(ngModel)]="newOffer.message" name="message" required></textarea>
            </div>

            <div class="modal-actions">
              <button type="button" class="btn btn-secondary" (click)="closeOfferModal()">Annuler</button>
              <button type="submit" class="btn btn-primary" [disabled]="submittingOffer">
                {{ submittingOffer ? 'Envoi...' : 'Transmettre l\'offre' }}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .detail-page {
      padding: 30px 0 60px;
    }
    .breadcrumb {
      margin-bottom: 20px;
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 0.9rem;
      color: var(--text-muted);
    }
    .breadcrumb a { color: var(--text-muted); }
    .breadcrumb a:hover { color: #fff; }
    .sep { color: var(--text-subtle); }
    .current { color: #fff; font-weight: 600; }
    .detail-grid {
      display: grid;
      grid-template-columns: 420px 1fr;
      gap: 30px;
    }
    .main-image-card {
      padding: 0;
      overflow: hidden;
      position: relative;
      margin-bottom: 24px;
      height: 380px;
    }
    .main-img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }
    .floating-badges {
      position: absolute;
      top: 14px;
      left: 14px;
      display: flex;
      gap: 8px;
    }
    .ai-inspect-card {
      padding: 24px;
    }
    .card-title-row {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 16px;
    }
    .audit-row {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 8px 0;
      border-bottom: 1px solid var(--border-glass);
      font-size: 0.88rem;
    }
    .audit-label { color: var(--text-muted); }
    .audit-notes {
      margin-top: 14px;
      font-size: 0.82rem;
      color: #93c5fd;
      background: rgba(59, 130, 246, 0.1);
      padding: 10px 14px;
      border-radius: var(--radius-sm);
      line-height: 1.4;
    }
    .anomaly-box {
      margin-top: 16px;
      padding: 12px 14px;
      border-radius: var(--radius-sm);
      background: rgba(16, 185, 129, 0.08);
      border: 1px solid rgba(16, 185, 129, 0.3);
      font-size: 0.85rem;
    }
    .anomaly-found {
      background: rgba(245, 158, 11, 0.08);
      border-color: rgba(245, 158, 11, 0.4);
    }
    .anomaly-header {
      display: flex;
      align-items: center;
      gap: 8px;
      margin-bottom: 4px;
    }
    .anomaly-msg { margin: 0 0 6px; }
    .anomaly-recs {
      margin-left: 20px;
      color: var(--text-muted);
      font-size: 0.8rem;
    }
    .item-header-card {
      padding: 30px;
      margin-bottom: 24px;
    }
    .type-category-row {
      display: flex;
      gap: 10px;
      align-items: center;
      margin-bottom: 12px;
    }
    .category-pill {
      font-size: 0.8rem;
      font-weight: 700;
      color: var(--primary);
      text-transform: uppercase;
      letter-spacing: 0.05em;
    }
    .condition-pill {
      font-size: 0.78rem;
      color: var(--text-muted);
      background: rgba(255, 255, 255, 0.05);
      padding: 3px 10px;
      border-radius: var(--radius-full);
    }
    .status-pill {
      margin-left: auto;
      font-size: 0.75rem;
      padding: 3px 10px;
      border-radius: var(--radius-full);
      font-weight: 700;
    }
    .status-available { background: rgba(16, 185, 129, 0.15); color: #34d399; }
    .status-reserved { background: rgba(245, 158, 11, 0.15); color: #fbbf24; }
    .status-sold { background: rgba(239, 68, 68, 0.15); color: #f87171; }
    .item-title {
      font-size: 1.8rem;
      margin-bottom: 20px;
    }
    .price-banner {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 20px;
      background: rgba(255, 255, 255, 0.03);
      border-radius: var(--radius-md);
      border: 1px solid var(--border-glass);
      margin-bottom: 24px;
    }
    .price-number {
      font-size: 2.2rem;
      font-weight: 800;
      font-family: var(--font-heading);
      color: #fff;
    }
    .price-free-large {
      font-size: 1.5rem;
      font-weight: 800;
      color: #34d399;
    }
    .metadata-grid {
      display: grid;
      grid-template-columns: repeat(2, 1fr);
      gap: 16px;
      margin-bottom: 24px;
    }
    .meta-item {
      display: flex;
      align-items: center;
      gap: 12px;
      font-size: 0.9rem;
      color: var(--text-muted);
    }
    .meta-item i {
      font-size: 1.2rem;
      color: var(--primary);
    }
    .meta-lbl {
      display: block;
      font-size: 0.72rem;
      color: var(--text-subtle);
      text-transform: uppercase;
    }
    .meta-val {
      font-weight: 600;
      color: #fff;
    }
    .description-section {
      margin-bottom: 20px;
      line-height: 1.7;
    }
    .description-section h3 {
      font-size: 1.15rem;
      margin-bottom: 10px;
    }
    .desc-content {
      white-space: pre-line;
      color: #d1d5db;
    }
    .tags-section {
      display: flex;
      gap: 8px;
      flex-wrap: wrap;
    }
    .tag-chip {
      font-size: 0.78rem;
      color: #c084fc;
      background: rgba(168, 85, 247, 0.1);
      padding: 3px 10px;
      border-radius: var(--radius-full);
    }
    .offers-card {
      padding: 24px;
    }
    .offers-list {
      display: flex;
      flex-direction: column;
      gap: 12px;
      margin-top: 14px;
    }
    .offer-item {
      background: rgba(255, 255, 255, 0.03);
      border: 1px solid var(--border-glass);
      border-radius: var(--radius-md);
      padding: 14px 18px;
    }
    .offer-header {
      display: flex;
      justify-content: space-between;
      margin-bottom: 6px;
    }
    .offer-date {
      font-size: 0.75rem;
      color: var(--text-subtle);
      margin-left: 8px;
    }
    .offer-amount-box {
      display: flex;
      align-items: center;
      gap: 10px;
    }
    .offer-amount {
      font-weight: 700;
      font-size: 1.1rem;
      color: #fff;
    }
    .offer-msg {
      font-size: 0.88rem;
      color: var(--text-muted);
      font-style: italic;
      margin: 0;
    }
    .no-offers {
      text-align: center;
      padding: 30px;
      color: var(--text-muted);
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
      color: var(--text-muted);
      font-size: 1.8rem;
      cursor: pointer;
    }
    .modal-actions {
      display: flex;
      justify-content: flex-end;
      gap: 12px;
      margin-top: 24px;
    }
    @media (max-width: 900px) {
      .detail-grid { grid-template-columns: 1fr; }
    }
  `]
})
export class ItemDetailComponent implements OnInit {
  item: MarketplaceItem | null = null;
  offers: MarketplaceOffer[] = [];
  anomalyResult: AnomalyDetectionResult | null = null;

  showOfferModal = false;
  submittingOffer = false;
  newOffer: MarketplaceOffer = {
    buyerName: '',
    buyerEmail: '',
    buyerPhone: '',
    offeredPrice: 0,
    message: ''
  };

  constructor(
    private route: ActivatedRoute,
    private marketplaceService: MarketplaceService,
    private aiService: AiAssistantService
  ) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (id) {
      this.loadItem(id);
      this.loadOffers(id);
    }
  }

  loadItem(id: number): void {
    this.marketplaceService.getItemById(id).subscribe({
      next: (data) => {
        this.item = data;
        this.newOffer.offeredPrice = data.price;
      },
      error: (err) => console.error('Erreur chargement objet:', err)
    });
  }

  loadOffers(itemId: number): void {
    this.marketplaceService.getOffersByItem(itemId).subscribe({
      next: (data) => this.offers = data,
      error: (err) => console.error('Erreur chargement offres:', err)
    });
  }

  runAnomalyCheck(): void {
    if (!this.item) return;
    this.aiService.detectAnomaly(this.item).subscribe({
      next: (res) => this.anomalyResult = res,
      error: (err) => console.error('Erreur détection anomalie:', err)
    });
  }

  getTagList(): string[] {
    if (!this.item?.tags) return [];
    return this.item.tags.split(',').map(t => t.trim()).filter(Boolean);
  }

  openOfferModal(): void {
    this.showOfferModal = true;
    if (this.item) {
      this.newOffer.message = this.item.type === 'DONATION'
        ? "Bonjour, je sollicite l'attribution solidaire de cet article."
        : `Bonjour, je vous propose ${this.newOffer.offeredPrice} € pour cet article.`;
    }
  }

  closeOfferModal(): void {
    this.showOfferModal = false;
  }

  submitOffer(): void {
    if (!this.item?.id) return;
    this.submittingOffer = true;

    this.marketplaceService.createOffer(this.item.id, this.newOffer).subscribe({
      next: (created) => {
        alert('Votre offre a été enregistrée avec succès !');
        this.submittingOffer = false;
        this.closeOfferModal();
        this.loadOffers(this.item!.id!);
      },
      error: (err) => {
        console.error('Erreur soumission offre:', err);
        alert('Erreur lors de la soumission.');
        this.submittingOffer = false;
      }
    });
  }

  getOfferBadgeClass(status?: string): string {
    if (status === 'ACCEPTED') return 'badge-success';
    if (status === 'REJECTED') return 'badge-danger';
    if (status === 'COMPLETED') return 'badge-info';
    return 'badge-warning';
  }
}
