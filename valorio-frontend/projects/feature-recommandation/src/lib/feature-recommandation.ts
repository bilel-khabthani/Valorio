import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { RecommandationApiService, RecoValorisationResponse, RecoActeursResponse, RecoMarcheResponse, ModeleMetadata, LIBELLE_ROUTE, libelleEquite } from '@valorio/api-recommandation';
import { ReparationApiService } from '@valorio/api-reparation';
import { Objet, messageErreur } from '@valorio/shared';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'valorio-feature-recommandation',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  template: `
    <section class="page">
      <header class="page-header">
        <div>
          <h1>Recommandation</h1>
          <p>Valorisation intelligente, acteurs pertinents et équité de marché</p>
        </div>
      </header>

      @if (erreur()) {
        <div class="alert error">{{ erreur() }}</div>
      }
      @if (succes()) {
        <div class="alert success">{{ succes() }}</div>
      }

      <div class="tabs">
        <button class="tab" [class.active]="tab() === 'valorisation'" (click)="tab.set('valorisation')">Valorisation</button>
        <button class="tab" [class.active]="tab() === 'acteurs'" (click)="tab.set('acteurs')">Acteurs</button>
        <button class="tab" [class.active]="tab() === 'marche'" (click)="tab.set('marche')">Marché</button>
      </div>

      <div class="card">
        @if (tab() === 'valorisation') {
          <h2>Recommander une route de valorisation</h2>
          <form class="toolbar" (ngSubmit)="submettreValorisation()">
            <label class="field">
              <span>Objet</span>
              <select name="objetId" [(ngModel)]="objetId" (ngModelChange)="prefillObjet()">
                <option [ngValue]="0">Sélectionner un objet</option>
                @for (o of objets(); track o.id) {
                  <option [ngValue]="o.id">{{ o.intitule }}{{ o.ville ? ' — ' + o.ville : '' }}</option>
                }
              </select>
            </label>
            <label class="field">
              <span>Coût de réparation estimé (€)</span>
              <input type="number" name="coutReparationEstime" [(ngModel)]="coutReparationEstime" step="0.01" min="0" />
            </label>
            <button class="btn" type="submit" [disabled]="chargement()">Recommander @if (chargement()) { <span class="spinner"></span> }</button>
          </form>

          @if (objetSelectionne()) {
            <div class="muted" style="margin-top:0.8rem">
              <strong>Résumé</strong> : {{ objetSelectionne()!.intitule }} | Catégorie: {{ objetSelectionne()!.categorieId }} | État: {{ objetSelectionne()!.etat }} | Âge: {{ objetSelectionne()!.ageAnnees ?? '—' }} | Valeur: {{ objetSelectionne()!.valeurEstimee ?? '—' }}€ | Ville: {{ objetSelectionne()!.ville ?? '—' }} | Matériaux: {{ (objetSelectionne()!.materiaux ?? []).join(', ') }}
            </div>
          }

          @if (resultatValorisation(); as r) {
            <div class="card" style="margin-top:0.9rem">
              <div class="spread">
                <div>
                  <h3>Route recommandée</h3>
                  <span [class]="'badge ' + r.routeRecommandee.toLowerCase()">{{ LIBELLE_ROUTE[r.routeRecommandee] }}</span>
                  <span class="muted">Probabilité : {{ (r.probabilite * 100).toFixed(1) }}% · Source : <span [class]="'badge ' + r.source.toLowerCase()">{{ r.source }}</span></span>
                </div>
                <div class="actions">
                  @if (r.routeRecommandee === 'REPARATION') {
                    <a class="btn" routerLink="/reparation">Appliquer</a>
                  } @if (r.routeRecommandee === 'DON') {
                    <a class="btn" routerLink="/don">Appliquer</a>
                  } @if (r.routeRecommandee === 'RECYCLAGE') {
                    <a class="btn" routerLink="/recyclage">Appliquer</a>
                  } @if (r.routeRecommandee === 'REVENTE') {
                    <button class="btn" type="button" (click)="informerRevente()">Appliquer</button>
                  }
                </div>
              </div>
              <div class="progress" style="margin-top:0.6rem"><div class="progress-bar" [style.width.%]="r.probabilite * 100"></div></div>
              @if (r.raisons && r.raisons.length > 0) {
                <div style="margin-top:0.8rem">
                  <strong>Raisons</strong>
                  <ul>
                    @for (ra of r.raisons; track ra.libelle) {
                      <li>{{ ra.libelle }} @if (ra.contribution != null) { ({{ (ra.contribution*100).toFixed(1) }}%) }</li>
                    }
                  </ul>
                </div>
              }
              @if (r.alternatives && r.alternatives.length > 0) {
                <div style="margin-top:0.8rem">
                  <strong>Alternatives</strong>
                  <ul>
                    @for (alt of r.alternatives; track alt.route) {
                      <li>{{ LIBELLE_ROUTE[alt.route] }} : {{ (alt.probabilite * 100).toFixed(1) }}%</li>
                    }
                  </ul>
                </div>
              }
              <div style="margin-top:0.8rem">
                <button class="btn secondary" type="button" [disabled]="feedbackDonne()" (click)="donnerFeedback(true)">👍 Utile</button>
                <button class="btn secondary" type="button" [disabled]="feedbackDonne()" (click)="donnerFeedback(false)" style="margin-left:0.4rem">👎 Pas utile</button>
              </div>
            </div>
          }
        }

        @if (tab() === 'acteurs') {
          <h2>Classer les acteurs</h2>
          <form class="toolbar" (ngSubmit)="submettreActeurs()">
            <label class="field">
              <span>Type d'acteur</span>
              <select name="typeActeur" [(ngModel)]="typeActeur">
                <option value="REPARATEUR">Réparateur</option>
                <option value="BENEFICIAIRE">Bénéficiaire</option>
                <option value="CENTRE_RECYCLAGE">Centre de recyclage</option>
              </select>
            </label>
            <label class="field">
              <span>Rayon (km)</span>
              <input type="number" name="rayonKm" [(ngModel)]="rayonKm" min="1" />
            </label>
            <label class="field">
              <span>Objet</span>
              <select name="objetIdActeurs" [(ngModel)]="objetId">
                <option [ngValue]="0">Facultatif</option>
                @for (o of objets(); track o.id) {
                  <option [ngValue]="o.id">{{ o.intitule }}</option>
                }
              </select>
            </label>
            <button class="btn" type="submit" [disabled]="chargement()">Rechercher</button>
          </form>
          @if (resultatActeurs(); as ra) {
            <table class="table" style="margin-top:0.9rem">
              <thead><tr><th>#</th><th>Candidat</th><th>Score</th><th>Acceptation</th><th>Délai (j)</th><th>Distance (km)</th><th>Raisons</th></tr></thead>
              <tbody>
                @for (c of ra.classement; track c.candidatId; let i = $index) {
                  <tr>
                    <td>{{ i+1 }}</td>
                    <td>{{ c.candidatId }} @if (c.distanceKm != null) { — {{ c.distanceKm?.toFixed(1) ?? '—' }} }</td>
                    <td><div class="progress"><div class="progress-bar" [style.width.%]="c.score * 100"></div></div>{{ (c.score*100).toFixed(0) }}%</td>
                    <td>{{ (c.probabiliteAcceptation*100).toFixed(1) }}%</td>
                    <td>{{ c.delaiJoursEstime ?? '—' }}</td>
                    <td>{{ c.distanceKm?.toFixed(1) ?? '—' }}</td>
                    <td><ul><li *ngFor="let r of c.raisons">{{ r }}</li></ul></td>
                  </tr>
                }
              </tbody>
            </table>
          }
        }

        @if (tab() === 'marche') {
          <h2>Articles similaires et équité de prix</h2>
          <form class="toolbar" (ngSubmit)="submettreMarche()">
            <label class="field"><span>Titre</span><input name="titre" [(ngModel)]="reqMarche.titre" /></label>
            <label class="field"><span>Description</span><input name="description" [(ngModel)]="reqMarche.description" /></label>
            <label class="field"><span>Catégorie</span><input name="categorie" [(ngModel)]="reqMarche.categorie" /></label>
            <label class="field"><span>État</span><select name="etat" [(ngModel)]="reqMarche.etat"><option value="NEUF">NEUF</option><option value="EXCELLENT">EXCELLENT</option><option value="BON">BON</option><option value="ENDOMMAGE">ENDOMMAGE</option><option value="HORS_SERVICE">HORS_SERVICE</option></select></label>
            <label class="field"><span>Prix</span><input type="number" name="prix" [(ngModel)]="reqMarche.prix" step="0.01" /></label>
            <label class="field"><span>Top K</span><input type="number" name="topK" [(ngModel)]="topK" min="1" max="20" /></label>
            <button class="btn" type="submit" [disabled]="chargement()">Rechercher</button>
          </form>
          @if (resultatMarche(); as rm) {
            <table class="table" style="margin-top:0.9rem">
              <thead><tr><th>Titre</th><th>Similarité</th><th>Score deal</th><th>Prix</th><th>Prix recommandé</th><th>Équité</th><th>Raisons</th></tr></thead>
              <tbody>
                @for (a of rm.articles; track a.articleId) {
                  <tr>
                    <td>{{ a.titre }}</td>
                    <td>{{ (a.similarite*100).toFixed(1) }}%</td>
                    <td><div class="progress"><div class="progress-bar" [style.width.%]="a.scoreDeal*100"></div></div></td>
                    <td>{{ a.prix ?? '—' }}€</td>
                    <td>{{ a.prixRecommande ?? '—' }}€</td>
                    <td><span [class]="'badge ' + a.equitePrix.toLowerCase()">{{ libelleEquite(a.equitePrix) }}</span></td>
                    <td><ul><li *ngFor="let r of a.raisons">{{ r }}</li></ul></td>
                  </tr>
                }
              </tbody>
            </table>
          }
        }
      </div>

      <div class="card">
        <h2>Modèle — transparence</h2>
        @if (metadata(); as m) {
          <div>
            <span [class]="'badge ' + (m.actif ? 'ml' : 'heuristique')">{{ m.actif ? 'Moteur ML actif' : 'Mode heuristique' }}</span>
            <span class="muted">Source: {{ m.source }}</span>
          </div>
          <div class="muted" style="margin-top:0.5rem">Versions: Valorisation {{ m.versionValorisation ?? '—' }} · Acteurs {{ m.versionActeurs ?? '—' }} · Marché {{ m.versionMarche ?? '—' }}</div>
          @if (m.metriques) {
            <div style="margin-top:0.5rem">
              @for (kv of metriquesEntries(); track kv[0]) {
                <span class="chip">{{ kv[0] }}: {{ kv[1].toFixed(2) }}</span>
              }
            </div>
          }
          @if (m.topFeatures && m.topFeatures.length > 0) {
            <div style="margin-top:0.5rem">
              <strong>Top features</strong>: @for (f of m.topFeatures; track f) { <span class="chip">{{ f }}</span> }
            </div>
          }
        } @else {
          <p class="muted">Chargement...</p>
        }
      </div>
    </section>
  `,
  styleUrls: ['./feature-recommandation.css']
})
export class FeatureRecommandationComponent implements OnInit {
  private readonly api = inject(RecommandationApiService);
  private readonly apiRep = inject(ReparationApiService);
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);

  readonly tab = signal<'valorisation'|'acteurs'|'marche'>('valorisation');
  readonly objets = signal<Objet[]>([]);
  readonly objetId = signal<number>(0);
  readonly coutReparationEstime = signal<number | null>(null);
  readonly chargement = signal(false);
  readonly erreur = signal<string|null>(null);
  readonly succes = signal<string|null>(null);

  readonly resultatValorisation = signal<RecoValorisationResponse|null>(null);
  readonly feedbackDonne = signal(false);

  readonly typeActeur = signal<'REPARATEUR'|'BENEFICIAIRE'|'CENTRE_RECYCLAGE'>('REPARATEUR');
  readonly rayonKm = signal<number>(100);
  readonly resultatActeurs = signal<RecoActeursResponse|null>(null);

  readonly reqMarche = { titre: '', description: '', categorie: '', etat: 'BON', prix: 0 };
  readonly topK = signal<number>(5);
  readonly resultatMarche = signal<RecoMarcheResponse|null>(null);

  readonly metadata = signal<ModeleMetadata|null>(null);
  readonly LIBELLE_ROUTE = LIBELLE_ROUTE;
  readonly libelleEquite = libelleEquite;

  ngOnInit(): void {
    this.chargerObjets();
    this.chargerMetadata();
  }

  get metriquesEntries() {
    return () => Object.entries(this.metadata()?.metriques ?? {});
  }

  objetSelectionne(): Objet | undefined {
    return this.objets().find(o => o.id === this.objetId());
  }

  prefillObjet(): void {
    const o = this.objetSelectionne();
    if (o && this.coutReparationEstime() == null) {
      // keep as is
    }
  }

  chargerObjets(): void {
    this.http.get<Objet[]>('/rep/api/objets').subscribe({
      next: (objs) => this.objets.set(objs),
      error: (err) => this.erreur.set(messageErreur(err))
    });
  }

  chargerMetadata(): void {
    this.api.metadataModele().subscribe({
      next: (m) => this.metadata.set(m),
      error: (err) => this.erreur.set(messageErreur(err))
    });
  }

  submettreValorisation(): void {
    this.erreur.set(null);
    this.succes.set(null);
    this.chargement.set(true);
    const o = this.objetSelectionne();
    this.api.recommanderValorisation({
      objetId: o ? undefined : this.objetId() || undefined,
      objet: o ? {
        categorieId: o.categorieId,
        intitule: o.intitule,
        etat: o.etat as any,
        ageAnnees: o.ageAnnees,
        valeurEstimee: o.valeurEstimee,
        coutReparationEstime: this.coutReparationEstime() ?? undefined,
        ville: o.ville,
        materiaux: o.materiaux
      } : undefined
    }).subscribe({
      next: (r) => {
        this.resultatValorisation.set(r);
        this.feedbackDonne.set(false);
        this.chargement.set(false);
      },
      error: (err) => {
        this.erreur.set(messageErreur(err));
        this.chargement.set(false);
      }
    });
  }

  informerRevente(): void {
    alert('Disponible via le module Marketplace (http://localhost:4200)');
  }

  donnerFeedback(utile: boolean): void {
    const r = this.resultatValorisation();
    if (!r) return;
    this.api.envoyerFeedback({ recommandationId: r.id, utile }).subscribe({
      next: () => {
        this.succes.set('Feedback enregistré');
        this.feedbackDonne.set(true);
      },
      error: (err) => this.erreur.set(messageErreur(err))
    });
  }

  submettreActeurs(): void {
    this.erreur.set(null);
    this.chargement.set(true);
    const o = this.objetSelectionne();
    this.api.recommanderActeurs({
      typeActeur: this.typeActeur(),
      rayonKm: this.rayonKm(),
      objet: o ? { intitule: o.intitule, etat: o.etat as any, ville: o.ville } : undefined
    }).subscribe({
      next: (r) => {
        this.resultatActeurs.set(r);
        this.chargement.set(false);
      },
      error: (err) => {
        this.erreur.set(messageErreur(err));
        this.chargement.set(false);
      }
    });
  }

  submettreMarche(): void {
    this.erreur.set(null);
    this.chargement.set(true);
    this.api.articlesSimilaires({
      requete: {
        titre: this.reqMarche.titre,
        description: this.reqMarche.description,
        categorie: this.reqMarche.categorie,
        etat: this.reqMarche.etat,
        prix: this.reqMarche.prix
      },
      topK: this.topK()
    }).subscribe({
      next: (r) => {
        this.resultatMarche.set(r);
        this.chargement.set(false);
      },
      error: (err) => {
        this.erreur.set(messageErreur(err));
        this.chargement.set(false);
      }
    });
  }
}
