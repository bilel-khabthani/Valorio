import { Component, OnInit, computed, effect, inject, signal, untracked } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ReparationApiService, FiltreDemandes } from '@valorio/api-reparation';
import {
  DemandeReparation,
  Objet,
  Reparateur,
  STATUTS_DEMANDE,
  SessionService,
  StatutDemande,
  Utilisateur,
  formaterDate,
  indexParId,
  labelObjet,
  labelReparateur,
  labelUtilisateur,
  libelleStatut,
  messageErreur,
} from '@valorio/shared';

@Component({
  selector: 'valorio-feature-reparation',
  imports: [FormsModule, RouterLink],
  template: `
    <section class="page">
      <header class="page-header">
        <div>
          <h1>Réparation</h1>
          <p>Recherche de réparateur, demande, devis et suivi d'intervention.</p>
        </div>
      </header>

      @if (erreur()) {
        <div class="alert error">{{ erreur() }}</div>
      }
      @if (succes()) {
        <div class="alert success">{{ succes() }}</div>
      }

      @if (espaceDemandeur()) {
        <div class="role-section">
          <h2 class="section-title">
            {{ estAdmin() ? 'Vue administrateur' : 'Espace utilisateur — demandeur' }}
          </h2>

          <div class="card">
            <h2>Nouvelle demande de réparation</h2>
            <form class="toolbar" (ngSubmit)="creerDemande()">
              <label class="field">
                <span>Objet à réparer</span>
                <select name="objetId" [(ngModel)]="nouveau.objetId" required>
                  @for (o of objets(); track o.id) {
                    <option [ngValue]="o.id">{{ optionObjet(o) }}</option>
                  }
                </select>
              </label>
              @if (estAdmin()) {
                <label class="field">
                  <span>Demandeur</span>
                  <select name="utilisateurId" [(ngModel)]="nouveau.utilisateurId" required>
                    @for (u of utilisateurs(); track u.id) {
                      <option [ngValue]="u.id">{{ optionUtilisateur(u) }}</option>
                    }
                  </select>
                </label>
              } @else {
                <label class="field">
                  <span>Demandeur</span>
                  <span class="muted">{{ session.label() }}</span>
                </label>
              }
              <label class="field" style="flex:1">
                <span>Description de la panne</span>
                <input
                  type="text"
                  name="descriptionPanne"
                  [(ngModel)]="nouveau.descriptionPanne"
                  placeholder="Ex. écran cassé, batterie usée..."
                  required
                />
              </label>
              <button class="btn" type="submit" [disabled]="enCours()">Créer la demande</button>
            </form>
          </div>

          <div class="card">
            <h2>Rechercher un réparateur</h2>
            <form class="toolbar" (ngSubmit)="chargerReparateurs()">
              <label class="field">
                <span>Ville</span>
                <input
                  type="text"
                  name="villeReparateur"
                  [(ngModel)]="villeReparateur"
                  placeholder="Tunis"
                />
              </label>
              <label class="field">
                <span>Compétence</span>
                <input
                  type="text"
                  name="competenceReparateur"
                  [(ngModel)]="competenceReparateur"
                  placeholder="électronique"
                />
              </label>
              <button class="btn secondary" type="submit">Rechercher</button>
            </form>

            @if (reparateurs().length === 0) {
              <p class="empty">Aucun réparateur pour ces critères.</p>
            } @else {
              <div class="grid-cards" style="margin-top:0.9rem">
                @for (r of reparateurs(); track r.id) {
                  <article class="tile">
                    <div class="spread">
                      <h3>{{ labelReparateur(r) }}</h3>
                      <span class="badge" [class.disponible]="r.disponible">{{
                        r.disponible ? 'Disponible' : 'Indisponible'
                      }}</span>
                    </div>
                    <span class="muted">{{ r.ville || 'Ville non précisée' }}</span>
                    <span class="muted">{{ r.competences || 'Compétences non précisées' }}</span>
                    <span class="muted">Note {{ r.noteMoyenne }} / 5 ({{ r.nbAvis }} avis)</span>
                  </article>
                }
              </div>
            }
          </div>

          <div class="card">
            <div class="spread">
              <h2>{{ estAdmin() ? 'Toutes les demandes' : 'Mes demandes' }}</h2>
              @if (estAdmin()) {
                <div class="toolbar">
                  <label class="field">
                    <span>Filtrer par statut</span>
                    <select name="filtreStatut" [(ngModel)]="filtreStatut" (ngModelChange)="chargerDemandes()">
                      <option value="">Tous</option>
                      @for (s of statuts; track s) {
                        <option [value]="s">{{ libelle(s) }}</option>
                      }
                    </select>
                  </label>
                  <label class="field">
                    <span>Filtrer par réparateur</span>
                    <select name="filtreReparateurId" [(ngModel)]="filtreReparateurId" (ngModelChange)="chargerDemandes()">
                      <option [ngValue]="undefined">Tous</option>
                      @for (r of reparateurs(); track r.id) {
                        <option [ngValue]="r.id">{{ labelReparateur(r) }}</option>
                      }
                    </select>
                  </label>
                </div>
              }
            </div>

            @if (demandes().length === 0) {
              <p class="empty">Aucune demande enregistrée.</p>
            } @else {
              <table>
                <thead>
                  <tr>
                    <th>#</th>
                    <th>Objet</th>
                    @if (estAdmin()) {
                      <th>Demandeur</th>
                    }
                    <th>Panne</th>
                    <th>Réparateur</th>
                    <th>Statut</th>
                    <th>Créée le</th>
                    <th></th>
                  </tr>
                </thead>
                <tbody>
                  @for (d of demandes(); track d.id) {
                    <tr>
                      <td>{{ d.id }}</td>
                      <td>{{ nomObjet(d.objetId) }}</td>
                      @if (estAdmin()) {
                        <td>{{ nomUtilisateur(d.utilisateurId) }}</td>
                      }
                      <td>{{ d.descriptionPanne }}</td>
                      <td>{{ nomReparateur(d.reparateurId) }}</td>
                      <td>
                        <span [class]="'badge ' + d.statut.toLowerCase()">{{ libelle(d.statut) }}</span>
                      </td>
                      <td>{{ date(d.dateCreation) }}</td>
                      <td>
                        <a class="btn small secondary" [routerLink]="['demandes', d.id]">Ouvrir</a>
                      </td>
                    </tr>
                  }
                </tbody>
              </table>
            }
          </div>
        </div>
      }

      @if (espaceReparateur()) {
        <div class="role-section">
          <h2 class="section-title">Espace réparateur</h2>
          <div class="card">
            <h2>Demandes qui me sont assignées</h2>
            @if (demandes().length === 0) {
              <p class="empty">Aucune demande ne vous est assignée.</p>
            } @else {
              <table>
                <thead>
                  <tr>
                    <th>#</th>
                    <th>Objet</th>
                    <th>Demandeur</th>
                    <th>Panne</th>
                    <th>Statut</th>
                    <th>Créée le</th>
                    <th></th>
                  </tr>
                </thead>
                <tbody>
                  @for (d of demandes(); track d.id) {
                    <tr>
                      <td>{{ d.id }}</td>
                      <td>{{ nomObjet(d.objetId) }}</td>
                      <td>{{ nomUtilisateur(d.utilisateurId) }}</td>
                      <td>{{ d.descriptionPanne }}</td>
                      <td>
                        <span [class]="'badge ' + d.statut.toLowerCase()">{{ libelle(d.statut) }}</span>
                      </td>
                      <td>{{ date(d.dateCreation) }}</td>
                      <td>
                        <a class="btn small secondary" [routerLink]="['demandes', d.id]">Traiter</a>
                      </td>
                    </tr>
                  }
                </tbody>
              </table>
            }
          </div>
        </div>
      }

      @if (horsRole()) {
        <div class="card">
          <p class="empty">
            Cette page concerne les rôles <strong>Utilisateur</strong> (déposer/ suivre une demande) et
            <strong>Réparateur</strong> (traiter les demandes). Changez de rôle dans la barre du haut.
          </p>
        </div>
      }
    </section>
  `,
})
export class FeatureReparationHome implements OnInit {
  private readonly api = inject(ReparationApiService);
  protected readonly session = inject(SessionService);

