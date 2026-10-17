import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { MarketplaceService } from '../../services/marketplace.service';
import { AiAssistantService } from '../../services/ai-assistant.service';
import { Category, ItemCondition, ItemStatus, ItemType, MarketplaceItem, ModerationResponse } from '../../models/marketplace.model';

@Component({
  selector: 'app-create-item',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  template: `
    <div class="create-page container">
      <!-- Breadcrumb -->
      <div class="breadcrumb">
        <a routerLink="/"><i class="fa-solid fa-arrow-left"></i> Retour au catalogue</a>
      </div>

      <div class="page-header">
        <div class="badge badge-ai mb-2">
          <i class="fa-solid fa-microchip"></i> Assistant Studio de Revalorisation IA
        </div>
        <h1>Publier une Annonce Marketplace</h1>
        <p>Utilisez les agents IA pour générer automatiquement le titre, le descriptif valorisant, les tags et l'estimation de prix juste.</p>
      </div>

      <!-- AI ASSISTANT BOX -->
      <section class="ai-studio-card glass-card">
        <div class="ai-studio-header">
          <div class="ai-studio-title">
            <div class="ai-badge-icon"><i class="fa-solid fa-wand-magic-sparkles"></i></div>
            <div>
              <h3>Assistant IA de Rédaction & Tarification</h3>
              <p>Saisissez quelques mots-clés ou l'objet brut recueilli pour que l'IA structure l'annonce.</p>
            </div>
          </div>
          <span class="badge badge-ai">LLM Prompt Engine v2.4</span>
        </div>

        <div class="ai-prompt-area">
          <div class="form-group mb-2">
            <label class="form-label">Description brute / Mots-clés de l'objet trouvé *</label>
            <div class="prompt-input-row">
              <input 
                type="text" 
                class="form-control prompt-input" 
                placeholder="Ex: iPad Air gris sidéral 64go avec étui cuir, trouvé dans le train, écran intact" 
                [(ngModel)]="aiPrompt"
                (keyup.enter)="generateWithAi()"
              />
              <button type="button" class="btn btn-ai" (click)="generateWithAi()" [disabled]="aiGenerating || !aiPrompt.trim()">
                <i class="fa-solid fa-sparkles" [class.fa-spin]="aiGenerating"></i> 
                {{ aiGenerating ? 'Génération IA...' : '✨ Générer avec l’IA' }}
              </button>
            </div>
          </div>

          <!-- Quick Prompts suggestions -->
          <div class="quick-prompts">
            <span class="quick-lbl">Exemples rapides :</span>
            <button class="chip" (click)="setPromptExample('Trottinette électrique Xiaomi Mi Pro 2, chargeur inclus, état moyen')">Trottinette Xiaomi</button>
            <button class="chip" (click)="setPromptExample('Guitare acoustique Yamaha F310 oubliée en salle de musique, très bon état')">Guitare Yamaha</button>
            <button class="chip" (click)="setPromptExample('Montre connectée Garmin Forerunner avec câble USB, bon état')">Montre Garmin</button>
            <button class="chip" (click)="setPromptExample('Manteau d’hiver imperméable bleu taille M pour don solidaire étudiant')">Manteau Don Solidaire</button>
          </div>
        </div>

        <!-- AI Insights Feedback Banner -->
        <div class="ai-feedback-banner" *ngIf="aiSummary">
          <div class="ai-summary-header">
            <i class="fa-solid fa-circle-check text-success"></i>
            <strong>Synthèse de l'optimisation IA :</strong>
          </div>
          <p class="ai-summary-text">{{ aiSummary }}</p>
        </div>
      </section>

      <!-- MAIN FORM -->
      <form (ngSubmit)="submitListing()" class="listing-form glass-card">
        <div class="form-grid">
          <!-- Col 1 -->
          <div class="form-col">
            <div class="form-group">
              <label class="form-label">Titre de l'annonce *</label>
              <input type="text" class="form-control" [(ngModel)]="item.title" name="title" required (input)="onFormChange()" />
            </div>

            <div class="form-row-2">
              <div class="form-group">
                <label class="form-label">Catégorie *</label>
                <select class="form-select" [(ngModel)]="item.category" name="category" required (change)="onFormChange()">
                  <option value="ELECTRONICS">Électronique & High-Tech</option>
                  <option value="JEWELRY_WATCHES">Bijoux & Montres</option>
                  <option value="BAGS_LUGGAGE">Sacs & Bagages</option>
                  <option value="DOCUMENTS_KEYS">Documents & Clés</option>
                  <option value="CLOTHING_ACCESSORIES">Vêtements & Accessoires</option>
                  <option value="BICYCLES_VEHICLES">Vélos & Mobilité</option>
                  <option value="SPORTS_LEISURE">Sports & Loisirs</option>
                  <option value="OTHER">Autre</option>
                </select>
              </div>

              <div class="form-group">
                <label class="form-label">État constaté *</label>
                <select class="form-select" [(ngModel)]="item.condition" name="condition" required (change)="onFormChange()">
                  <option value="NEW_UNOPENED">Neuf sous emballage (100% valeur)</option>
                  <option value="EXCELLENT">Comme neuf (85% valeur)</option>
                  <option value="GOOD">Bon état (65% valeur)</option>
                  <option value="FAIR">État moyen / usure (40% valeur)</option>
                  <option value="FOR_PARTS">Pour pièces / HS (15% valeur)</option>
                </select>
              </div>
            </div>

            <div class="form-row-2">
              <div class="form-group">
                <label class="form-label">Type de publication *</label>
                <select class="form-select" [(ngModel)]="item.type" name="type" required (change)="onTypeChange()">
                  <option value="SALE">Vente solidaire (Objet non réclamé)</option>
                  <option value="DONATION">Don solidaire gratuit (0 €)</option>
                  <option value="REWARD_BOUNTY">Avis de recherche / Récompense</option>
                </select>
              </div>

              <div class="form-group">
                <div class="price-label-row">
                  <label class="form-label">Prix (€) *</label>
                  <button type="button" class="btn-text-ai" (click)="estimatePriceWithAi()" [disabled]="item.type === 'DONATION'">
                    <i class="fa-solid fa-calculator"></i> Estimer le prix (IA)
                  </button>
                </div>
                <input 
                  type="number" 
                  step="0.5" 
                  class="form-control" 
                  [(ngModel)]="item.price" 
                  name="price" 
                  [disabled]="item.type === 'DONATION'" 
                  required 
                  (input)="onFormChange()"
                />
                <small class="text-subtle" *ngIf="item.aiEstimatedPrice && item.type !== 'DONATION'">
                  Estimation IA recommandée : {{ item.aiEstimatedPrice }} €
                </small>
              </div>
            </div>

            <div class="form-group">
              <label class="form-label">Description détaillée (Markdown supporté) *</label>
              <textarea class="form-control desc-area" rows="8" [(ngModel)]="item.description" name="description" required (input)="onFormChange()"></textarea>
            </div>
          </div>

          <!-- Col 2 -->
          <div class="form-col">
            <div class="form-group">
              <label class="form-label">URL de la photo de l'objet</label>
              <input type="text" class="form-control" [(ngModel)]="item.imageUrl" name="imageUrl" placeholder="https://images.unsplash.com/..." />
              <div class="image-preview" *ngIf="item.imageUrl">
                <img [src]="item.imageUrl" alt="Aperçu photo" class="preview-img" (error)="onImageError()" />
              </div>
            </div>

            <div class="form-row-2">
              <div class="form-group">
                <label class="form-label">ID Objet Trouvé d'origine</label>
                <input type="text" class="form-control" [(ngModel)]="item.originalObjectId" name="originalObjectId" placeholder="Ex: OBJ-2024-4412" />
              </div>

              <div class="form-group">
                <label class="form-label">Lieu de collecte / retrait</label>
                <input type="text" class="form-control" [(ngModel)]="item.location" name="location" placeholder="Ex: Guichet Central Gare" />
              </div>
            </div>

            <div class="form-group">
              <label class="form-label">Tags (séparés par des virgules)</label>
              <input type="text" class="form-control" [(ngModel)]="item.tags" name="tags" placeholder="smartphone, apple, revalorise" />
            </div>

            <div class="form-row-2">
              <div class="form-group">
                <label class="form-label">Nom du vendeur / service</label>
                <input type="text" class="form-control" [(ngModel)]="item.sellerName" name="sellerName" placeholder="Service Objets Trouvés" />
              </div>

              <div class="form-group">
                <label class="form-label">Email de contact</label>
                <input type="email" class="form-control" [(ngModel)]="item.sellerEmail" name="sellerEmail" placeholder="contact@objets-trouves.fr" />
              </div>
            </div>

            <!-- LIVE AI SAFETY & MODERATION MONITOR -->
            <div class="ai-safety-monitor" [ngClass]="getSafetyCardClass()">
              <div class="safety-header">
                <div class="safety-title">
                  <i [ngClass]="getSafetyIcon()"></i>
                  <span>Contrôle de Sécurité & Conformité IA</span>
                </div>
                <span class="safety-score">{{ moderationResult ? moderationResult.safetyScore : 98 }}%</span>
              </div>

              <p class="safety-desc">
                {{ moderationResult ? moderationResult.aiExplanation : 'Analyse en continu des textes contre les contrefaçons, objets illégaux et pièces d\'identité.' }}
              </p>

              <div class="safety-warnings" *ngIf="moderationResult && moderationResult.flaggedIssues.length > 0">
                <div class="warning-pill" *ngFor="let issue of moderationResult.flaggedIssues">
                  <i class="fa-solid fa-triangle-exclamation"></i> {{ issue }}
                </div>
              </div>
            </div>
          </div>
        </div>

        <div class="form-actions-bottom">
          <a routerLink="/" class="btn btn-secondary">Annuler</a>
          <button type="submit" class="btn btn-primary btn-submit" [disabled]="submitting || (moderationResult && !moderationResult.approved)">
            <i class="fa-solid fa-check"></i> {{ submitting ? 'Publication en cours...' : 'Publier l’annonce sur le Marketplace' }}
          </button>
        </div>
      </form>
    </div>
  `,
  styles: [`
    .create-page {
      padding: 30px 0 60px;
    }
    .breadcrumb {
      margin-bottom: 16px;
    }
    .breadcrumb a {
      color: var(--text-muted);
      font-size: 0.9rem;
      display: inline-flex;
      align-items: center;
      gap: 6px;
    }
    .breadcrumb a:hover { color: #fff; }
    .page-header {
      margin-bottom: 24px;
    }
    .page-header h1 {
      font-size: 2.2rem;
      margin-bottom: 6px;
    }
    .ai-studio-card {
      margin-bottom: 28px;
      padding: 24px;
      border: 1px solid rgba(168, 85, 247, 0.3);
      background: linear-gradient(135deg, rgba(30, 20, 50, 0.7) 0%, rgba(17, 24, 39, 0.8) 100%);
      box-shadow: var(--shadow-ai-glow);
    }
    .ai-studio-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 16px;
    }
    .ai-studio-title {
      display: flex;
      align-items: center;
      gap: 14px;
    }
    .ai-badge-icon {
      width: 44px;
      height: 44px;
      border-radius: var(--radius-md);
      background: var(--ai-glow);
      display: flex;
      align-items: center;
      justify-content: center;
      color: #fff;
      font-size: 1.2rem;
    }
    .prompt-input-row {
      display: flex;
      gap: 12px;
    }
    .prompt-input {
      font-size: 1.05rem;
      background: rgba(11, 15, 25, 0.85);
      border-color: rgba(168, 85, 247, 0.4);
    }
    .quick-prompts {
      display: flex;
      align-items: center;
      gap: 8px;
      flex-wrap: wrap;
      margin-top: 10px;
    }
    .quick-lbl {
      font-size: 0.78rem;
      color: var(--text-muted);
    }
    .chip {
      padding: 4px 10px;
      font-size: 0.78rem;
      background: rgba(255, 255, 255, 0.05);
      border: 1px solid var(--border-glass);
      color: var(--text-main);
      border-radius: var(--radius-full);
      cursor: pointer;
    }
    .chip:hover {
      background: rgba(168, 85, 247, 0.2);
      border-color: #c084fc;
    }
    .ai-feedback-banner {
      margin-top: 16px;
      padding: 12px 16px;
      border-radius: var(--radius-md);
      background: rgba(16, 185, 129, 0.08);
      border: 1px solid rgba(16, 185, 129, 0.2);
      font-size: 0.88rem;
    }
    .ai-summary-header {
      display: flex;
      align-items: center;
      gap: 8px;
      color: #34d399;
      margin-bottom: 4px;
    }
    .ai-summary-text {
      color: #e5e7eb;
      margin: 0;
    }
    .listing-form {
      padding: 30px;
    }
    .form-grid {
      display: grid;
      grid-template-columns: repeat(2, 1fr);
      gap: 30px;
    }
    .form-row-2 {
      display: grid;
      grid-template-columns: repeat(2, 1fr);
      gap: 16px;
    }
    .price-label-row {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 8px;
    }
    .btn-text-ai {
      background: none;
      border: none;
      color: #c084fc;
      font-size: 0.78rem;
      font-weight: 600;
      cursor: pointer;
      display: inline-flex;
      align-items: center;
      gap: 4px;
    }
    .btn-text-ai:hover { text-decoration: underline; }
    .desc-area {
      font-family: monospace;
      font-size: 0.88rem;
      line-height: 1.5;
    }
    .image-preview {
      margin-top: 10px;
      height: 140px;
      border-radius: var(--radius-md);
      overflow: hidden;
      border: 1px solid var(--border-glass);
    }
    .preview-img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }
    .ai-safety-monitor {
      padding: 16px;
      border-radius: var(--radius-md);
      border: 1px solid var(--border-glass);
      margin-top: 14px;
      transition: all var(--transition-smooth);
    }
    .safety-low {
      background: rgba(16, 185, 129, 0.08);
      border-color: rgba(16, 185, 129, 0.3);
    }
    .safety-high, .safety-blocked {
      background: rgba(239, 68, 68, 0.08);
      border-color: rgba(239, 68, 68, 0.4);
    }
    .safety-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 8px;
    }
    .safety-title {
      display: flex;
      align-items: center;
      gap: 8px;
      font-weight: 700;
      font-size: 0.9rem;
    }
    .safety-score {
      font-size: 1.2rem;
      font-weight: 800;
    }
    .safety-desc {
      font-size: 0.82rem;
      margin: 0;
    }
    .safety-warnings {
      margin-top: 10px;
      display: flex;
      flex-direction: column;
      gap: 6px;
    }
    .warning-pill {
      font-size: 0.78rem;
      color: #fca5a5;
      background: rgba(239, 68, 68, 0.15);
      padding: 4px 10px;
      border-radius: 4px;
      display: flex;
      align-items: center;
      gap: 6px;
    }
    .form-actions-bottom {
      display: flex;
      justify-content: flex-end;
      gap: 16px;
      margin-top: 30px;
      padding-top: 24px;
      border-top: 1px solid var(--border-glass);
    }
    .btn-submit {
      padding: 12px 28px;
      font-size: 1rem;
    }
    @media (max-width: 900px) {
      .form-grid { grid-template-columns: 1fr; }
      .prompt-input-row { flex-direction: column; }
    }
  `]
})
export class CreateItemComponent {
  aiPrompt = '';
  aiGenerating = false;
  aiSummary = '';
  moderationResult: ModerationResponse | null = null;
  submitting = false;

