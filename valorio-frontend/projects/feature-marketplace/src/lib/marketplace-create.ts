import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import {
  AiAssistantApiService,
  CATEGORIES,
  CONDITIONS,
  Category,
  ItemCondition,
  ItemType,
  LIBELLES_CATEGORIE,
  LIBELLES_ETAT,
  LIBELLES_TYPE,
  MarketplaceApiService,
  MarketplaceItem,
  ModerationResponse,
  TYPES,
} from '@valorio/api-marketplace';
import { AuthService, messageErreur } from '@valorio/shared';

@Component({
  selector: 'valorio-marketplace-create',
  imports: [FormsModule, RouterLink],
  template: `
    <section class="page">
      <header class="page-header">
        <div>
          <a class="muted" routerLink="..">← Retour au catalogue</a>
          <h1>Publier une annonce</h1>
          <p>Assistant IA de rédaction, tarification et modération.</p>
        </div>
      </header>

      @if (erreur()) {
        <div class="alert error">{{ erreur() }}</div>
      }

      <div class="card ai">
        <div class="spread">
          <h2>Assistant IA de rédaction & tarification</h2>
          <span class="badge">Moteur heuristique v2.4</span>
        </div>
        <div class="toolbar">
          <label class="field" style="flex:1">
            <span>Description brute / mots-clés de l'objet trouvé</span>
            <input
              type="text"
              name="prompt"
              [(ngModel)]="promptIa"
              placeholder="Ex: iPad Air gris 64go avec étui cuir, écran intact"
              (keyup.enter)="genererAvecIa()"
            />
          </label>
          <button class="btn" type="button" [disabled]="generationIa() || !promptIa.trim()" (click)="genererAvecIa()">
            {{ generationIa() ? 'Génération...' : 'Générer avec l’IA' }}
          </button>
        </div>
        <div class="actions">
          @for (exemple of exemples; track exemple.label) {
            <button class="btn secondary small" type="button" (click)="promptIa = exemple.texte">
              {{ exemple.label }}
            </button>
          }
        </div>
        @if (resumeIa()) {
          <div class="alert success">{{ resumeIa() }}</div>
        }
      </div>

      <form class="card" (ngSubmit)="publier()">
        <div class="grid grid-2">
          <div class="grid">
            <label class="field">
              <span>Titre de l'annonce *</span>
              <input type="text" name="title" [(ngModel)]="item.title" required (input)="analyser()" />
            </label>
            <div class="grid grid-2">
              <label class="field">
                <span>Catégorie *</span>
                <select name="category" [(ngModel)]="item.category" (change)="analyser()">
                  @for (c of categories; track c) {
                    <option [ngValue]="c">{{ libelleCategorie(c) }}</option>
                  }
                </select>
              </label>
              <label class="field">
                <span>État constaté *</span>
                <select name="condition" [(ngModel)]="item.condition" (change)="analyser()">
                  @for (c of conditions; track c) {
                    <option [ngValue]="c">{{ libelleEtat(c) }}</option>
                  }
                </select>
              </label>
            </div>
            <div class="grid grid-2">
              <label class="field">
                <span>Type de publication *</span>
                <select name="type" [(ngModel)]="item.type" (change)="onTypeChange()">
                  @for (t of types; track t) {
                    <option [ngValue]="t">{{ libelleType(t) }}</option>
                  }
                </select>
              </label>
              <div class="field">
                <span>
                  Prix (DT) *
                  <button class="lien-ia" type="button" (click)="estimerPrix()" [disabled]="item.type === 'DONATION'">
                    Estimer (IA)
                  </button>
                </span>
                <input
                  type="number"
                  step="0.5"
                  name="price"
                  [(ngModel)]="item.price"
                  [disabled]="item.type === 'DONATION'"
                  (input)="analyser()"
                />
              </div>
            </div>
            <label class="field">
              <span>Description détaillée *</span>
              <textarea name="description" rows="8" [(ngModel)]="item.description" required (input)="analyser()"></textarea>
            </label>
          </div>

          <div class="grid">
            <label class="field">
              <span>URL de la photo</span>
              <input type="text" name="imageUrl" [(ngModel)]="item.imageUrl" placeholder="https://..." />
            </label>
            <div class="grid grid-2">
              <label class="field">
                <span>ID objet trouvé d'origine</span>
                <input type="text" name="originalObjectId" [(ngModel)]="item.originalObjectId" placeholder="Ex: 12" />
              </label>
              <label class="field">
                <span>Lieu de retrait</span>
                <input type="text" name="location" [(ngModel)]="item.location" />
              </label>
            </div>
            <label class="field">
              <span>Tags (séparés par des virgules)</span>
              <input type="text" name="tags" [(ngModel)]="item.tags" />
            </label>

            <div
              class="card"
              [class.moniteur-ok]="!moderation() || moderation()?.approved"
              [class.moniteur-ko]="moderation() && !moderation()?.approved"
            >
              <div class="spread">
                <strong>Contrôle de conformité IA</strong>
                <span>{{ moderation()?.safetyScore ?? 98 }}%</span>
              </div>
              <p class="muted">
                {{ moderation()?.aiExplanation || 'Analyse continue des textes contre les contrefaçons et objets illégaux.' }}
              </p>
              @for (issue of moderation()?.flaggedIssues ?? []; track issue) {
                <div class="alert error">{{ issue }}</div>
              }
            </div>
          </div>
        </div>

        <div class="actions" style="justify-content:flex-end; margin-top:1rem">
          <a class="btn secondary" routerLink="..">Annuler</a>
          <button class="btn" type="submit" [disabled]="publication() || (moderation() != null && !moderation()!.approved)">
            {{ publication() ? 'Publication...' : 'Publier l’annonce' }}
          </button>
        </div>
      </form>
    </section>
  `,
  styles: [
    `
      .ai {
        border-color: rgba(139, 92, 246, 0.35);
        background: linear-gradient(135deg, #faf5ff 0%, #ffffff 55%);
      }
      .lien-ia {
        border: none;
        background: none;
        color: var(--ai-dark);
        font-size: 0.75rem;
        font-weight: 700;
        cursor: pointer;
        margin-left: 0.35rem;
      }
      .moniteur-ok {
        border-color: rgba(22, 163, 74, 0.4);
        background: #f0fdf4;
      }
      .moniteur-ko {
        border-color: rgba(220, 38, 38, 0.4);
        background: #fef2f2;
      }
    `,
  ],
})
export class MarketplaceCreate {
  private readonly api = inject(MarketplaceApiService);
  private readonly ia = inject(AiAssistantApiService);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  readonly categories = CATEGORIES;
  readonly conditions = CONDITIONS;
  readonly types = TYPES;