  readonly demandes = signal<DemandeReparation[]>([]);
  readonly reparateurs = signal<Reparateur[]>([]);
  readonly objets = signal<Objet[]>([]);
  readonly utilisateurs = signal<Utilisateur[]>([]);
  readonly erreur = signal<string | null>(null);
  readonly succes = signal<string | null>(null);
  readonly enCours = signal(false);

  protected readonly estAdmin = this.session.estAdmin;
  protected readonly espaceDemandeur = computed(() => this.session.est('UTILISATEUR', 'ADMIN'));
  protected readonly espaceReparateur = computed(() => this.session.est('REPARATEUR'));
  protected readonly horsRole = computed(
    () => !this.espaceDemandeur() && !this.espaceReparateur(),
  );

  private readonly objetsIndex = computed(() => indexParId(this.objets()));
  private readonly utilisateursIndex = computed(() => indexParId(this.utilisateurs()));
  private readonly reparateursIndex = computed(() => indexParId(this.reparateurs()));

  readonly statuts = STATUTS_DEMANDE;
  readonly libelle = libelleStatut;
  readonly date = formaterDate;
  readonly labelReparateur = labelReparateur;

  filtreStatut: StatutDemande | '' = '';
  filtreReparateurId: number | undefined = undefined;
  villeReparateur = '';
  competenceReparateur = '';

