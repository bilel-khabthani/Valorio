import { CommonModule } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import {
  AiAssistantApiService,
  AnomalyDetectionResult,
  Category,
  EstimatePriceResponse,
  GenerateListingResponse,
  ItemCondition,
  ItemType,
  ModerationResponse,
} from '@valorio/api-marketplace';
import { messageErreur } from '@valorio/shared';

type Onglet = 'GENERATOR' | 'VALUATION' | 'MODERATION' | 'ANOMALY' | 'TESTS';

@Component({
  selector: 'valorio-marketplace-ai-lab',
  imports: [CommonModule, FormsModule, RouterLink],
  template: `
    <section class="page">
      <header class="page-header">
        <div>
          <a class="muted" routerLink="..">← Retour au catalogue</a>
          <h1>Laboratoire IA & qualité</h1>
          <p>
            Démonstration interactive : génération d'annonce, estimation de prix, modération et
            détection d'anomalies — moteur heuristique hybride.
          </p>
        </div>
      </header>

      @if (erreur()) {
        <div class="alert error">{{ erreur() }}</div>
      }

      <div class="card actions">
        @for (tab of onglets; track tab.id) {
          <button class="btn secondary small" [class.actif]="onglet() === tab.id" (click)="onglet.set(tab.id)">
            {{ tab.label }}
          </button>
        }
      </div>

      @switch (onglet()) {
        @case ('GENERATOR') {
          <div class="grid grid-2">
            <div class="card">
              <h2>Banc d'essai : prompt LLM</h2>
              <label class="field">
                <span>Prompt utilisateur</span>
                <textarea name="genPrompt" rows="3" [(ngModel)]="promptGeneration"></textarea>
              </label>
              <div class="actions">
                <button class="btn secondary small" (click)="promptGeneration = 'MacBook Pro 14 M2 gris oublié en amphi A'">MacBook Pro</button>
                <button class="btn secondary small" (click)="promptGeneration = 'Bague en argent avec pierre bleue trouvée au vestiaire'">Bague argent</button>
              </div>
              <button class="btn" type="button" [disabled]="generation()" (click)="executerGeneration()">
                {{ generation() ? 'Génération...' : 'Exécuter l’inférence IA' }}
              </button>
            </div>
            <div class="card">
              <h2>Sortie structurée</h2>
              @if (resultatGeneration(); as res) {
                <p><strong>Titre :</strong> {{ res.title }}</p>
                <p>
                  <span class="badge">{{ res.suggestedCategory }}</span>
                  <span class="badge">{{ res.suggestedCondition }}</span>
                </p>
                <p>
                  <strong>{{ res.suggestedPrice }} DT</strong>
                  <span class="muted">[{{ res.minPrice }} – {{ res.maxPrice }} DT]</span>
                </p>
                <div class="actions">
                  @for (tag of res.tags; track tag) {
                    <span class="badge">#{{ tag }}</span>
                  }
                </div>
                <pre class="desc">{{ res.description }}</pre>
              } @else {
                <p class="empty">Lancez l'inférence pour observer le résultat.</p>
              }
            </div>
          </div>
        }

        @case ('VALUATION') {
          <div class="grid grid-2">
            <div class="card">
              <h2>Simulateur de décote</h2>
              <label class="field">
                <span>Dénomination</span>
                <input type="text" name="estTitle" [(ngModel)]="titreEstimation" />
              </label>
              <label class="field">
                <span>Catégorie</span>
                <select name="estCategory" [(ngModel)]="categorieEstimation">
                  <option value="ELECTRONICS">Électronique</option>
                  <option value="JEWELRY_WATCHES">Bijoux & Montres</option>
                  <option value="BICYCLES_VEHICLES">Vélos & Mobilité</option>
                  <option value="OTHER">Autre</option>
                </select>
              </label>
              <label class="field">
                <span>État</span>
                <select name="estCondition" [(ngModel)]="etatEstimation">
                  <option value="NEW_UNOPENED">Neuf (x1.00)</option>
                  <option value="EXCELLENT">Comme neuf (x0.85)</option>
                  <option value="GOOD">Bon état (x0.65)</option>
                  <option value="FAIR">Moyen (x0.40)</option>
                  <option value="FOR_PARTS">Pour pièces (x0.15)</option>
                </select>
              </label>
              <button class="btn" type="button" (click)="executerEstimation()">Calculer la valorisation</button>
            </div>
            <div class="card">
              <h2>Rapport d'évaluation</h2>
              @if (resultatEstimation(); as res) {
                <p><strong class="gros">{{ res.recommendedPrice }} DT</strong> — fourchette {{ res.minFairPrice }} – {{ res.maxFairPrice }} DT</p>
                <p>Gamme : <span class="badge">{{ res.pricingTier }}</span></p>
                <p>Décote : <strong>-{{ res.estimatedDepreciationPercent }}%</strong></p>
                <p class="muted">{{ res.rationale }}</p>
                <ul>
                  @for (facteur of res.marketFactors; track facteur) {
                    <li class="muted">{{ facteur }}</li>
                  }
                </ul>
              } @else {
                <p class="empty">Lancez le calcul pour voir le rapport.</p>
              }
            </div>
          </div>
        }

        @case ('MODERATION') {
          <div class="grid grid-2">
            <div class="card">
              <h2>Bac à sable modération</h2>
              <label class="field">
                <span>Titre</span>
                <input type="text" name="modTitle" [(ngModel)]="titreModeration" />
              </label>
              <label class="field">
                <span>Description</span>
                <textarea name="modDesc" rows="3" [(ngModel)]="descModeration"></textarea>
              </label>
              <div class="actions">
                <button class="btn secondary small" (click)="scenarioModeration('PASSPORT')">Vente passeport</button>
                <button class="btn secondary small" (click)="scenarioModeration('NORMAL')">Vélo normal</button>
              </div>
              <button class="btn" type="button" (click)="executerModeration()">Analyser la conformité</button>
            </div>
            <div class="card">
              <h2>Diagnostic</h2>
              @if (resultatModeration(); as res) {
                <div class="alert" [class.success]="res.approved" [class.error]="!res.approved">
                  <strong>{{ res.approved ? 'Annonce conforme' : 'Annonce bloquée' }}</strong>
                  — score {{ res.safetyScore }}% (risque {{ res.riskLevel }})
                </div>
                <p class="muted">{{ res.aiExplanation }}</p>
                @for (issue of res.flaggedIssues; track issue) {
                  <div class="alert error">{{ issue }}</div>
                }
              } @else {
                <p class="empty">Analysez un scénario pour voir le diagnostic.</p>
              }
            </div>
          </div>
        }

        @case ('ANOMALY') {
          <div class="grid grid-2">
            <div class="card">
              <h2>Détection d'anomalies de prix</h2>
              <label class="field">
                <span>Titre</span>
                <input type="text" name="anomTitle" [(ngModel)]="titreAnomalie" />
              </label>
              <label class="field">
                <span>Type</span>
                <select name="anomType" [(ngModel)]="typeAnomalie">
                  <option value="SALE">Vente</option>
                  <option value="DONATION">Don (doit être 0 DT)</option>
                </select>
              </label>
              <label class="field">
                <span>Prix (DT)</span>
                <input type="number" name="anomPrice" [(ngModel)]="prixAnomalie" />
              </label>
              <div class="actions">
                <button class="btn secondary small" (click)="scenarioAnomalie('LOW')">iPhone à 2 DT</button>
                <button class="btn secondary small" (click)="scenarioAnomalie('DONATION')">Don à 35 DT</button>
              </div>
              <button class="btn" type="button" (click)="executerAnomalie()">Détecter les anomalies</button>
            </div>
            <div class="card">
              <h2>Rapport de détection</h2>
              @if (resultatAnomalie(); as res) {
                <div class="alert" [class.success]="!res.anomalyDetected" [class.error]="res.anomalyDetected">
                  <strong>{{ res.anomalyDetected ? 'Anomalie détectée' : 'Prix cohérent' }}</strong>
                  — {{ res.anomalyType }}, score {{ (res.anomalyScore * 100) | number: '1.0-0' }}%
                </div>
                <p>Prix : {{ res.actualPrice }} DT — normalité {{ res.expectedMin }} – {{ res.expectedMax }} DT</p>
                <p class="muted">{{ res.diagnosticMessage }}</p>
                @for (rec of res.recommendations; track rec) {
                  <p class="muted">• {{ rec }}</p>
                }
              } @else {
                <p class="empty">Testez un cas limite.</p>
              }
            </div>
          </div>
        }

        @case ('TESTS') {
          <div class="card">
            <h2>Suite de tests & assurance qualité</h2>
            <p class="muted">
              Validation par tests unitaires JUnit 5, tests de contrôleurs MockMvc et scénarios de
              robustesse (rédaction générée avec l'assistance de l'IA).
            </p>
            <table>
              <thead>
                <tr>
                  <th>Classe</th>
                  <th>Cas de test</th>
                  <th>Objectif</th>
                </tr>
              </thead>
              <tbody>
                @for (test of tests; track test.methode) {
                  <tr>
                    <td><code>{{ test.classe }}</code></td>
                    <td><code>{{ test.methode }}</code></td>
                    <td>{{ test.objectif }}</td>
                  </tr>
                }
              </tbody>
            </table>
          </div>
        }
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
      .desc {
        background: #f1f5f9;
        padding: 0.75rem;
        border-radius: var(--radius);
        font-size: 0.8rem;
        white-space: pre-wrap;
        max-height: 240px;
        overflow-y: auto;
      }
      .gros {
        font-size: 1.6rem;
      }
      code {
        color: var(--ai-dark);
        font-size: 0.82rem;
      }
    `,
  ],
})
export class MarketplaceAiLab {
  private readonly ia = inject(AiAssistantApiService);