  item: MarketplaceItem = {
    title: '',
    description: '',
    category: 'ELECTRONICS',
    condition: 'GOOD',
    type: 'SALE',
    status: 'AVAILABLE',
    price: 50,
    imageUrl: 'https://images.unsplash.com/photo-1592750475338-74b7b21085ab?w=600&auto=format&fit=crop&q=80',
    tags: 'objet-trouve, verifie-ia',
    location: 'Guichet Central des Objets Trouvés',
    originalObjectId: 'OBJ-' + Math.floor(1000 + Math.random() * 9000),
    sellerName: 'Service Revalorisation',
    sellerEmail: 'service.revalorisation@ville.fr',
    unclaimedDays: 90
  };

  constructor(
    private marketplaceService: MarketplaceService,
    private aiService: AiAssistantService,
    private router: Router
  ) {}

  setPromptExample(text: string): void {
    this.aiPrompt = text;
  }

  generateWithAi(): void {
    if (!this.aiPrompt.trim()) return;
    this.aiGenerating = true;

    this.aiService.generateListing({
      prompt: this.aiPrompt,
      category: this.item.category,
      condition: this.item.condition,
      type: this.item.type,
      location: this.item.location
    }).subscribe({
      next: (res) => {
        this.item.title = res.title;
        this.item.description = res.description;
        this.item.category = res.suggestedCategory;
        this.item.condition = res.suggestedCondition;
        this.item.price = res.suggestedPrice;
        this.item.aiEstimatedPrice = res.suggestedPrice;
        this.item.tags = res.tags.join(', ');
        this.aiSummary = res.aiSummary;

        this.aiGenerating = false;
        this.onFormChange();
      },
      error: (err) => {
        console.error('Erreur génération IA:', err);
        this.aiGenerating = false;
      }
    });
  }