  nouveau = { objetId: 0, utilisateurId: 0, descriptionPanne: '' };

  constructor() {
    effect(() => {
      this.session.identite();
      untracked(() => this.chargerDemandes());
    });
  }

  ngOnInit(): void {
    this.chargerReferentiels();
    this.chargerReparateurs();
  }

  optionObjet(objet: Objet): string {
    return objet.ville ? `${objet.intitule} — ${objet.ville}` : objet.intitule;
  }

  optionUtilisateur(utilisateur: Utilisateur): string {
    const nom = labelUtilisateur(utilisateur);
    return utilisateur.ville ? `${nom} — ${utilisateur.ville}` : nom;
  }

  nomObjet(id?: number | null): string {
    if (id == null) {
      return '—';
    }
    return labelObjet(this.objetsIndex().get(id), id);
  }

  nomUtilisateur(id?: number | null): string {
    if (id == null) {
      return '—';
    }
    return labelUtilisateur(this.utilisateursIndex().get(id), id);
  }

  nomReparateur(id?: number | null): string {
    if (id == null) {
      return 'Non assigné';
    }
    return labelReparateur(this.reparateursIndex().get(id), id);
  }

  chargerReferentiels(): void {
    this.api.listerObjets().subscribe({
      next: (objets) => {
        this.objets.set(objets);
        if (!this.nouveau.objetId && objets.length > 0) {
          this.nouveau.objetId = objets[0].id;
        }
      },
      error: (err) => this.erreur.set(messageErreur(err)),
    });
    this.api.listerUtilisateurs().subscribe({
      next: (utilisateurs) => {
        this.utilisateurs.set(utilisateurs);
        if (!this.nouveau.utilisateurId && utilisateurs.length > 0) {
          this.nouveau.utilisateurId = utilisateurs[0].id;
        }
      },
      error: (err) => this.erreur.set(messageErreur(err)),
    });
  }

  chargerDemandes(): void {
    this.erreur.set(null);
    this.api.listerDemandes(this.filtreCourant()).subscribe({
      next: (demandes) => this.demandes.set(demandes),
      error: (err) => this.erreur.set(messageErreur(err)),
    });
  }

  private filtreCourant(): FiltreDemandes {
    if (this.estAdmin()) {
      return { statut: this.filtreStatut || undefined, reparateurId: this.filtreReparateurId };
    }
    if (this.session.est('UTILISATEUR')) {
      return { utilisateurId: this.session.utilisateurId() ?? undefined };
    }
    if (this.session.est('REPARATEUR')) {
      return { reparateurId: this.session.reparateurId() ?? undefined };
    }
    return {};
  }

  chargerReparateurs(): void {
    this.api
      .rechercherReparateurs({
        ville: this.villeReparateur || undefined,
        competence: this.competenceReparateur || undefined,
      })
      .subscribe({
        next: (reparateurs) => this.reparateurs.set(reparateurs),
        error: (err) => this.erreur.set(messageErreur(err)),
      });
  }

  creerDemande(): void {
    this.erreur.set(null);
    this.succes.set(null);
    if (!this.nouveau.descriptionPanne.trim()) {
      this.erreur.set('La description de la panne est obligatoire.');
      return;
    }
    const utilisateurId = this.estAdmin()
      ? this.nouveau.utilisateurId
      : this.session.utilisateurId();
    if (!this.nouveau.objetId || !utilisateurId) {
      this.erreur.set('Sélectionne un objet et un demandeur.');
      return;
    }
    this.enCours.set(true);
    this.api.creerDemande(this.nouveau.objetId, utilisateurId, this.nouveau.descriptionPanne).subscribe({
      next: (demande) => {
        this.enCours.set(false);
        this.succes.set(`Demande #${demande.id} créée.`);
        this.nouveau = { ...this.nouveau, descriptionPanne: '' };
        this.chargerDemandes();
      },
      error: (err) => {
        this.enCours.set(false);
        this.erreur.set(messageErreur(err));
      },
    });
  }
}