  readonly erreur = signal<string | null>(null);
  readonly resumeIa = signal<string | null>(null);
  readonly generationIa = signal(false);
  readonly publication = signal(false);
  readonly moderation = signal<ModerationResponse | null>(null);

  promptIa = '';
  item: MarketplaceItem = this.itemVide();

  readonly exemples = [
    { label: 'Trottinette Xiaomi', texte: 'Trottinette électrique Xiaomi Mi Pro 2, chargeur inclus, état moyen' },
    { label: 'Guitare Yamaha', texte: 'Guitare acoustique Yamaha F310 oubliée en salle de musique, très bon état' },
    { label: 'Manteau don', texte: 'Manteau d’hiver imperméable bleu taille M pour don solidaire étudiant' },
  ];

  readonly libelleCategorie = (c: Category) => LIBELLES_CATEGORIE[c];
  readonly libelleEtat = (c: ItemCondition) => LIBELLES_ETAT[c];
  readonly libelleType = (t: ItemType) => LIBELLES_TYPE[t];

  genererAvecIa(): void {
    if (!this.promptIa.trim()) {
      return;
    }
    this.generationIa.set(true);
    this.ia
      .generateListing({
        prompt: this.promptIa,
        category: this.item.category,
        condition: this.item.condition,
        type: this.item.type,
        location: this.item.location,
      })
      .subscribe({
        next: (res) => {
          this.item = {
            ...this.item,
            title: res.title,
            description: res.description,
            category: res.suggestedCategory,
            condition: res.suggestedCondition,
            price: res.suggestedPrice,
            aiEstimatedPrice: res.suggestedPrice,
            tags: res.tags.join(', '),
          };
          this.resumeIa.set(res.aiSummary);
          this.generationIa.set(false);
          this.analyser();
        },
        error: (err) => {
          this.generationIa.set(false);
          this.erreur.set(messageErreur(err));
        },
      });
  }

  estimerPrix(): void {
    this.ia
      .estimatePrice({ title: this.item.title, category: this.item.category, condition: this.item.condition })
      .subscribe({
        next: (res) => {
          this.item = { ...this.item, price: res.recommendedPrice, aiEstimatedPrice: res.recommendedPrice };
          this.resumeIa.set(res.rationale);
        },
        error: (err) => this.erreur.set(messageErreur(err)),
      });
  }

  onTypeChange(): void {
    if (this.item.type === 'DONATION') {
      this.item = { ...this.item, price: 0 };
    }
    this.analyser();
  }

  analyser(): void {
    if (!this.item.title) {
      return;
    }
    this.ia
      .moderateContent({
        title: this.item.title,
        description: this.item.description,
        category: this.item.category,
        price: String(this.item.price ?? 0),
      })
      .subscribe({
        next: (res) => this.moderation.set(res),
        error: () => undefined,
      });
  }

  publier(): void {
    this.publication.set(true);
    this.erreur.set(null);
    const utilisateur = this.auth.currentUser();
    const payload: MarketplaceItem = {
      ...this.item,
      sellerId: utilisateur?.id,
      sellerName: this.item.sellerName || this.auth.userName(),
      sellerEmail: this.item.sellerEmail || utilisateur?.email,
    };
    this.api.createItem(payload).subscribe({
      next: (created) => {
        this.publication.set(false);
        this.router.navigate(['/marketplace/items', created.id]);
      },
      error: (err) => {
        this.publication.set(false);
        this.erreur.set(messageErreur(err));
      },
    });
  }

  private itemVide(): MarketplaceItem {
    return {
      title: '',
      description: '',
      category: 'ELECTRONICS',
      condition: 'GOOD',
      type: 'SALE',
      status: 'AVAILABLE',
      price: 50,
      tags: 'objet-trouve, verifie-ia',
      location: 'Guichet central des objets trouvés',
      originalObjectId: String(Math.floor(1000 + Math.random() * 9000)),
      unclaimedDays: 90,
    };
  }
}
