import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ObjetsApiService } from '@valorio/api-objets';
import {
  Categorie,
  DashboardStats,
  ETATS_OBJET,
  EtatObjet,
  Objet,
  SessionService,
  StatutObjet,
  Utilisateur,
  formaterDate,
  indexParId,
  labelUtilisateur,
  libelleStatut,
  messageErreur,
} from '@valorio/shared';

@Component({
  selector: 'valorio-feature-objets',
  imports: [FormsModule, RouterLink],
  template: `
    <section class="page">
      <header class="page-header">
        <div>
          <h1>Objets</h1>
          <p>Déclaration, catégorisation, diagnostic et cycle de vie des objets.</p>
        </div>
      </header>

      @if (erreur()) {
        <div class="alert error">{{ erreur() }}</div>
      }
      @if (succes()) {
        <div class="alert success">{{ succes() }}</div>
      }

      <div class="grid-cards">
        <article class="tile">
          <span class="muted">Objets déclarés</span>
          <h3>{{ stats()?.totalObjets ?? objets().length }}</h3>
        </article>
        <article class="tile">
          <span class="muted">Valeur estimée cumulée</span>
          <h3>{{ stats()?.totalValeurEstimee ?? 0 }} DT</h3>
        </article>
        <article class="tile">
          <span class="muted">Diagnostics</span>
          <h3>{{ stats()?.totalDiagnostics ?? 0 }}</h3>
        </article>
        <article class="tile">
          <span class="muted">Événements</span>
          <h3>{{ stats()?.totalEvenements ?? 0 }}</h3>
        </article>
      </div>

      <div class="card">
        <div class="spread">
          <h2>{{ estAdmin() ? 'Vue administrateur' : 'Déclarer un objet' }}</h2>
        </div>
        <form class="toolbar" (ngSubmit)="declarer()">
          <label class="field" style="flex:1">
            <span>Intitulé</span>
            <input type="text" name="intitule" [(ngModel)]="nouvelle.intitule" placeholder="Machine à laver" required />
          </label>
          <label class="field">
            <span>Catégorie</span>
            <select name="categorieId" [(ngModel)]="nouvelle.categorieId" required>
              @for (c of categories(); track c.id) {
                <option [ngValue]="c.id">{{ c.nom }}</option>
              }
            </select>
          </label>
          <label class="field">
            <span>État</span>
            <select name="etat" [(ngModel)]="nouvelle.etat">
              @for (e of etats; track e) {
                <option [ngValue]="e">{{ libelle(e) }}</option>
              }
            </select>
          </label>
          <label class="field">
            <span>Âge (années)</span>
            <input type="number" name="ageAnnees" [(ngModel)]="nouvelle.ageAnnees" min="0" />
          </label>
          <label class="field">
            <span>Valeur estimée (DT)</span>
            <input type="number" name="valeurEstimee" [(ngModel)]="nouvelle.valeurEstimee" min="0" step="any" />
          </label>
          <label class="field">
            <span>Ville</span>
            <input type="text" name="ville" [(ngModel)]="nouvelle.ville" />
          </label>
          <label class="field" style="flex:1">
            <span>Matériaux (séparés par virgule)</span>
            <input type="text" name="materiaux" [(ngModel)]="nouvelle.materiaux" placeholder="acier, plastique" />
          </label>
          @if (estAdmin()) {
            <label class="field">
              <span>Propriétaire</span>
              <select name="proprietaireId" [(ngModel)]="nouvelle.proprietaireId" required>
                @for (u of utilisateurs(); track u.id) {
                  <option [ngValue]="u.id">{{ optionUtilisateur(u) }}</option>
                }
              </select>
            </label>
          }
          <label class="field" style="flex:1">
            <span>Description</span>
            <input type="text" name="description" [(ngModel)]="nouvelle.description" placeholder="État, dimensions..." />
          </label>
          <button class="btn" type="submit" [disabled]="enCours()">Déclarer</button>
        </form>
      </div>

      <div class="card">
        <div class="spread">
          <h2>Registre des objets</h2>
          <div class="toolbar">
            <label class="field">
              <span>Catégorie</span>
              <select name="fCategorie" [(ngModel)]="filtre.categorieId">
                <option [ngValue]="undefined">Toutes</option>
                @for (c of categories(); track c.id) {
                  <option [ngValue]="c.id">{{ c.nom }}</option>
                }
              </select>
            </label>
            <label class="field">
              <span>État</span>
              <select name="fEtat" [(ngModel)]="filtre.etat">
                <option [ngValue]="undefined">Tous</option>
                @for (e of etats; track e) {
                  <option [ngValue]="e">{{ libelle(e) }}</option>
                }
              </select>
            </label>
            <label class="field">
              <span>Statut</span>
              <select name="fStatut" [(ngModel)]="filtre.statut">
                <option [ngValue]="undefined">Tous</option>
                @for (s of statuts; track s) {
                  <option [ngValue]="s">{{ libelle(s) }}</option>
                }
              </select>
            </label>
            <label class="field">
              <span>Ville</span>
              <input type="text" name="fVille" [(ngModel)]="filtre.ville" />
            </label>
            <label class="field" style="flex:1">
              <span>Recherche</span>
              <input type="text" name="fQ" [(ngModel)]="filtre.q" placeholder="Intitulé, description" />
            </label>
            <button class="btn secondary" type="button" (click)="chargerObjets()">Filtrer</button>
          </div>
        </div>

        @if (objets().length === 0) {
          <p class="empty">Aucun objet.</p>
        } @else {
          <div class="grid-cards" style="margin-top:0.9rem">
            @for (o of objets(); track o.id) {
              <article class="tile">
                <div class="spread">
                  <h3>{{ o.intitule }}</h3>
                  @if (o.statut) {
                    <span [class]="'badge ' + o.statut.toLowerCase()">{{ libelle(o.statut) }}</span>
                  }
                </div>
                <span class="muted">Catégorie : {{ nomCategorie(o.categorieId) }}</span>
                <span class="muted">État : {{ libelle(o.etat) }}</span>
                @if (o.valeurEstimee != null) {
                  <span class="muted">Valeur estimée : {{ o.valeurEstimee }} DT</span>
                }
                <span class="muted">{{ o.ville || 'Ville inconnue' }}</span>
                @if (o.proprietaireId != null) {
                  <span class="muted">Propriétaire : {{ nomUtilisateur(o.proprietaireId) }}</span>
                }
                <span class="muted">Déclaré le {{ date(o.dateDeclaration) }}</span>
                <a class="btn small secondary" [routerLink]="['objet', o.id]">Détail & diagnostic</a>
              </article>
            }
          </div>
        }
      </div>
    </section>
  `,
})
export class FeatureObjetsHome implements OnInit {
  private readonly api = inject(ObjetsApiService);
  private readonly session = inject(SessionService);