  estimatePriceWithAi(): void {
    this.aiService.estimatePrice({
      title: this.item.title,
      category: this.item.category,
      condition: this.item.condition
    }).subscribe({
      next: (res) => {
        this.item.price = res.recommendedPrice;
        this.item.aiEstimatedPrice = res.recommendedPrice;
        this.aiSummary = res.rationale;
        this.onFormChange();
      },
      error: (err) => console.error('Erreur estimation prix:', err)
    });
  }

  onTypeChange(): void {
    if (this.item.type === 'DONATION') {
      this.item.price = 0;
    }
    this.onFormChange();
  }

  onFormChange(): void {
    if (!this.item.title) return;
    this.aiService.moderateContent({
      title: this.item.title,
      description: this.item.description,
      category: this.item.category,
      price: this.item.price != null ? this.item.price.toString() : '0'
    }).subscribe({
      next: (res) => this.moderationResult = res,
      error: (err) => console.error('Erreur modération:', err)
    });
  }

  onImageError(): void {
    this.item.imageUrl = 'https://images.unsplash.com/photo-1550009158-9ebf69173e03?w=600&auto=format&fit=crop&q=80';
  }

  getSafetyCardClass(): string {
    if (!this.moderationResult) return 'safety-low';
    if (this.moderationResult.riskLevel === 'BLOCKED') return 'safety-blocked';
    if (this.moderationResult.riskLevel === 'HIGH') return 'safety-high';
    return 'safety-low';
  }

  getSafetyIcon(): string {
    if (!this.moderationResult) return 'fa-solid fa-shield-check text-success';
    if (!this.moderationResult.approved) return 'fa-solid fa-triangle-exclamation text-danger';
    return 'fa-solid fa-shield-check text-success';
  }

  submitListing(): void {
    this.submitting = true;
    this.marketplaceService.createItem(this.item).subscribe({
      next: (created) => {
        this.submitting = false;
        alert(`Annonce créée avec succès ! L'objet '${created.title}' est maintenant visible sur le marketplace.`);
        this.router.navigate(['/']);
      },
      error: (err) => {
        console.error('Erreur création annonce:', err);
        alert('Erreur lors de la publication de l\'annonce.');
        this.submitting = false;
      }
    });
  }
}