  readonly onglets = [
    { id: 'GENERATOR' as Onglet, label: '1. Génération' },
    { id: 'VALUATION' as Onglet, label: '2. Estimation' },
    { id: 'MODERATION' as Onglet, label: '3. Modération' },
    { id: 'ANOMALY' as Onglet, label: '4. Anomalies' },
    { id: 'TESTS' as Onglet, label: '5. Tests & qualité' },
  ];

  readonly onglet = signal<Onglet>('GENERATOR');
  readonly erreur = signal<string | null>(null);

  // Génération
  promptGeneration = 'iPhone 14 Pro 256go violet trouvé dans le bus, état impeccable';
  readonly generation = signal(false);
  readonly resultatGeneration = signal<GenerateListingResponse | null>(null);

  // Estimation
  titreEstimation = 'VTT Rockrider ST 530';
  categorieEstimation: Category = 'BICYCLES_VEHICLES';
  etatEstimation: ItemCondition = 'GOOD';
  readonly resultatEstimation = signal<EstimatePriceResponse | null>(null);

  // Modération
  titreModeration = 'Passeport biométrique trouvé';
  descModeration = 'Document officiel retrouvé en gare, proposé à la vente.';
  readonly resultatModeration = signal<ModerationResponse | null>(null);

  // Anomalie
  titreAnomalie = 'Apple iPhone 15 Pro';
  typeAnomalie: ItemType = 'SALE';
  prixAnomalie = 2;
  readonly resultatAnomalie = signal<AnomalyDetectionResult | null>(null);