  readonly objets = signal<Objet[]>([]);
  readonly categories = signal<Categorie[]>([]);
  readonly utilisateurs = signal<Utilisateur[]>([]);
  readonly stats = signal<DashboardStats | null>(null);
  readonly erreur = signal<string | null>(null);
  readonly succes = signal<string | null>(null);
  readonly enCours = signal(false);

  protected readonly estAdmin = this.session.estAdmin;

  readonly etats = ETATS_OBJET;
  readonly statuts: StatutObjet[] = [
    'DECLARE',
    'DISPONIBLE',
    'EN_REPARATION',
    'REPARE',
    'VENDU',
    'DONNE',
    'RECYCLE',
  ];

  private readonly categoriesIndex = computed(() => indexParId(this.categories()));
  private readonly utilisateursIndex = computed(() => indexParId(this.utilisateurs()));

  readonly libelle = libelleStatut;
  readonly date = formaterDate;

  filtre = {
    categorieId: undefined as number | undefined,
    etat: undefined as EtatObjet | undefined,
    statut: undefined as StatutObjet | undefined,
    ville: '',
    q: '',
  };

  nouvelle = {
    intitule: '',
    description: '',
    categorieId: 0,
    etat: 'BON' as EtatObjet,
    ageAnnees: undefined as number | undefined,
    valeurEstimee: undefined as number | undefined,
    ville: 'Tunis',
    materiaux: '',
    proprietaireId: 0,
  };

