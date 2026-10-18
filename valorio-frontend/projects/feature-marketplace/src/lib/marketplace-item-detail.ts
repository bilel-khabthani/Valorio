import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import {
  AiAssistantApiService,
  AnomalyDetectionResult,
  LIBELLES_CATEGORIE,
  LIBELLES_ETAT,
  LIBELLES_STATUT_ITEM,
  LIBELLES_STATUT_OFFRE,
  MarketplaceApiService,
  MarketplaceItem,
  MarketplaceOffer,
  OfferStatus,
} from '@valorio/api-marketplace';
import { AuthService, formaterDate, messageErreur } from '@valorio/shared';

@Component({
  selector: 'valorio-marketplace-item-detail',
  imports: [CommonModule, FormsModule, RouterLink],
  template: `
    <section class="page">
      @if (erreur()) {
        <div class="alert error">{{ erreur() }}</div>
      }

      @if (article(); as item) {
        <header class="page-header">
          <div>
            <a class="muted" routerLink="..">← Retour au catalogue</a>
            <h1>{{ item.title }}</h1>
            <p class="muted">
              {{ libelleCategorie(item.category) }} · {{ libelleEtat(item.condition) }} ·
              <span class="badge">{{ libelleStatut(item.status) }}</span>
            </p>
          </div>
        </header>

        <div class="grid grid-2">
          <article class="card">
            <h2>Annonce</h2>
            @if (item.imageUrl) {
              <img class="visuel" [src]="item.imageUrl" [alt]="item.title" />
            }
            <div class="spread">
              <strong class="prix">{{ item.type === 'DONATION' ? 'Gratuit (Don)' : (item.price | number: '1.2-2') + ' DT' }}</strong>
              <span class="badge">{{ item.type }}</span>
            </div>
            <div class="spread">
              <span class="muted">Équité prix : {{ item.aiPriceFairness || 'n/a' }}</span>
              <span class="muted">Valeur IA : {{ item.aiEstimatedPrice ?? 'n/a' }} DT</span>
            </div>
            <p class="desc">{{ item.description }}</p>
            @if (item.tags) {
              <div class="actions">
                @for (tag of tags(); track tag) {
                  <span class="badge">#{{ tag }}</span>
                }
              </div>
            }
            <ul class="meta">
              <li>Référence objet trouvé : {{ item.originalObjectId || 'n/a' }}</li>
              <li>Lieu de retrait : {{ item.location || 'n/a' }}</li>
              <li>Dépositaire : {{ item.sellerName || 'Service' }}</li>
              <li>Mise en ligne : {{ date(item.createdAt) }}</li>
            </ul>
          </article>

          <article class="card">
            <div class="spread">
              <h2>Audit sécurité & équité IA</h2>
              <button class="btn secondary small" type="button" (click)="lancerControle()">Re-scanner</button>
            </div>
            <p class="muted">Score de vérification : {{ item.aiVerificationScore ?? 98 }}%</p>
            @if (item.aiModerationNotes) {
              <div class="alert info">{{ item.aiModerationNotes }}</div>
            }
            @if (anomalie(); as anomalie) {
              <div class="alert" [class.error]="anomalie.anomalyDetected" [class.success]="!anomalie.anomalyDetected">
                <strong>{{ anomalie.anomalyDetected ? 'Anomalie détectée' : 'Contrôle validé' }} :</strong>
                {{ anomalie.diagnosticMessage }}
                @if (anomalie.recommendations.length) {
                  <ul>
                    @for (rec of anomalie.recommendations; track rec) {
                      <li>{{ rec }}</li>
                    }
                  </ul>
                }
              </div>
            }

            <div class="actions">
              <button class="btn" type="button" [disabled]="item.status === 'SOLD'" (click)="ouvrirOffre()">
                {{ item.type === 'DONATION' ? 'Solliciter l’attribution' : 'Faire une proposition' }}
              </button>
            </div>
          </article>
        </div>

        <div class="card">
          <div class="spread">
            <h2>Propositions ({{ offres().length }})</h2>
          </div>
          @if (offres().length === 0) {
            <p class="empty">Aucune proposition pour le moment.</p>
          } @else {
            <table>
              <thead>
                <tr>
                  <th>Acheteur</th>
                  <th>Montant</th>
                  <th>Message</th>
                  <th>Statut</th>
                  <th>Reçue le</th>
                </tr>
              </thead>
              <tbody>
                @for (offre of offres(); track offre.id) {
                  <tr>
                    <td>{{ offre.buyerName }}<br /><span class="muted">{{ offre.buyerEmail }}</span></td>
                    <td>{{ offre.offeredPrice | number: '1.2-2' }} DT</td>
                    <td>{{ offre.message }}</td>
                    <td><span class="badge">{{ libelleStatutOffre(offre.status) }}</span></td>
                    <td>{{ date(offre.createdAt) }}</td>
                  </tr>
                }
              </tbody>
            </table>
          }
        </div>
      } @else if (!erreur()) {
        <p class="empty">Chargement de l'annonce...</p>
      }

      @if (modaleOuverte()) {
        <div class="modal-backdrop" (click)="fermerOffre()">
          <div class="card modal" (click)="$event.stopPropagation()">
            <div class="spread">
              <h2>Proposer une acquisition</h2>
              <button class="btn secondary small" type="button" (click)="fermerOffre()">Fermer</button>
            </div>
            <form class="grid" (ngSubmit)="envoyerOffre()">
              <label class="field">
                <span>Nom complet *</span>
                <input type="text" name="buyerName" [(ngModel)]="nouvelleOffre.buyerName" required />
              </label>
              <label class="field">
                <span>Email *</span>
                <input type="email" name="buyerEmail" [(ngModel)]="nouvelleOffre.buyerEmail" required />
              </label>
              <label class="field">
                <span>Téléphone</span>
                <input type="text" name="buyerPhone" [(ngModel)]="nouvelleOffre.buyerPhone" />
              </label>
              @if (article()?.type !== 'DONATION') {
                <label class="field">
                  <span>Montant proposé (DT) *</span>
                  <input type="number" step="0.5" name="offeredPrice" [(ngModel)]="nouvelleOffre.offeredPrice" required />
                </label>
              }
              <label class="field">
                <span>Message *</span>
                <textarea name="message" rows="3" [(ngModel)]="nouvelleOffre.message" required></textarea>
              </label>
              <button class="btn" type="submit" [disabled]="envoi()">
                {{ envoi() ? 'Envoi...' : 'Transmettre l’offre' }}
              </button>
            </form>
          </div>
        </div>
      }
    </section>
  `,
  styles: [
    `
      .visuel {
        width: 100%;
        max-height: 260px;
        object-fit: cover;
        border-radius: var(--radius);
        margin-bottom: 0.75rem;
      }
      .prix {
        font-size: 1.3rem;
      }
      .desc {
        white-space: pre-line;
        color: #374151;
      }
      .meta {
        margin: 0.75rem 0 0;
        padding-left: 1.1rem;
        color: var(--muted);
        font-size: 0.88rem;
      }
      .modal-backdrop {
        position: fixed;
        inset: 0;
        background: rgba(15, 23, 42, 0.45);
        display: flex;
        align-items: center;
        justify-content: center;
        padding: 1rem;
        z-index: 50;
      }
      .modal {
        width: min(560px, 100%);
        max-height: 90vh;
        overflow-y: auto;
      }
    `,
  ],
})
export class MarketplaceItemDetail implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly api = inject(MarketplaceApiService);
  private readonly ia = inject(AiAssistantApiService);
  private readonly auth = inject(AuthService);

  readonly article = signal<MarketplaceItem | null>(null);
  readonly offres = signal<MarketplaceOffer[]>([]);
  readonly anomalie = signal<AnomalyDetectionResult | null>(null);
  readonly erreur = signal<string | null>(null);
  readonly modaleOuverte = signal(false);
  readonly envoi = signal(false);

  nouvelleOffre: MarketplaceOffer = this.offreVide();
  private itemId = 0;

  readonly date = formaterDate;
  readonly libelleCategorie = (c: MarketplaceItem['category']) => LIBELLES_CATEGORIE[c];
  readonly libelleEtat = (c: MarketplaceItem['condition']) => LIBELLES_ETAT[c];
  readonly libelleStatut = (s: MarketplaceItem['status']) => LIBELLES_STATUT_ITEM[s];
  readonly libelleStatutOffre = (s?: OfferStatus) => (s ? LIBELLES_STATUT_OFFRE[s] : '');

  ngOnInit(): void {
    this.itemId = Number(this.route.snapshot.paramMap.get('id'));
    if (this.itemId) {
      this.chargerArticle();
      this.chargerOffres();
    }
  }

  tags(): string[] {
    const brut = this.article()?.tags;
    return brut ? brut.split(',').map((t) => t.trim()).filter(Boolean) : [];
  }

  chargerArticle(): void {
    this.api.getItemById(this.itemId).subscribe({
      next: (item) => {
        this.article.set(item);
        this.nouvelleOffre.offeredPrice = item.price;
      },
      error: (err) => this.erreur.set(messageErreur(err)),
    });
  }

  chargerOffres(): void {
    this.api.getOffersByItem(this.itemId).subscribe({
      next: (offres) => this.offres.set(offres),
      error: () => undefined,
    });
  }

  lancerControle(): void {
    const item = this.article();
    if (!item) {
      return;
    }
    this.ia.detectAnomaly(item).subscribe({
      next: (res) => this.anomalie.set(res),
      error: (err) => this.erreur.set(messageErreur(err)),
    });
  }

  ouvrirOffre(): void {
    const utilisateur = this.auth.currentUser();
    const item = this.article();
    this.nouvelleOffre = {
      buyerName: this.auth.userName() || 'Acheteur',
      buyerEmail: utilisateur?.email ?? '',
      buyerPhone: '',
      offeredPrice: item?.price ?? 0,
      message:
        item?.type === 'DONATION'
          ? "Bonjour, je sollicite l'attribution solidaire de cet article."
          : `Bonjour, je vous propose ${item?.price ?? 0} DT pour cet article.`,
    };
    this.modaleOuverte.set(true);
  }

  fermerOffre(): void {
    this.modaleOuverte.set(false);
  }

  envoyerOffre(): void {
    this.envoi.set(true);
    this.api.createOffer(this.itemId, this.nouvelleOffre).subscribe({
      next: () => {
        this.envoi.set(false);
        this.fermerOffre();
        this.chargerOffres();
      },
      error: (err) => {
        this.envoi.set(false);
        this.erreur.set(messageErreur(err));
      },
    });
  }

  private offreVide(): MarketplaceOffer {
    return { buyerName: '', buyerEmail: '', buyerPhone: '', offeredPrice: 0, message: '' };
  }
}
