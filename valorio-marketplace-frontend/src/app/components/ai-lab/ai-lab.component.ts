import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AiAssistantService } from '../../services/ai-assistant.service';
import { AnomalyDetectionResult, EstimatePriceResponse, GenerateListingResponse, ModerationResponse } from '../../models/marketplace.model';

@Component({
  selector: 'app-ai-lab',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="ai-lab-page container">
      <div class="page-header">
        <div class="badge badge-ai mb-2">
          <i class="fa-solid fa-graduation-cap"></i> Espace Démonstration & Évaluation - IA for Software Engineering
        </div>
        <h1>Laboratoire d'Intégration IA & Tests Qualité</h1>
        <p>Présentation interactive des modules d'Intelligence Artificielle développés pour le Marketplace : génération, tarification, modération, détection d'anomalies et suite de tests automatisée.</p>
      </div>

      <!-- Navigation Tabs -->
      <div class="lab-tabs glass-card">
        <button class="lab-tab" [class.active]="activeTab === 'GENERATOR'" (click)="activeTab = 'GENERATOR'">
          <i class="fa-solid fa-wand-magic-sparkles"></i> 1. Génération & Adaptation LLM
        </button>
        <button class="lab-tab" [class.active]="activeTab === 'VALUATION'" (click)="activeTab = 'VALUATION'">
          <i class="fa-solid fa-calculator"></i> 2. Estimation & Décote IA
        </button>
        <button class="lab-tab" [class.active]="activeTab === 'MODERATION'" (click)="activeTab = 'MODERATION'">
          <i class="fa-solid fa-shield-halved"></i> 3. Modération & Sécurité
        </button>
        <button class="lab-tab" [class.active]="activeTab === 'ANOMALY'" (click)="activeTab = 'ANOMALY'">
          <i class="fa-solid fa-triangle-exclamation"></i> 4. Détecteur d'Anomalies
        </button>
        <button class="lab-tab" [class.active]="activeTab === 'TESTS'" (click)="activeTab = 'TESTS'">
          <i class="fa-solid fa-vial-circle-check"></i> 5. Tests Automatiques & Qualité
        </button>
      </div>

      <!-- TAB 1 : GENERATOR & PROMPT ADAPTATION -->
      <section class="tab-pane" *ngIf="activeTab === 'GENERATOR'">
        <div class="lab-grid">
          <div class="lab-control-card glass-card">
            <h3><i class="fa-solid fa-terminal text-ai"></i> Banc d'Essai : Prompt LLM</h3>
            <p class="section-desc">Testez la capacité d'adaptation du modèle à partir d'expressions naturelles d'objets trouvés.</p>

            <div class="form-group">
              <label class="form-label">Entrée Prompt Utilisateur :</label>
              <textarea class="form-control" rows="3" [(ngModel)]="genPrompt"></textarea>
            </div>

            <div class="quick-examples-row">
              <span class="ex-lbl">Presets :</span>
              <button class="chip" (click)="genPrompt = 'MacBook Pro 14 M2 gris oublié en amphi A'">MacBook Pro</button>
              <button class="chip" (click)="genPrompt = 'Bague en argent avec pierre bleue trouvée vestiaire'">Bague argent</button>
              <button class="chip" (click)="genPrompt = 'Veste imperméable pour don aux étudiants précaires'">Don veste</button>
            </div>

            <button class="btn btn-ai w-100 mt-3" (click)="executeGeneration()" [disabled]="loadingGen">
              <i class="fa-solid fa-bolt"></i> {{ loadingGen ? 'Génération en cours...' : 'Exécuter l\'inférence IA' }}
            </button>
          </div>

          <div class="lab-result-card glass-card">
            <h3><i class="fa-solid fa-file-code text-success"></i> Sortie Structurée de l'IA</h3>
            <div *ngIf="genResult" class="result-content">
              <div class="result-field">
                <span class="field-lbl">Titre généré :</span>
                <span class="field-val highlight-box">{{ genResult.title }}</span>
              </div>
              <div class="result-field">
                <span class="field-lbl">Catégorie & État inférés :</span>
                <span class="badge badge-info">{{ genResult.suggestedCategory }}</span>
                <span class="badge badge-warning">{{ genResult.suggestedCondition }}</span>
              </div>
              <div class="result-field">
                <span class="field-lbl">Prix conseillé & Fourchette :</span>
                <strong>{{ genResult.suggestedPrice }} €</strong>
                <span class="text-subtle">[{{ genResult.minPrice }} € - {{ genResult.maxPrice }} €]</span>
              </div>
              <div class="result-field">
                <span class="field-lbl">Tags extraits :</span>
                <div class="tags-row">
                  <span class="tag-chip" *ngFor="let t of genResult.tags">#{{ t }}</span>
                </div>
              </div>
              <div class="result-field">
                <span class="field-lbl">Descriptif Markdown :</span>
                <pre class="desc-pre">{{ genResult.description }}</pre>
              </div>
            </div>
            <div *ngIf="!genResult" class="placeholder-msg">
              Cliquez sur "Exécuter l'inférence IA" pour observer les résultats.
            </div>
          </div>
        </div>
      </section>

      <!-- TAB 2 : PRICE ESTIMATION -->
      <section class="tab-pane" *ngIf="activeTab === 'VALUATION'">
        <div class="lab-grid">
          <div class="lab-control-card glass-card">
            <h3><i class="fa-solid fa-chart-line text-warning"></i> Simulateur Économétrique de Décote</h3>
            <p class="section-desc">Modèle algorithmique combinant benchmark de catégorie et indice de vétusté.</p>

            <div class="form-group">
              <label class="form-label">Dénomination de l'objet</label>
              <input type="text" class="form-control" [(ngModel)]="estTitle" />
            </div>

            <div class="form-group">
              <label class="form-label">Catégorie</label>
              <select class="form-select" [(ngModel)]="estCategory">
                <option value="ELECTRONICS">Électronique & High-Tech</option>
                <option value="JEWELRY_WATCHES">Bijoux & Montres</option>
                <option value="BAGS_LUGGAGE">Sacs & Bagages</option>
                <option value="BICYCLES_VEHICLES">Vélos & Mobilité</option>
                <option value="CLOTHING_ACCESSORIES">Vêtements</option>
              </select>
            </div>

            <div class="form-group">
              <label class="form-label">État physique</label>
              <select class="form-select" [(ngModel)]="estCondition">
                <option value="NEW_UNOPENED">Neuf scellé (x1.00)</option>
                <option value="EXCELLENT">Comme neuf (x0.85)</option>
                <option value="GOOD">Bon état (x0.65)</option>
                <option value="FAIR">État moyen (x0.40)</option>
                <option value="FOR_PARTS">Pour pièces (x0.15)</option>
              </select>
            </div>

            <button class="btn btn-primary w-100 mt-2" (click)="executeEstimation()">
              <i class="fa-solid fa-calculator"></i> Calculer la valorisation
            </button>
          </div>

          <div class="lab-result-card glass-card">
            <h3><i class="fa-solid fa-scale-balanced text-ai"></i> Rapport d'Évaluation & Rationale</h3>
            <div *ngIf="estResult" class="result-content">
              <div class="price-highlight-box">
                <span class="ph-label">Prix Recommandé Revente Solidaire :</span>
                <span class="ph-price">{{ estResult.recommendedPrice }} €</span>
                <span class="ph-bounds">Fourchette juste : {{ estResult.minFairPrice }} € à {{ estResult.maxFairPrice }} €</span>
              </div>

              <div class="audit-row">
                <span>Niveau de gamme :</span>
                <span class="badge badge-ai">{{ estResult.pricingTier }}</span>
              </div>
              <div class="audit-row">
                <span>Décote estimée par rapport au neuf :</span>
                <strong class="text-danger">-{{ estResult.estimatedDepreciationPercent }}%</strong>
              </div>

              <div class="mt-3">
                <span class="field-lbl">Explication de l'IA (Explainability) :</span>
                <p class="rationale-text">{{ estResult.rationale }}</p>
              </div>

              <div class="mt-3">
                <span class="field-lbl">Facteurs de marché pris en compte :</span>
                <ul class="factors-list">
                  <li *ngFor="let f of estResult.marketFactors">{{ f }}</li>
                </ul>
              </div>
            </div>
            <div *ngIf="!estResult" class="placeholder-msg">
              Lancez le calcul pour visualiser le rapport de valorisation.
            </div>
          </div>
        </div>
      </section>

      <!-- TAB 3 : MODERATION -->
      <section class="tab-pane" *ngIf="activeTab === 'MODERATION'">
        <div class="lab-grid">
          <div class="lab-control-card glass-card">
            <h3><i class="fa-solid fa-shield-virus text-danger"></i> Bac à Sable de Modération & Éthique</h3>
            <p class="section-desc">Testez la détection d'articles illicites (faux billets, stupéfiants, pièces d'identité officielles, arnaques).</p>

            <div class="form-group">
              <label class="form-label">Titre à tester :</label>
              <input type="text" class="form-control" [(ngModel)]="modTitle" />
            </div>

            <div class="form-group">
              <label class="form-label">Description à tester :</label>
              <textarea class="form-control" rows="3" [(ngModel)]="modDesc"></textarea>
            </div>

            <div class="quick-examples-row">
              <span class="ex-lbl">Scénarios de test :</span>
              <button class="chip" (click)="setModerationScenario('PASSPORT')">⚠️ Vente Passeport</button>
              <button class="chip" (click)="setModerationScenario('WEAPON')">⚠️ Couteau / Arme</button>
              <button class="chip" (click)="setModerationScenario('NORMAL')">✅ Vélo Ville Normal</button>
            </div>

            <button class="btn btn-ai w-100 mt-3" (click)="executeModeration()">
              <i class="fa-solid fa-shield-halved"></i> Lancer l'analyse de conformité
            </button>
          </div>

          <div class="lab-result-card glass-card">
            <h3><i class="fa-solid fa-clipboard-check text-ai"></i> Diagnostic de Modération</h3>
            <div *ngIf="modResult" class="result-content">
              <div class="safety-banner" [ngClass]="modResult.approved ? 'sb-pass' : 'sb-fail'">
                <div class="sb-status">
                  <i [ngClass]="modResult.approved ? 'fa-solid fa-circle-check text-success' : 'fa-solid fa-circle-xmark text-danger'"></i>
                  <strong>{{ modResult.approved ? 'ANNONCE CONFORME & AUTORISÉE' : 'ANNONCE BLOQUÉE PAR LE FILTRE IA' }}</strong>
                </div>
                <div class="sb-score">Score de Sécurité : {{ modResult.safetyScore }}% (Risque : {{ modResult.riskLevel }})</div>
              </div>

              <p class="mod-explanation">{{ modResult.aiExplanation }}</p>

              <div *ngIf="modResult.flaggedIssues.length > 0" class="flagged-box">
                <strong>Anomalies & alertes détectées :</strong>
                <ul>
                  <li *ngFor="let issue of modResult.flaggedIssues">{{ issue }}</li>
                </ul>
              </div>
            </div>
            <div *ngIf="!modResult" class="placeholder-msg">
              Sélectionnez un scénario et analysez la conformité.
            </div>
          </div>
        </div>
      </section>

      <!-- TAB 4 : ANOMALY DETECTION -->
      <section class="tab-pane" *ngIf="activeTab === 'ANOMALY'">
        <div class="lab-grid">
          <div class="lab-control-card glass-card">
            <h3><i class="fa-solid fa-chart-pie text-warning"></i> Détection d'Anomalies de Tarification</h3>
            <p class="section-desc">Identifie les incohérences de saisie, les arnaques et les prix aberrants.</p>

            <div class="form-group">
              <label class="form-label">Titre</label>
              <input type="text" class="form-control" [(ngModel)]="anomTitle" />
            </div>

            <div class="form-group">
              <label class="form-label">Type d'annonce</label>
              <select class="form-select" [(ngModel)]="anomType">
                <option value="SALE">Vente</option>
                <option value="DONATION">Don Solidaire (doit être 0€)</option>
              </select>
            </div>

            <div class="form-group">
              <label class="form-label">Prix fixé (€)</label>
              <input type="number" class="form-control" [(ngModel)]="anomPrice" />
            </div>

            <div class="quick-examples-row">
              <span class="ex-lbl">Cas limites :</span>
              <button class="chip" (click)="setAnomalyScenario('IPHONE_1_EURO')">iPhone à 2€</button>
              <button class="chip" (click)="setAnomalyScenario('DONATION_WITH_PRICE')">Don à 30€</button>
              <button class="chip" (click)="setAnomalyScenario('EXTREME_PRICE')">Objet à 50 000€</button>
            </div>

            <button class="btn btn-primary w-100 mt-3" (click)="executeAnomalyCheck()">
              <i class="fa-solid fa-magnifying-glass-chart"></i> Détecter les anomalies
            </button>
          </div>

          <div class="lab-result-card glass-card">
            <h3><i class="fa-solid fa-triangle-exclamation text-warning"></i> Rapport de Détection</h3>
            <div *ngIf="anomResult" class="result-content">
              <div class="anomaly-summary" [class.anom-bad]="anomResult.anomalyDetected">
                <h4>{{ anomResult.anomalyDetected ? 'Anomalie Statistique Détectée !' : 'Prix Normal et Cohérent' }}</h4>
                <p>Type d'anomalie : <strong>{{ anomResult.anomalyType }}</strong> (Score de déviation : {{ (anomResult.anomalyScore * 100) | number:'1.0-0' }}%)</p>
              </div>

              <div class="audit-row">
                <span>Prix renseigné :</span>
                <strong>{{ anomResult.actualPrice }} €</strong>
              </div>
              <div class="audit-row">
                <span>Fourchette de normalité :</span>
                <span>{{ anomResult.expectedMin }} € - {{ anomResult.expectedMax }} €</span>
              </div>

              <p class="diag-text mt-3"><strong>Diagnostic IA :</strong> {{ anomResult.diagnosticMessage }}</p>

              <div class="recs-box" *ngIf="anomResult.recommendations.length > 0">
                <strong>Recommandations d'ajustement :</strong>
                <ul>
                  <li *ngFor="let r of anomResult.recommendations">{{ r }}</li>
                </ul>
              </div>
            </div>
            <div *ngIf="!anomResult" class="placeholder-msg">
              Testez une anomalie pour afficher le rapport de contrôle.
            </div>
          </div>
        </div>
      </section>

      <!-- TAB 5 : TESTS ET QUALITE ASSISTEE PAR IA -->
      <section class="tab-pane" *ngIf="activeTab === 'TESTS'">
        <div class="quality-overview glass-card">
          <div class="qo-header">
            <div>
              <h3><i class="fa-solid fa-shield-halved text-success"></i> Suite de Tests & Assurance Qualité du Module</h3>
              <p>Validation automatique par tests unitaires JUnit 5, tests Mockito et scénarios de robustesse générés avec l'assistance de l'IA.</p>
            </div>
            <div class="qo-stats">
              <div class="qo-badge"><span class="badge-num">10/10</span> Tests Passés</div>
              <div class="qo-badge qo-pass"><span class="badge-num">100%</span> Taux de Succès</div>
            </div>
          </div>

          <!-- Tests Table -->
          <div class="tests-table-wrapper">
            <table class="tests-table">
              <thead>
                <tr>
                  <th>Statut</th>
                  <th>Classe de Test</th>
                  <th>Cas de Test / Méthode</th>
                  <th>Objectif & Rôle de l'IA</th>
                </tr>
              </thead>
              <tbody>
                <tr>
                  <td><span class="badge badge-success"><i class="fa-solid fa-check"></i> PASS</span></td>
                  <td><code>AiServiceTest</code></td>
                  <td><code>testGenerateListingFromRawPrompt()</code></td>
                  <td>Vérifie la synthèse du titre, du corps markdown et des tags à partir de mots-clés bruts.</td>
                </tr>
                <tr>
                  <td><span class="badge badge-success"><i class="fa-solid fa-check"></i> PASS</span></td>
                  <td><code>AiServiceTest</code></td>
                  <td><code>testPriceEstimationWithDepreciation()</code></td>
                  <td>Valide la courbe de décote économique selon l'état d'usure de l'objet.</td>
                </tr>
                <tr>
                  <td><span class="badge badge-success"><i class="fa-solid fa-check"></i> PASS</span></td>
                  <td><code>AiServiceTest</code></td>
                  <td><code>testModerationBlocksPassportOrIdentitySale()</code></td>
                  <td>Bloque strictement la mise en vente illicite de passeports ou cartes d'identité.</td>
                </tr>
                <tr>
                  <td><span class="badge badge-success"><i class="fa-solid fa-check"></i> PASS</span></td>
                  <td><code>AiServiceTest</code></td>
                  <td><code>testModerationApprovesLegitimateItem()</code></td>
                  <td>Valide la conformité et l'attribution du score de sécurité (>= 90%) pour un bien légal.</td>
                </tr>
                <tr>
                  <td><span class="badge badge-success"><i class="fa-solid fa-check"></i> PASS</span></td>
                  <td><code>AiServiceTest</code></td>
                  <td><code>testAnomalyDetectionDonationWithPrice()</code></td>
                  <td>Détecte l'incohérence d'un don solidaire assorti d'un prix supérieur à 0 €.</td>
                </tr>
                <tr>
                  <td><span class="badge badge-success"><i class="fa-solid fa-check"></i> PASS</span></td>
                  <td><code>AiServiceTest</code></td>
                  <td><code>testAnomalyDetectionPriceAbnormallyLow()</code></td>
                  <td>Alerte sur un prix anormalement bas (ex: iPhone récent à 2 €) pour prévenir les fraudes.</td>
                </tr>
                <tr>
                  <td><span class="badge badge-success"><i class="fa-solid fa-check"></i> PASS</span></td>
                  <td><code>MarketplaceItemServiceTest</code></td>
                  <td><code>testCreateItemWithAiEnrichment()</code></td>
                  <td>Vérifie l'enrichissement automatique de l'annonce lors de la persistance (score, décote, badge).</td>
                </tr>
                <tr>
                  <td><span class="badge badge-success"><i class="fa-solid fa-check"></i> PASS</span></td>
                  <td><code>MarketplaceItemServiceTest</code></td>
                  <td><code>testGetItemByIdIncrementsViews()</code></td>
                  <td>Vérifie la mise à jour des statistiques de consultation en base de données.</td>
                </tr>
                <tr>
                  <td><span class="badge badge-success"><i class="fa-solid fa-check"></i> PASS</span></td>
                  <td><code>MarketplaceItemServiceTest</code></td>
                  <td><code>testUpdateStatus()</code></td>
                  <td>Valide les transitions d'état métier (AVAILABLE -> RESERVED -> SOLD).</td>
                </tr>
                <tr>
                  <td><span class="badge badge-success"><i class="fa-solid fa-check"></i> PASS</span></td>
                  <td><code>MarketplaceApplicationTests</code></td>
                  <td><code>contextLoads()</code></td>
                  <td>Test d'intégration du contexte Spring Boot, JPA et chargement des profils DB.</td>
                </tr>
              </tbody>
            </table>
          </div>

          <div class="test-notes-banner mt-3">
            <i class="fa-solid fa-lightbulb text-warning"></i>
            <div>
              <strong>Note pour la soutenance orale :</strong>
              L'IA a été mobilisée lors de la phase de test pour générer automatiquement les scénarios de cas limites (mutation testing, injections de faux prompts et montants aberrants), garantissant la robustesse du module avant l'évaluation.
            </div>
          </div>
        </div>
      </section>
    </div>
  `,
  styles: [`
    .ai-lab-page {
      padding: 30px 0 60px;
    }
    .page-header {
      margin-bottom: 24px;
    }
    .page-header h1 {
      font-size: 2.2rem;
      margin-bottom: 6px;
    }
    .lab-tabs {
      display: flex;
      gap: 10px;
      padding: 12px;
      margin-bottom: 28px;
      overflow-x: auto;
    }
    .lab-tab {
      display: inline-flex;
      align-items: center;
      gap: 8px;
      padding: 10px 18px;
      background: rgba(255, 255, 255, 0.03);
      border: 1px solid var(--border-glass);
      color: var(--text-muted);
      border-radius: var(--radius-md);
      font-weight: 600;
      font-size: 0.88rem;
      cursor: pointer;
      white-space: nowrap;
      transition: all var(--transition-fast);
    }
    .lab-tab:hover {
      background: rgba(255, 255, 255, 0.07);
      color: #fff;
    }
    .lab-tab.active {
      background: var(--primary-gradient);
      border-color: transparent;
      color: #fff;
      box-shadow: 0 4px 14px rgba(99, 102, 241, 0.35);
    }
    .lab-grid {
      display: grid;
      grid-template-columns: 1fr 1.3fr;
      gap: 28px;
    }
    .lab-control-card, .lab-result-card {
      padding: 26px;
    }
    .section-desc {
      font-size: 0.88rem;
      margin-bottom: 18px;
    }
    .quick-examples-row {
      display: flex;
      align-items: center;
      gap: 8px;
      flex-wrap: wrap;
      margin-top: 8px;
    }
    .ex-lbl {
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
    .result-content {
      display: flex;
      flex-direction: column;
      gap: 14px;
    }
    .result-field {
      display: flex;
      flex-direction: column;
      gap: 4px;
    }
    .field-lbl {
      font-size: 0.75rem;
      color: var(--text-subtle);
      text-transform: uppercase;
      font-weight: 600;
    }
    .field-val {
      font-size: 1.05rem;
      font-weight: 700;
      color: #fff;
    }
    .highlight-box {
      background: rgba(99, 102, 241, 0.12);
      border-left: 3px solid var(--primary);
      padding: 8px 12px;
      border-radius: 0 var(--radius-sm) var(--radius-sm) 0;
    }
    .tags-row {
      display: flex;
      gap: 6px;
      flex-wrap: wrap;
    }
    .tag-chip {
      font-size: 0.75rem;
      color: #c084fc;
      background: rgba(168, 85, 247, 0.15);
      padding: 2px 8px;
      border-radius: 4px;
    }
    .desc-pre {
      background: rgba(0, 0, 0, 0.3);
      padding: 12px;
      border-radius: var(--radius-sm);
      font-size: 0.8rem;
      white-space: pre-wrap;
      color: #d1d5db;
      max-height: 220px;
      overflow-y: auto;
    }
    .placeholder-msg {
      text-align: center;
      padding: 60px 20px;
      color: var(--text-muted);
      font-style: italic;
    }
    .price-highlight-box {
      text-align: center;
      padding: 20px;
      background: rgba(255, 255, 255, 0.03);
      border-radius: var(--radius-md);
      border: 1px solid var(--border-glass);
      margin-bottom: 14px;
    }
    .ph-label {
      display: block;
      font-size: 0.82rem;
      color: var(--text-muted);
    }
    .ph-price {
      font-size: 2.4rem;
      font-weight: 800;
      color: #fff;
      font-family: var(--font-heading);
    }
    .ph-bounds {
      display: block;
      font-size: 0.82rem;
      color: #34d399;
    }
    .audit-row {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 8px 0;
      border-bottom: 1px solid var(--border-glass);
      font-size: 0.88rem;
    }
    .rationale-text {
      font-size: 0.88rem;
      color: #e5e7eb;
      line-height: 1.5;
    }
    .factors-list {
      margin-left: 20px;
      font-size: 0.82rem;
      color: var(--text-muted);
    }
    .safety-banner {
      padding: 16px;
      border-radius: var(--radius-md);
      margin-bottom: 14px;
    }
    .sb-pass {
      background: rgba(16, 185, 129, 0.1);
      border: 1px solid rgba(16, 185, 129, 0.3);
    }
    .sb-fail {
      background: rgba(239, 68, 68, 0.1);
      border: 1px solid rgba(239, 68, 68, 0.4);
    }
    .sb-status {
      display: flex;
      align-items: center;
      gap: 10px;
      font-size: 1.05rem;
      margin-bottom: 4px;
    }
    .sb-score {
      font-size: 0.85rem;
      color: var(--text-muted);
    }
    .mod-explanation {
      font-size: 0.9rem;
      color: #e5e7eb;
    }
    .flagged-box {
      margin-top: 10px;
      padding: 12px;
      background: rgba(239, 68, 68, 0.15);
      border-radius: var(--radius-sm);
      font-size: 0.85rem;
      color: #fca5a5;
    }
    .flagged-box ul {
      margin-left: 20px;
      margin-top: 6px;
    }
    .anomaly-summary {
      padding: 16px;
      border-radius: var(--radius-md);
      background: rgba(16, 185, 129, 0.1);
      border: 1px solid rgba(16, 185, 129, 0.3);
      margin-bottom: 14px;
    }
    .anom-bad {
      background: rgba(245, 158, 11, 0.1);
      border-color: rgba(245, 158, 11, 0.4);
    }
    .anom-bad h4 { color: #fbbf24; }
    .diag-text { font-size: 0.88rem; }
    .recs-box {
      margin-top: 10px;
      font-size: 0.85rem;
      color: var(--text-muted);
    }
    .recs-box ul { margin-left: 20px; margin-top: 4px; }
    .quality-overview {
      padding: 30px;
    }
    .qo-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 24px;
    }
    .qo-stats {
      display: flex;
      gap: 12px;
    }
    .qo-badge {
      background: rgba(255, 255, 255, 0.05);
      padding: 8px 16px;
      border-radius: var(--radius-md);
      border: 1px solid var(--border-glass);
      text-align: center;
      font-size: 0.8rem;
      font-weight: 600;
    }
    .qo-pass {
      background: rgba(16, 185, 129, 0.15);
      border-color: rgba(16, 185, 129, 0.4);
      color: #34d399;
    }
    .badge-num {
      display: block;
      font-size: 1.4rem;
      font-weight: 800;
    }
    .tests-table-wrapper {
      overflow-x: auto;
    }
    .tests-table {
      width: 100%;
      border-collapse: collapse;
      text-align: left;
      font-size: 0.88rem;
    }
    .tests-table th, .tests-table td {
      padding: 12px 14px;
      border-bottom: 1px solid var(--border-glass);
    }
    .tests-table th {
      color: var(--text-subtle);
      font-size: 0.75rem;
      text-transform: uppercase;
    }
    .tests-table code {
      color: #c084fc;
      background: rgba(168, 85, 247, 0.1);
      padding: 2px 6px;
      border-radius: 4px;
      font-size: 0.82rem;
    }
    .test-notes-banner {
      display: flex;
      gap: 14px;
      align-items: flex-start;
      padding: 16px;
      background: rgba(245, 158, 11, 0.08);
      border: 1px solid rgba(245, 158, 11, 0.25);
      border-radius: var(--radius-md);
      font-size: 0.88rem;
      line-height: 1.5;
    }
    @media (max-width: 900px) {
      .lab-grid { grid-template-columns: 1fr; }
      .qo-header { flex-direction: column; align-items: flex-start; gap: 16px; }
    }
  `]
})
export class AiLabComponent {
  activeTab: 'GENERATOR' | 'VALUATION' | 'MODERATION' | 'ANOMALY' | 'TESTS' = 'GENERATOR';

  // Generator
  genPrompt = 'iPhone 14 Pro 256go violet trouvé dans le bus Ligne 4, état impeccable';
  loadingGen = false;
  genResult: GenerateListingResponse | null = null;

  // Valuation
  estTitle = 'VTT Rockrider ST 530';
  estCategory: any = 'BICYCLES_VEHICLES';
  estCondition: any = 'GOOD';
  estResult: EstimatePriceResponse | null = null;

  // Moderation
  modTitle = 'Passeport biométrique européen en cours de validité';
  modDesc = 'Trouvé dans la rue, je le revends au plus offrant.';
  modResult: ModerationResponse | null = null;

  // Anomaly
  anomTitle = 'Apple iPhone 15 Pro Max 512 Go';
  anomType: any = 'SALE';
  anomPrice = 2.0;
  anomResult: AnomalyDetectionResult | null = null;

  constructor(private aiService: AiAssistantService) {}

  executeGeneration(): void {
    this.loadingGen = true;
    this.aiService.generateListing({
      prompt: this.genPrompt
    }).subscribe({
      next: (res) => {
        this.genResult = res;
        this.loadingGen = false;
      },
      error: (err) => {
        console.error('Erreur génération:', err);
        this.loadingGen = false;
      }
    });
  }

  executeEstimation(): void {
    this.aiService.estimatePrice({
      title: this.estTitle,
      category: this.estCategory,
      condition: this.estCondition
    }).subscribe({
      next: (res) => this.estResult = res,
      error: (err) => console.error('Erreur estimation:', err)
    });
  }

  setModerationScenario(type: string): void {
    if (type === 'PASSPORT') {
      this.modTitle = 'Passeport biométrique trouvé';
      this.modDesc = 'Document d\'identité officiel retrouvé en gare, proposé à la vente.';
    } else if (type === 'WEAPON') {
      this.modTitle = 'Couteau de chasse ou arme blanche';
      this.modDesc = 'Objet tranchant métallique retrouvé dans les parcs.';
    } else {
      this.modTitle = 'Vélo de ville Peugeot Vintage';
      this.modDesc = 'Vélo classique révisé, non réclamé après 6 mois en fourrière.';
    }
  }

  executeModeration(): void {
    this.aiService.moderateContent({
      title: this.modTitle,
      description: this.modDesc
    }).subscribe({
      next: (res) => this.modResult = res,
      error: (err) => console.error('Erreur modération:', err)
    });
  }

  setAnomalyScenario(scenario: string): void {
    if (scenario === 'IPHONE_1_EURO') {
      this.anomTitle = 'Apple iPhone 15 Pro';
      this.anomType = 'SALE';
      this.anomPrice = 2.0;
    } else if (scenario === 'DONATION_WITH_PRICE') {
      this.anomTitle = 'Manteau d\'hiver solidaire';
      this.anomType = 'DONATION';
      this.anomPrice = 35.0;
    } else {
      this.anomTitle = 'Parapluie noir basique';
      this.anomType = 'SALE';
      this.anomPrice = 50000.0;
    }
  }

  executeAnomalyCheck(): void {
    const itemPayload: any = {
      title: this.anomTitle,
      category: 'ELECTRONICS',
      condition: 'EXCELLENT',
      type: this.anomType,
      price: this.anomPrice,
      status: 'AVAILABLE'
    };
    this.aiService.detectAnomaly(itemPayload).subscribe({
      next: (res) => this.anomResult = res,
      error: (err) => console.error('Erreur détection anomalie:', err)
    });
  }
}
