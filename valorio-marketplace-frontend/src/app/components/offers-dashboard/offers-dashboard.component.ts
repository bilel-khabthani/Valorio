import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MarketplaceService } from '../../services/marketplace.service';
import { MarketplaceOffer, OfferStatus } from '../../models/marketplace.model';

@Component({
  selector: 'app-offers-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <div class="offers-page container">
      <div class="page-header">
        <div class="badge badge-ai mb-2">
          <i class="fa-solid fa-handshake-simple"></i> Gestion des Négociations & Transactions
        </div>
        <h1>Offres d'Acquisition & Négociations</h1>
        <p>Validez les propositions de rachat et les demandes d'attribution solidaire des objets trouvés.</p>
      </div>

      <!-- Filters Row -->
      <div class="filter-row glass-card">
        <div class="filter-chips">
          <button class="chip" [class.active]="selectedStatus === 'ALL'" (click)="filterByStatus('ALL')">
            Toutes ({{ allOffers.length }})
          </button>
          <button class="chip" [class.active]="selectedStatus === 'PENDING'" (click)="filterByStatus('PENDING')">
            <i class="fa-solid fa-clock"></i> En attente ({{ getCount('PENDING') }})
          </button>
          <button class="chip" [class.active]="selectedStatus === 'ACCEPTED'" (click)="filterByStatus('ACCEPTED')">
            <i class="fa-solid fa-check"></i> Acceptées ({{ getCount('ACCEPTED') }})
          </button>
          <button class="chip" [class.active]="selectedStatus === 'COMPLETED'" (click)="filterByStatus('COMPLETED')">
            <i class="fa-solid fa-award"></i> Finalisées ({{ getCount('COMPLETED') }})
          </button>
          <button class="chip" [class.active]="selectedStatus === 'REJECTED'" (click)="filterByStatus('REJECTED')">
            <i class="fa-solid fa-xmark"></i> Refusées ({{ getCount('REJECTED') }})
          </button>
        </div>
      </div>

      <!-- Offers List -->
      <div class="offers-grid" *ngIf="filteredOffers.length > 0">
        <div class="offer-card glass-card" *ngFor="let o of filteredOffers">
          <div class="offer-top-row">
            <div class="buyer-details">
              <div class="avatar-circle">
                {{ o.buyerName ? o.buyerName.charAt(0).toUpperCase() : 'U' }}
              </div>
              <div>
                <h3 class="buyer-name">{{ o.buyerName }}</h3>
                <span class="buyer-contact">
                  <i class="fa-solid fa-envelope"></i> {{ o.buyerEmail }}
                  <span *ngIf="o.buyerPhone"> &bull; <i class="fa-solid fa-phone"></i> {{ o.buyerPhone }}</span>
                </span>
              </div>
            </div>

            <div class="status-and-price">
              <span class="offer-price-tag" *ngIf="o.item?.type !== 'DONATION'">
                {{ o.offeredPrice | number:'1.2-2' }} €
              </span>
              <span class="price-free-pill" *ngIf="o.item?.type === 'DONATION'">
                Don Solidaire (0 €)
              </span>
              <span class="badge" [ngClass]="getStatusBadgeClass(o.status)">
                {{ getStatusLabel(o.status) }}
              </span>
            </div>
          </div>

          <!-- Associated Item -->
          <div class="associated-item-row" *ngIf="o.item">
            <div class="item-thumb">
              <img [src]="o.item.imageUrl || 'https://images.unsplash.com/photo-1550009158-9ebf69173e03?w=600&auto=format&fit=crop&q=80'" alt="Miniature" />
            </div>
            <div class="item-info">
              <span class="item-ref">Annonce cible :</span>
              <a [routerLink]="['/items', o.item.id]" class="item-link">{{ o.item.title }}</a>
              <span class="item-price-ref">Prix affiché : {{ o.item.price }} €</span>
            </div>
          </div>

          <!-- Message -->
          <div class="message-box">
            <i class="fa-solid fa-quote-left quote-icon"></i>
            <p>{{ o.message }}</p>
          </div>

          <!-- Actions -->
          <div class="card-actions">
            <span class="offer-time">Reçue le {{ o.createdAt | date:'dd/MM/yyyy à HH:mm' }}</span>

            <div class="action-buttons">
              <!-- Pending actions -->
              <ng-container *ngIf="o.status === 'PENDING'">
                <button class="btn btn-secondary btn-sm" (click)="updateStatus(o.id!, 'REJECTED')">
                  <i class="fa-solid fa-xmark text-danger"></i> Refuser
                </button>
                <button class="btn btn-primary btn-sm" (click)="updateStatus(o.id!, 'ACCEPTED')">
                  <i class="fa-solid fa-check"></i> Accepter l'offre
                </button>
              </ng-container>

              <!-- Accepted actions -->
              <ng-container *ngIf="o.status === 'ACCEPTED'">
                <button class="btn btn-success btn-sm" (click)="updateStatus(o.id!, 'COMPLETED')">
                  <i class="fa-solid fa-circle-check"></i> Marquer Finalisée & Attribué
                </button>
              </ng-container>

              <!-- Completed / Rejected -->
              <ng-container *ngIf="o.status === 'COMPLETED' || o.status === 'REJECTED'">
                <button class="btn btn-secondary btn-sm" (click)="deleteOffer(o.id!)">
                  <i class="fa-regular fa-trash-can"></i> Supprimer
                </button>
              </ng-container>
            </div>
          </div>
        </div>
      </div>

      <!-- Empty State -->
      <div class="empty-state glass-card" *ngIf="filteredOffers.length === 0">
        <i class="fa-solid fa-inbox empty-icon"></i>
        <h3>Aucune offre dans cette catégorie</h3>
        <p>Les nouvelles propositions d'acheteurs et demandeurs apparaîtront ici.</p>
      </div>
    </div>
  `,
  styles: [`
    .offers-page {
      padding: 30px 0 60px;
    }
    .page-header {
      margin-bottom: 24px;
    }
    .page-header h1 {
      font-size: 2.2rem;
      margin-bottom: 6px;
    }
    .filter-row {
      padding: 16px 20px;
      margin-bottom: 24px;
    }
    .filter-chips {
      display: flex;
      gap: 10px;
      flex-wrap: wrap;
    }
    .chip {
      padding: 6px 14px;
      font-size: 0.82rem;
      font-weight: 500;
      background: rgba(255, 255, 255, 0.04);
      border: 1px solid var(--border-glass);
      color: var(--text-muted);
      border-radius: var(--radius-full);
      cursor: pointer;
      display: inline-flex;
      align-items: center;
      gap: 6px;
    }
    .chip.active {
      background: rgba(99, 102, 241, 0.2);
      border-color: var(--primary);
      color: #fff;
      font-weight: 700;
    }
    .offers-grid {
      display: grid;
      grid-template-columns: 1fr;
      gap: 20px;
    }
    .offer-card {
      padding: 24px;
    }
    .offer-top-row {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      margin-bottom: 16px;
    }
    .buyer-details {
      display: flex;
      align-items: center;
      gap: 14px;
    }
    .avatar-circle {
      width: 46px;
      height: 46px;
      border-radius: 50%;
      background: var(--primary-gradient);
      color: #fff;
      font-weight: 800;
      font-size: 1.2rem;
      display: flex;
      align-items: center;
      justify-content: center;
      box-shadow: 0 4px 10px rgba(99, 102, 241, 0.3);
    }
    .buyer-name {
      font-size: 1.15rem;
      margin-bottom: 2px;
    }
    .buyer-contact {
      font-size: 0.8rem;
      color: var(--text-muted);
    }
    .status-and-price {
      display: flex;
      align-items: center;
      gap: 12px;
    }
    .offer-price-tag {
      font-size: 1.4rem;
      font-weight: 800;
      font-family: var(--font-heading);
      color: #fff;
    }
    .price-free-pill {
      font-size: 0.95rem;
      font-weight: 700;
      color: #34d399;
    }
    .associated-item-row {
      display: flex;
      align-items: center;
      gap: 12px;
      padding: 10px 14px;
      background: rgba(255, 255, 255, 0.03);
      border-radius: var(--radius-md);
      border: 1px solid var(--border-glass);
      margin-bottom: 14px;
    }
    .item-thumb {
      width: 44px;
      height: 44px;
      border-radius: var(--radius-sm);
      overflow: hidden;
    }
    .item-thumb img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }
    .item-info {
      display: flex;
      flex-direction: column;
    }
    .item-ref {
      font-size: 0.72rem;
      color: var(--text-subtle);
    }
    .item-link {
      font-weight: 600;
      color: #fff;
      font-size: 0.9rem;
    }
    .item-link:hover { text-decoration: underline; }
    .item-price-ref {
      font-size: 0.75rem;
      color: var(--text-muted);
    }
    .message-box {
      background: rgba(0, 0, 0, 0.2);
      border-left: 3px solid var(--primary);
      padding: 12px 16px;
      border-radius: 0 var(--radius-sm) var(--radius-sm) 0;
      margin-bottom: 16px;
      font-size: 0.9rem;
      color: #e5e7eb;
      position: relative;
    }
    .quote-icon {
      color: rgba(99, 102, 241, 0.4);
      margin-right: 6px;
    }
    .card-actions {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding-top: 14px;
      border-top: 1px solid var(--border-glass);
    }
    .offer-time {
      font-size: 0.78rem;
      color: var(--text-subtle);
    }
    .action-buttons {
      display: flex;
      gap: 8px;
    }
    .empty-state {
      text-align: center;
      padding: 60px;
    }
    .empty-icon {
      font-size: 3rem;
      color: var(--text-subtle);
      margin-bottom: 16px;
    }
    @media (max-width: 768px) {
      .offer-top-row { flex-direction: column; gap: 12px; }
      .card-actions { flex-direction: column; align-items: flex-start; gap: 12px; }
    }
  `]
})
export class OffersDashboardComponent implements OnInit {
  allOffers: MarketplaceOffer[] = [];
  filteredOffers: MarketplaceOffer[] = [];
  selectedStatus: string = 'ALL';

  constructor(private marketplaceService: MarketplaceService) {}

  ngOnInit(): void {
    this.loadOffers();
  }

  loadOffers(): void {
    this.marketplaceService.getAllOffers().subscribe({
      next: (data) => {
        this.allOffers = data;
        this.applyFilter();
      },
      error: (err) => console.error('Erreur chargement offres:', err)
    });
  }

  filterByStatus(status: string): void {
    this.selectedStatus = status;
    this.applyFilter();
  }

  applyFilter(): void {
    if (this.selectedStatus === 'ALL') {
      this.filteredOffers = [...this.allOffers];
    } else {
      this.filteredOffers = this.allOffers.filter(o => o.status === this.selectedStatus);
    }
  }

  getCount(status: string): number {
    return this.allOffers.filter(o => o.status === status).length;
  }

  updateStatus(offerId: number, status: string): void {
    this.marketplaceService.updateOfferStatus(offerId, status).subscribe({
      next: () => {
        this.loadOffers();
      },
      error: (err) => console.error('Erreur mise à jour statut:', err)
    });
  }

  deleteOffer(offerId: number): void {
    if (!confirm('Supprimer cette offre ?')) return;
    this.marketplaceService.deleteOffer(offerId).subscribe({
      next: () => this.loadOffers(),
      error: (err) => console.error('Erreur suppression offre:', err)
    });
  }

  getStatusBadgeClass(status?: string): string {
    if (status === 'ACCEPTED') return 'badge-success';
    if (status === 'REJECTED') return 'badge-danger';
    if (status === 'COMPLETED') return 'badge-info';
    return 'badge-warning';
  }

  getStatusLabel(status?: string): string {
    if (status === 'ACCEPTED') return 'Acceptée';
    if (status === 'REJECTED') return 'Refusée';
    if (status === 'COMPLETED') return 'Finalisée';
    return 'En attente';
  }
}