  ngOnInit(): void {
    this.chargerReferentiels();
    this.chargerObjets();
    this.chargerStats();
  }

  optionUtilisateur(utilisateur: Utilisateur): string {
    const nom = labelUtilisateur(utilisateur);
    return utilisateur.ville ? `${nom} — ${utilisateur.ville}` : nom;
  }

  nomCategorie(id?: number | null): string {
    if (id == null) {
      return '—';
    }
    return this.categoriesIndex().get(id)?.nom ?? `Catégorie #${id}`;
  }

  nomUtilisateur(id?: number | null): string {
    if (id == null) {
      return '—';
    }
    return labelUtilisateur(this.utilisateursIndex().get(id), id);
  }

  chargerReferentiels(): void {
    this.api.listerCategories().subscribe({
      next: (categories) => {
        this.categories.set(categories);
        if (!this.nouvelle.categorieId && categories.length > 0) {
          this.nouvelle.categorieId = categories[0].id;
        }
      },
      error: (err) => this.erreur.set(messageErreur(err)),
    });
    this.api.listerUtilisateurs().subscribe({
      next: (utilisateurs) => {
        this.utilisateurs.set(utilisateurs);
        if (!this.nouvelle.proprietaireId && utilisateurs.length > 0) {
          this.nouvelle.proprietaireId = utilisateurs[0].id;
        }
      },
      error: () => {
        /* annuaire indisponible : la saisie manuelle du propriétaire reste possible en admin */
      },
    });
  }

  chargerObjets(): void {
    this.erreur.set(null);
    this.api
      .listerObjets({
        categorieId: this.filtre.categorieId,
        etat: this.filtre.etat,
        statut: this.filtre.statut,
        ville: this.filtre.ville || undefined,
        q: this.filtre.q || undefined,
      })
      .subscribe({
        next: (objets) => this.objets.set(objets),
        error: (err) => this.erreur.set(messageErreur(err)),
      });
  }

  chargerStats(): void {
    this.api.stats().subscribe({
      next: (stats) => this.stats.set(stats),
      error: () => {
        /* les statistiques sont optionnelles */
      },
    });
  }

  declarer(): void {
    this.erreur.set(null);
    this.succes.set(null);
    if (!this.nouvelle.intitule.trim()) {
      this.erreur.set("L'intitulé est obligatoire.");
      return;
    }
    const proprietaireId = this.estAdmin() ? this.nouvelle.proprietaireId : this.session.utilisateurId();
    if (!this.nouvelle.categorieId || !proprietaireId) {
      this.erreur.set('Sélectionne une catégorie et un propriétaire.');
      return;
    }
    this.enCours.set(true);
    this.api
      .declarer({
        intitule: this.nouvelle.intitule,
        description: this.nouvelle.description || undefined,
        categorieId: this.nouvelle.categorieId,
        etat: this.nouvelle.etat,
        ageAnnees: this.nouvelle.ageAnnees,
        valeurEstimee: this.nouvelle.valeurEstimee,
        ville: this.nouvelle.ville || undefined,
        materiaux: this.materiaux(),
        proprietaireId,
      })
      .subscribe({
        next: (objet) => {
          this.enCours.set(false);
          this.succes.set(`Objet #${objet.id} « ${objet.intitule} » déclaré.`);
          this.nouvelle = {
            ...this.nouvelle,
            intitule: '',
            description: '',
            materiaux: '',
            ageAnnees: undefined,
            valeurEstimee: undefined,
          };
          this.chargerObjets();
          this.chargerStats();
        },
        error: (err) => {
          this.enCours.set(false);
          this.erreur.set(messageErreur(err));
        },
      });
  }

  private materiaux(): string[] | undefined {
    const liste = this.nouvelle.materiaux
      .split(',')
      .map((m) => m.trim())
      .filter((m) => m.length > 0);
    return liste.length > 0 ? liste : undefined;
  }
}