  readonly tests = [
    { classe: 'AiServiceTest', methode: 'genereUneAnnonceAvecTitreEtPrix()', objectif: 'Synthèse du titre, du markdown et des tags depuis des mots-clés bruts.' },
    { classe: 'AiServiceTest', methode: 'genereUnDonGratuit()', objectif: 'Un don solidaire est valorisé à 0 DT.' },
    { classe: 'AiServiceTest', methode: 'estimeUnPrixSelonCategorieEtEtat()', objectif: 'Courbe de décote selon l’état et la catégorie.' },
    { classe: 'AiServiceTest', methode: 'refuseLesContenusProhibes()', objectif: 'Bloque la mise en vente de pièces d’identité et contenus illicites.' },
    { classe: 'AiServiceTest', methode: 'detecteUneAnomalieDePrixSurUnDon()', objectif: 'Incohérence d’un don assorti d’un prix.' },
    { classe: 'MarketplaceItemControllerTest', methode: 'creeUnArticle()', objectif: 'Création d’annonce exposée en HTTP 201.' },
    { classe: 'MarketplaceOfferControllerTest', methode: 'changeLeStatutDUneOffre()', objectif: 'Transitions PENDING → ACCEPTED → COMPLETED.' },
    { classe: 'AiAssistantControllerTest', methode: 'genereUneAnnonce()', objectif: 'Endpoint /ai/generate-listing sérialisé correctement.' },
  ];

  executerGeneration(): void {
    this.generation.set(true);
    this.erreur.set(null);
    this.ia.generateListing({ prompt: this.promptGeneration }).subscribe({
      next: (res) => {
        this.resultatGeneration.set(res);
        this.generation.set(false);
      },
      error: (err) => {
        this.generation.set(false);
        this.erreur.set(messageErreur(err));
      },
    });
  }

  executerEstimation(): void {
    this.ia
      .estimatePrice({ title: this.titreEstimation, category: this.categorieEstimation, condition: this.etatEstimation })
      .subscribe({
        next: (res) => this.resultatEstimation.set(res),
        error: (err) => this.erreur.set(messageErreur(err)),
      });
  }

  scenarioModeration(scenario: 'PASSPORT' | 'NORMAL'): void {
    if (scenario === 'PASSPORT') {
      this.titreModeration = 'Passeport biométrique trouvé';
      this.descModeration = 'Document d’identité officiel retrouvé en gare, proposé à la vente.';
    } else {
      this.titreModeration = 'Vélo de ville Peugeot vintage';
      this.descModeration = 'Vélo révisé, non réclamé après 6 mois.';
    }
  }

  executerModeration(): void {
    this.ia.moderateContent({ title: this.titreModeration, description: this.descModeration }).subscribe({
      next: (res) => this.resultatModeration.set(res),
      error: (err) => this.erreur.set(messageErreur(err)),
    });
  }

  scenarioAnomalie(scenario: 'LOW' | 'DONATION'): void {
    if (scenario === 'LOW') {
      this.titreAnomalie = 'Apple iPhone 15 Pro';
      this.typeAnomalie = 'SALE';
      this.prixAnomalie = 2;
    } else {
      this.titreAnomalie = 'Manteau d’hiver solidaire';
      this.typeAnomalie = 'DONATION';
      this.prixAnomalie = 35;
    }
  }

  executerAnomalie(): void {
    this.ia
      .detectAnomaly({
        title: this.titreAnomalie,
        category: 'ELECTRONICS',
        condition: 'EXCELLENT',
        type: this.typeAnomalie,
        status: 'AVAILABLE',
        price: this.prixAnomalie,
        description: '',
      })
      .subscribe({
        next: (res) => this.resultatAnomalie.set(res),
        error: (err) => this.erreur.set(messageErreur(err)),
      });
  }
}
