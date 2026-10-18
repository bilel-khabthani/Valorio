import { CommonModule } from '@angular/common';
import { Component, OnInit, computed, signal, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import {
  LIBELLES_STATUT_OFFRE,
  MarketplaceApiService,
  MarketplaceOffer,
  OfferStatus,
} from '@valorio/api-marketplace';
import { formaterDate, messageErreur } from '@valorio/shared';

@Component({
  selector: 'valorio-marketplace-offers',
  imports: [CommonModule, RouterLink],
  template: `
    <section class="page">
      <header class="page-header">
        <div>
          <a class="muted" routerLink="..">← Retour au catalogue</a>
          <h1>Négociations & acquisitions</h1>
          <p>Validez les propositions de rachat et les demandes d’attribution solidaire.</p>
        </div>
      </header>

      @if (erreur()) {
        <div class="alert error">{{ erreur() }}</div>
      }

      <div class="card actions">
        <button class="btn secondary small" [class.actif]="statut() === 'ALL'" (click)="filtrer('ALL')">
          Toutes ({{ offres().length }})
        </button>
        @for (s of statuts; track s) {
          <button class="btn secondary small" [class.actif]="statut() === s" (click)="filtrer(s)">
            {{ libelleStatut(s) }} ({{ compter(s) }})
          </button>
        }
      </div>

      @if (offresFiltrees().length === 0) {
        <p class="empty">Aucune offre dans cette catégorie.</p>
      } @else {
        <div class="grid">
          @for (offre of offresFiltrees(); track offre.id) {
            <article class="card">
              <div class="spread">
                <div>
                  <h2>{{ offre.buyerName }}</h2>
                  <span class="muted">{{ offre.buyerEmail }} @if (offre.buyerPhone) { · {{ offre.buyerPhone }} }</span>
                </div>
                <div class="spread">
                  <strong>{{ offre.offeredPrice | number: '1.2-2' }} DT</strong>
                  <span class="badge">{{ libelleStatut(offre.status) }}</span>
                </div>
              </div>

              @if (offre.item; as item) {
                <p class="muted">
                  Annonce cible :
                  <a [routerLink]="['/marketplace/items', item.id]">{{ item.title }}</a>
                  ({{ item.price | number: '1.2-2' }} DT)
                </p>
              }

              <p class="message">« {{ offre.message }} »</p>
              <span class="muted">Reçue le {{ date(offre.createdAt) }}</span>

              <div class="actions" style="margin-top:0.6rem">
                @if (offre.status === 'PENDING') {
                  <button class="btn small" (click)="changerStatut(offre.id, 'ACCEPTED')">Accepter</button>
                  <button class="btn secondary small" (click)="changerStatut(offre.id, 'REJECTED')">Refuser</button>
                }
                @if (offre.status === 'ACCEPTED') {
                  <button class="btn small" (click)="changerStatut(offre.id, 'COMPLETED')">Marquer finalisée</button>
                }
                @if (offre.status === 'COMPLETED' || offre.status === 'REJECTED') {
                  <button class="btn secondary small" (click)="supprimer(offre.id)">Supprimer</button>
                }
              </div>
            </article>
          }
        </div>
      }
    </section>
  `,
  styles: [
    `
      .actif {
        background: var(--primary);
        border-color: var(--primary);
        color: #fff;
      }
      .message {
        color: #374151;
        font-style: italic;
      }
    `,
  ],
})
export class MarketplaceOffers implements OnInit {
  private readonly api = inject(MarketplaceApiService);

  readonly offres = signal<MarketplaceOffer[]>([]);
  readonly statut = signal<'ALL' | OfferStatus>('ALL');
  readonly erreur = signal<string | null>(null);

  readonly statuts: OfferStatus[] = ['PENDING', 'ACCEPTED', 'COMPLETED', 'REJECTED'];
  readonly date = formaterDate;

  readonly offresFiltrees = computed(() => {
    const statut = this.statut();
    const offres = this.offres();
    return statut === 'ALL' ? offres : offres.filter((o) => o.status === statut);
  });

  ngOnInit(): void {
    this.charger();
  }

  libelleStatut(statut?: OfferStatus): string {
    return statut ? LIBELLES_STATUT_OFFRE[statut] : '';
  }

  compter(statut: OfferStatus): number {
    return this.offres().filter((o) => o.status === statut).length;
  }

  filtrer(statut: 'ALL' | OfferStatus): void {
    this.statut.set(statut);
  }

  charger(): void {
    this.erreur.set(null);
    this.api.getAllOffers().subscribe({
      next: (offres) => this.offres.set(offres),
      error: (err) => this.erreur.set(messageErreur(err)),
    });
  }

  changerStatut(id: number | undefined, statut: OfferStatus): void {
    if (id == null) {
      return;
    }
    this.api.updateOfferStatus(id, statut).subscribe({
      next: () => this.charger(),
      error: (err) => this.erreur.set(messageErreur(err)),
    });
  }

  supprimer(id: number | undefined): void {
    if (id == null) {
      return;
    }
    this.api.deleteOffer(id).subscribe({
      next: () => this.charger(),
      error: (err) => this.erreur.set(messageErreur(err)),
    });
  }
}
