import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { ObjetsApiService } from '@valorio/api-objets';
import {
  Categorie,
  DiagnosticObjet,
  EvenementHistorique,
  Objet,
  RecommandationObjet,
  STATUTS_OBJET,
  SessionService,
  StatutObjet,
  formaterDate,
  indexParId,
  libelleStatut,
  messageErreur,
} from '@valorio/shared';

@Component({
  selector: 'valorio-objet-detail',
  imports: [FormsModule, RouterLink],
  template: `
    <section class="page">
      <header class="page-header">
        <div>
          <h1>{{ objet()?.intitule || 'Objet' }} <span class="muted">#{{ objetId }}</span></h1>
          <p>{{ objet()?.description || 'Sans description' }}</p>
          @if (objet(); as o) {
            <p class="muted">
              Catégorie : {{ nomCategorie(o.categorieId) }} · État : {{ libelle(o.etat) }}
              · {{ o.ville || 'Ville inconnue' }}
              @if (o.valeurEstimee != null) {
                · Valeur estimée : {{ o.valeurEstimee }} DT
              }
              @if (o.ageAnnees != null) {
                · Âge : {{ o.ageAnnees }} ans
              }
            </p>
            @if (o.materiaux && o.materiaux.length > 0) {
              <p class="muted">Matériaux : {{ o.materiaux.join(', ') }}</p>
            }
          }
        </div>
        @if (objet(); as o) {
          <div class="toolbar">
            @if (o.statut) {
              <span [class]="'badge ' + o.statut.toLowerCase()">{{ libelle(o.statut) }}</span>
            }
            <button class="btn small danger" type="button" (click)="supprimer()" [disabled]="enCours()">
              Supprimer
            </button>
          </div>
        }
      </header>

      @if (erreur()) {
        <div class="alert error">{{ erreur() }}</div>
      }
      @if (succes()) {
        <div class="alert success">{{ succes() }}</div>
      }

      <div class="card">
        <div class="spread">
          <h2>Photos</h2>
          <div class="toolbar">
            <input type="file" accept="image/*" multiple (change)="onFilesSelected($event)" />
            <button class="btn secondary" type="button" (click)="ajouterPhotos()" [disabled]="enCours()">
              Ajouter
            </button>
          </div>
        </div>
        @if (photos().length === 0) {
          <p class="empty">Aucune photo pour cet objet.</p>
        } @else {
          <div class="grid-cards" style="margin-top:0.9rem">
            @for (p of photos(); track p) {
              <a class="tile" [href]="urlPhoto(p)" target="_blank" rel="noopener">
                <img [src]="urlPhoto(p)" [alt]="objet()?.intitule || 'Photo objet'" style="width:100%" />
              </a>
            }
          </div>
        }
      </div>

      <div class="card">
        <h2>Diagnostic</h2>
        <form class="toolbar" (ngSubmit)="diagnostiquer()">
          <label class="field" style="flex:1">
            <span>Panne déclarée</span>
            <input type="text" name="panneDeclaree" [(ngModel)]="diagnostic.panneDeclaree" placeholder="Ne démarre plus" />
          </label>
          <label class="field" style="flex:1">
            <span>Problèmes observés (séparés par virgule)</span>
            <input type="text" name="problemes" [(ngModel)]="diagnostic.problemes" placeholder="moteur, courroie" />
          </label>
          <button class="btn" type="submit" [disabled]="enCours()">Lancer le diagnostic</button>
        </form>

        @if (diagnostics().length === 0) {
          <p class="empty">Aucun diagnostic enregistré.</p>
        } @else {
          <ul class="timeline" style="margin-top:0.9rem">
            @for (d of diagnostics(); track d.id) {
              <li>
                <span class="when">{{ date(d.date) }}</span>
                <span class="muted">{{ d.panneDeclaree || 'Panne non précisée' }}</span>
                @if (d.problemes && d.problemes.length > 0) {
                  <span class="muted">Problèmes : {{ d.problemes.join(', ') }}</span>
                }
                @if (d.analyse) {
                  <span class="muted">Analyse : {{ d.analyse }}</span>
                }
                @if (d.diagnostic) {
                  <strong>{{ d.diagnostic }}</strong>
                }
              </li>
            }
          </ul>
        }
      </div>

      <div class="card">
        <div class="spread">
          <h2>Recommandation</h2>
          <button class="btn secondary" type="button" (click)="analyser()" [disabled]="enCours()">
            Calculer la recommandation
          </button>
        </div>
        @if (recommandation(); as r) {
          <article class="tile" style="margin-top:0.9rem">
            <div class="spread">
              <h3>{{ r.action }}</h3>
              @if (r.economieEstimee != null) {
                <span class="badge">Économie : {{ r.economieEstimee }} DT</span>
              }
            </div>
            @if (r.justification) {
              <span class="muted">{{ r.justification }}</span>
            }
            @if (r.coutReparationEstime != null) {
              <span class="muted">Coût de réparation estimé : {{ r.coutReparationEstime }} DT</span>
            }
            @if (r.valeurApresReparationEstimee != null) {
              <span class="muted">Valeur après réparation : {{ r.valeurApresReparationEstimee }} DT</span>
            }
          </article>
        } @else {
          <p class="empty">Lance un diagnostic puis calcule la recommandation (réparer, revendre, donner, recycler).</p>
        }
      </div>

      <div class="card">
        <h2>Cycle de vie</h2>
        <form class="toolbar" (ngSubmit)="changerStatut()">
          <label class="field">
            <span>Nouveau statut</span>
            <select name="statut" [(ngModel)]="statutForm.statut" required>
              @for (s of prochainsStatuts(); track s) {
                <option [ngValue]="s">{{ libelle(s) }}</option>
              }
            </select>
          </label>
          <label class="field" style="flex:1">
            <span>Commentaire</span>
            <input type="text" name="commentaire" [(ngModel)]="statutForm.commentaire" />
          </label>
          <button class="btn" type="submit" [disabled]="enCours()">Appliquer</button>
        </form>

        @if (historique().length === 0) {
          <p class="empty">Aucun événement.</p>
        } @else {
          <ul class="timeline" style="margin-top:0.9rem">
            @for (e of historique(); track e.id) {
              <li>
                <span class="when">{{ date(e.date) }}</span>
                <span class="badge">{{ libelle(e.type) }}</span>
                <span class="muted">{{ e.libelle }}</span>
                @if (e.statutCible) {
                  <span class="muted">→ {{ libelle(e.statutCible) }}</span>
                }
              </li>
            }
          </ul>
        }
      </div>

      <a class="btn secondary" routerLink="/objets">Retour aux objets</a>
    </section>
  `,
})
export class ObjetDetail implements OnInit {
  private readonly api = inject(ObjetsApiService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly session = inject(SessionService);

  readonly objet = signal<Objet | null>(null);
  readonly historique = signal<EvenementHistorique[]>([]);
  readonly diagnostics = signal<DiagnosticObjet[]>([]);
  readonly recommandation = signal<RecommandationObjet | null>(null);
  readonly categories = signal<Categorie[]>([]);
  readonly erreur = signal<string | null>(null);
  readonly succes = signal<string | null>(null);
  readonly enCours = signal(false);

  readonly photos = computed(() => this.objet()?.photos ?? []);

  private readonly categoriesIndex = computed(() => indexParId(this.categories()));

  readonly libelle = libelleStatut;
  readonly date = formaterDate;

  objetId = 0;
  photosChoisies: File[] = [];
  diagnostic = { panneDeclaree: '', problemes: '' };
  statutForm: { statut: StatutObjet | ''; commentaire: string } = { statut: '', commentaire: '' };

  ngOnInit(): void {
    this.objetId = Number(this.route.snapshot.paramMap.get('id'));
    this.chargerCategories();
    this.charger();
  }

  nomCategorie(id?: number | null): string {
    if (id == null) {
      return '—';
    }
    return this.categoriesIndex().get(id)?.nom ?? `Catégorie #${id}`;
  }

  urlPhoto(photo: string): string {
    return this.api.urlPhoto(photo);
  }

  prochainsStatuts(): StatutObjet[] {
    const courant = this.objet()?.statut;
    return STATUTS_OBJET.filter((s) => s !== courant);
  }

  onFilesSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.photosChoisies = input.files ? Array.from(input.files) : [];
  }

