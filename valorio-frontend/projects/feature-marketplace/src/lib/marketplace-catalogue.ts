import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import {
  Category,
  ItemCondition,
  ItemType,
  LIBELLES_CATEGORIE,
  LIBELLES_ETAT,
  LIBELLES_TYPE,
  MarketplaceApiService,
  MarketplaceItem,
  MarketplaceOffer,
  MarketplaceStats,
} from '@valorio/api-marketplace';
import { AuthService, formaterDate, messageErreur } from '@valorio/shared';

@Component({
  selector: 'valorio-marketplace-catalogue',
  imports: [CommonModule, FormsModule, RouterLink],
  template: `
    <section class="page">
      <header class="page-header">
        <div>
          <h1>Marketplace solidaire</h1>
          <p>
            Objets trouvés non réclamés revalorisés : tarification équitable assistée par IA,
            modération automatique et acquisition sécurisée.
          </p>
        </div>
        <div class="actions">
          <a class="btn" routerLink="create">Déposer une annonce (IA)</a>
          <a class="btn secondary" routerLink="ai-lab">Laboratoire IA</a>
          <a class="btn secondary" routerLink="offers">Négociations</a>
        </div>
      </header>

      <div class="grid-cards">
        <article class="tile">
          <span class="muted">Objets</span>
          <h3>{{ stats()?.totalItems ?? articles().length }}</h3>
        </article>
        <article class="tile">
          <span class="muted">Disponibles</span>
          <h3>{{ stats()?.availableItems ?? 0 }}</h3>
        </article>
        <article class="tile">
          <span class="muted">Score sécurité IA</span>
          <h3>{{ stats()?.averageSafetyScore ?? 0 }}%</h3>
        </article>
        <article class="tile">
          <span class="muted">Offres en cours</span>
          <h3>{{ stats()?.pendingOffers ?? 0 }}</h3>
        </article>
      </div>

      @if (erreur()) {
        <div class="alert error">{{ erreur() }}</div>
      }
      @if (succes()) {
        <div class="alert success">{{ succes() }}</div>
      }

      <div class="card">
        <div class="toolbar">
          <label class="field" style="flex:1">
            <span>Recherche</span>
            <input
              type="text"
              name="q"
              [(ngModel)]="recherche"
              placeholder="Objet, marque, tag..."
              (keyup.enter)="chargerArticles()"
            />
          </label>
          <button class="btn" type="button" (click)="chargerArticles()">Filtrer</button>
          @if (filtresActifs()) {
            <button class="btn secondary" type="button" (click)="reinitialiser()">Réinitialiser</button>
          }
        </div>

        <div class="chips">
          <span class="muted">Type :</span>
          <button class="chip" [class.active]="!type()" (click)="choisirType(undefined)">Tous</button>
          <button class="chip" [class.active]="type() === 'SALE'" (click)="choisirType('SALE')">Vente</button>
          <button class="chip" [class.active]="type() === 'DONATION'" (click)="choisirType('DONATION')">Dons (0 DT)</button>
          <button class="chip" [class.active]="type() === 'REWARD_BOUNTY'" (click)="choisirType('REWARD_BOUNTY')">Récompense</button>
        </div>

        <div class="chips">
          <span class="muted">Catégorie :</span>
          <button class="chip" [class.active]="!categorie()" (click)="choisirCategorie(undefined)">Toutes</button>
          @for (c of categories; track c) {
            <button class="chip" [class.active]="categorie() === c" (click)="choisirCategorie(c)">
              {{ libelleCategorie(c) }}
            </button>
          }
        </div>
      </div>

      @if (articles().length === 0) {
        <p class="empty">Aucun objet ne correspond aux filtres.</p>
      } @else {
        <div class="grid-cards">
          @for (item of articles(); track item.id) {
            <article class="tile annonce">
              <div class="spread">
                <span class="badge">{{ libelleType(item.type) }}</span>
                @if (item.aiVerificationScore != null) {
                  <span class="badge">{{ item.aiVerificationScore }}% sûr</span>
                }
              </div>
              <h3>{{ item.title }}</h3>
              <span class="muted">{{ libelleCategorie(item.category) }} · {{ libelleEtat(item.condition) }}</span>
              <div class="prix">
                @if (item.type === 'DONATION') {
                  <strong class="don">Gratuit (Don)</strong>
                } @else {
                  <strong>{{ item.price | number: '1.2-2' }} DT</strong>
                }
                <span class="badge">{{ libelleEquite(item.aiPriceFairness) }}</span>
              </div>
              @if (item.aiEstimatedPrice != null && item.type !== 'DONATION') {
                <span class="muted">Estimation IA : {{ item.aiEstimatedPrice | number: '1.0-0' }} DT</span>
              }
              <span class="muted">{{ item.location || 'Lieu non précisé' }} · {{ item.unclaimedDays }} j non réclamé</span>
              <span class="muted">{{ item.viewsCount || 0 }} vues · mis en ligne le {{ date(item.createdAt) }}</span>
              <div class="actions">
                <a class="btn secondary small" [routerLink]="['items', item.id]">Détails</a>
                <button
                  class="btn small"
                  type="button"
                  [disabled]="item.status === 'SOLD'"
                  (click)="ouvrirOffre(item)"
                >
                  {{ item.type === 'DONATION' ? 'Demander' : 'Faire une offre' }}
                </button>
              </div>
            </article>
          }
        </div>
      }

      @if (articleOffre(); as cible) {
        <div class="modal-backdrop" (click)="fermerOffre()">
          <div class="card modal" (click)="$event.stopPropagation()">
            <div class="spread">
              <h2>{{ cible.type === 'DONATION' ? 'Demande d’attribution solidaire' : 'Proposer un rachat' }}</h2>
              <button class="btn secondary small" type="button" (click)="fermerOffre()">Fermer</button>
            </div>
            <p class="muted">
              Objet : <strong>{{ cible.title }}</strong> —
              {{ cible.type === 'DONATION' ? 'Don gratuit (0 DT)' : cible.price + ' DT' }}
            </p>
            <form class="grid" (ngSubmit)="envoyerOffre()">
              <label class="field">
                <span>Nom complet *</span>
                <input type="text" name="buyerName" [(ngModel)]="offre.buyerName" required />
              </label>
              <label class="field">
                <span>Email *</span>
                <input type="email" name="buyerEmail" [(ngModel)]="offre.buyerEmail" required />
              </label>
              <label class="field">
                <span>Téléphone</span>
                <input type="text" name="buyerPhone" [(ngModel)]="offre.buyerPhone" />
              </label>
              @if (cible.type !== 'DONATION') {
                <label class="field">
                  <span>Montant proposé (DT) *</span>
                  <input type="number" step="0.5" name="offeredPrice" [(ngModel)]="offre.offeredPrice" required />
                </label>
              }
              <label class="field">
                <span>Message *</span>
                <textarea name="message" rows="3" [(ngModel)]="offre.message" required></textarea>
              </label>
              <div class="actions">
                <button class="btn" type="submit" [disabled]="envoiOffre()">
                  {{ envoiOffre() ? 'Envoi...' : 'Transmettre la proposition' }}
                </button>
                <button class="btn secondary" type="button" (click)="fermerOffre()">Annuler</button>
              </div>
            </form>
          </div>
        </div>
      }
    </section>
  `,
  styles: [
    `
      .chips {
        display: flex;
        flex-wrap: wrap;
        gap: 0.4rem;
        align-items: center;
        margin-top: 0.6rem;
      }
      .chip {
        border: 1px solid var(--border);
        background: #fff;
        color: var(--muted);
        border-radius: 999px;
        padding: 0.25rem 0.7rem;
        font-size: 0.78rem;
        font-weight: 600;
        cursor: pointer;
      }
      .chip.active {
        background: var(--primary);
        border-color: var(--primary);
        color: #fff;
      }
      .annonce h3 {
        font-size: 1rem;
        margin: 0;
      }
      .prix {
        display: flex;
        justify-content: space-between;
        align-items: center;
        gap: 0.5rem;
      }
      .prix strong {
        font-size: 1.15rem;
      }
      .don {
        color: var(--success);
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
export class MarketplaceCatalogue implements OnInit {
  private readonly api = inject(MarketplaceApiService);
  private readonly auth = inject(AuthService);

  readonly articles = signal<MarketplaceItem[]>([]);
  readonly stats = signal<MarketplaceStats | null>(null);
  readonly erreur = signal<string | null>(null);
  readonly succes = signal<string | null>(null);
  readonly envoiOffre = signal(false);
  readonly articleOffre = signal<MarketplaceItem | null>(null);

  readonly categories = Object.keys(LIBELLES_CATEGORIE) as Category[];

  readonly type = signal<ItemType | undefined>(undefined);
  readonly categorie = signal<Category | undefined>(undefined);
  recherche = '';

  offre: MarketplaceOffer = this.offreVide();

  readonly date = formaterDate;
  readonly libelleCategorie = (c: Category) => LIBELLES_CATEGORIE[c];
  readonly libelleEtat = (c: ItemCondition) => LIBELLES_ETAT[c];
  readonly libelleType = (t: ItemType) => LIBELLES_TYPE[t];

  ngOnInit(): void {
    this.chargerStats();
    this.chargerArticles();
  }

  filtresActifs(): boolean {
    return !!(this.type() || this.categorie() || this.recherche);
  }

  chargerStats(): void {
    this.api.getStats().subscribe({
      next: (stats) => this.stats.set(stats),
      error: () => undefined,
    });
  }

  chargerArticles(): void {
    this.erreur.set(null);
    this.api
      .getItems({
        category: this.categorie(),
        type: this.type(),
        search: this.recherche.trim() || undefined,
      })
      .subscribe({
        next: (articles) => this.articles.set(articles),
        error: (err) => this.erreur.set(messageErreur(err)),
      });
  }

  choisirType(type?: ItemType): void {
    this.type.set(type);
    this.chargerArticles();
  }

  choisirCategorie(categorie?: Category): void {
    this.categorie.set(categorie);
    this.chargerArticles();
  }

  reinitialiser(): void {
    this.type.set(undefined);
    this.categorie.set(undefined);
    this.recherche = '';
    this.chargerArticles();
  }

  libelleEquite(fairness?: string): string {
    if (fairness === 'EXCELLENT_DEAL') {
      return 'Affaire certifiée';
    }
    if (fairness === 'SOLIDARITY_FREE') {
      return 'Solidaire';
    }
    if (fairness === 'PREMIUM_PRICED') {
      return 'Prix premium';
    }
    return 'Prix équitable IA';
  }

  ouvrirOffre(item: MarketplaceItem): void {
    const utilisateur = this.auth.currentUser();
    const nom = this.auth.userName();
    this.offre = {
      buyerName: nom || 'Acheteur',
      buyerEmail: utilisateur?.email ?? '',
      buyerPhone: '',
      offeredPrice: item.type === 'DONATION' ? 0 : item.price,
      message:
        item.type === 'DONATION'
          ? "Bonjour, je sollicite l'attribution solidaire de cet objet."
          : `Bonjour, je suis intéressé par « ${item.title} » et vous propose ${item.price} DT.`,
    };
    this.articleOffre.set(item);
  }

  fermerOffre(): void {
    this.articleOffre.set(null);
  }

  envoyerOffre(): void {
    const item = this.articleOffre();
    if (!item?.id) {
      return;
    }
    this.envoiOffre.set(true);
    this.erreur.set(null);
    this.api.createOffer(item.id, this.offre).subscribe({
      next: () => {
        this.envoiOffre.set(false);
        this.succes.set('Votre proposition a été transmise.');
        this.fermerOffre();
        this.chargerStats();
      },
      error: (err) => {
        this.envoiOffre.set(false);
        this.erreur.set(messageErreur(err));
      },
    });
  }

  private offreVide(): MarketplaceOffer {
    return { buyerName: '', buyerEmail: '', buyerPhone: '', offeredPrice: 0, message: '' };
  }
}