  chargerCategories(): void {
    this.api.listerCategories().subscribe({
      next: (categories) => this.categories.set(categories),
      error: () => {
        /* catégorie affichée par repli sur l'identifiant */
      },
    });
  }

  charger(): void {
    this.erreur.set(null);
    this.api.getObjet(this.objetId).subscribe({
      next: (objet) => this.objet.set(objet),
      error: (err) => this.erreur.set(messageErreur(err)),
    });
    this.api.historique(this.objetId).subscribe({
      next: (historique) => this.historique.set(historique),
      error: () => {
        /* historique optionnel */
      },
    });
    this.api.listerDiagnostics(this.objetId).subscribe({
      next: (diagnostics) => this.diagnostics.set(diagnostics),
      error: () => {
        /* diagnostics optionnels */
      },
    });
  }

  ajouterPhotos(): void {
    if (this.photosChoisies.length === 0) {
      this.erreur.set('Sélectionne au moins une photo.');
      return;
    }
    this.erreur.set(null);
    this.succes.set(null);
    this.enCours.set(true);
    this.api.ajouterPhotos(this.objetId, this.photosChoisies).subscribe({
      next: (objet) => {
        this.enCours.set(false);
        this.succes.set('Photos ajoutées.');
        this.objet.set(objet);
        this.photosChoisies = [];
        this.chargerHistorique();
      },
      error: (err) => {
        this.enCours.set(false);
        this.erreur.set(messageErreur(err));
      },
    });
  }

  diagnostiquer(): void {
    const problemes = this.diagnostic.problemes
      .split(',')
      .map((p) => p.trim())
      .filter((p) => p.length > 0);
    if (!this.diagnostic.panneDeclaree.trim() && problemes.length === 0) {
      this.erreur.set('Renseigne une panne déclarée ou des problèmes observés.');
      return;
    }
    this.erreur.set(null);
    this.succes.set(null);
    this.enCours.set(true);
    this.api
      .diagnostiquer(this.objetId, {
        panneDeclaree: this.diagnostic.panneDeclaree || undefined,
        problemes: problemes.length > 0 ? problemes : undefined,
      })
      .subscribe({
        next: (diagnostic) => {
          this.enCours.set(false);
          this.succes.set('Diagnostic enregistré.');
          this.diagnostic = { panneDeclaree: '', problemes: '' };
          this.diagnostics.update((liste) => [...liste, diagnostic]);
          this.chargerHistorique();
        },
        error: (err) => {
          this.enCours.set(false);
          this.erreur.set(messageErreur(err));
        },
      });
  }

  analyser(): void {
    this.erreur.set(null);
    this.api.recommandation(this.objetId).subscribe({
      next: (recommandation) => this.recommandation.set(recommandation),
      error: (err) => this.erreur.set(messageErreur(err)),
    });
  }

  changerStatut(): void {
    if (!this.statutForm.statut) {
      this.erreur.set('Sélectionne un statut.');
      return;
    }
    this.erreur.set(null);
    this.succes.set(null);
    this.enCours.set(true);
    this.api
      .changerStatut(
        this.objetId,
        this.statutForm.statut,
        this.statutForm.commentaire || undefined,
        this.session.utilisateurId() ?? undefined,
      )
      .subscribe({
        next: (objet) => {
          this.enCours.set(false);
          this.succes.set('Statut mis à jour.');
          this.objet.set(objet);
          this.statutForm = { statut: '', commentaire: '' };
          this.chargerHistorique();
        },
        error: (err) => {
          this.enCours.set(false);
          this.erreur.set(messageErreur(err));
        },
      });
  }

  supprimer(): void {
    this.erreur.set(null);
    this.enCours.set(true);
    this.api.supprimer(this.objetId).subscribe({
      next: () => {
        this.enCours.set(false);
        this.router.navigate(['/objets']);
      },
      error: (err) => {
        this.enCours.set(false);
        this.erreur.set(messageErreur(err));
      },
    });
  }

  private chargerHistorique(): void {
    this.api.historique(this.objetId).subscribe({
      next: (historique) => this.historique.set(historique),
      error: () => {
        /* historique optionnel */
      },
    });
  }
}
