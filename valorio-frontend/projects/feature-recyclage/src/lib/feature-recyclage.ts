import { Component, OnInit, computed, effect, inject, signal, untracked } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { RecyclageApiService } from '@valorio/api-recyclage';
import {
  AnalyseRecyclabilite,
  CentreRecyclage,
  DemandeCollecte,
  Objet,
  SessionService,
  StatutCollecte,
  Utilisateur,
  formaterDate,
  indexParId,
  labelCentre,
  labelObjet,
  labelUtilisateur,
  libelleStatut,
  messageErreur,
} from '@valorio/shared';

@Component({
  selector: 'valorio-feature-recyclage',
  imports: [FormsModule, RouterLink],
  template: `
    <section class="page">
      <header class="page-header">
        <div>
          <h1>Recyclage</h1>
          <p>Identification des matériaux, recherche de centre, demande de collecte.</p>
        </div>
      </header>

      @if (erreur()) {
        <div class="alert error">{{ erreur() }}</div>
      }
      @if (succes()) {
        <div class="alert success">{{ succes() }}</div>
      }

      @if (espaceUtilisateur()) {
        <div class="role-section">
          <h2 class="section-title">
            {{ estAdmin() ? 'Vue administrateur — utilisateur' : 'Espace utilisateur' }}
          </h2>

          <div class="card">
            <h2>Analyser la recyclabilité d'un objet</h2>
            <form class="toolbar" (ngSubmit)="analyser()">
              <label class="field">
                <span>Objet</span>
                <select name="objetId" [(ngModel)]="objetId" required>
                  @for (o of objets(); track o.id) {
                    <option [ngValue]="o.id">{{ optionObjet(o) }}</option>
                  }
                </select>
              </label>
              <button class="btn" type="submit">Analyser</button>
            </form>

            @if (analyse(); as a) {
              <div class="alert info" style="margin-top:0.9rem">
                Verdict : <strong>{{ a.recyclable }}</strong>
              </div>
              <ul class="timeline" style="margin-top:0.7rem">
                @for (motif of a.motifs; track motif) {
                  <li>• {{ motif }}</li>
                }
              </ul>
              @if (a.centresAppropries.length > 0) {
                <p class="muted">Centres appropriés : {{ nomsCentres(a.centresAppropries) }}</p>
              }
            }
          </div>

          <div class="card">
            <h2>Rechercher un centre de recyclage</h2>
            <form class="toolbar" (ngSubmit)="chercherCentres()">
              <label class="field">
                <span>Matériau</span>
                <input type="text" name="materiau" [(ngModel)]="recherche.materiau" placeholder="plastique" />
              </label>
              <label class="field">
                <span>Latitude</span>
                <input type="number" name="latitude" [(ngModel)]="recherche.latitude" step="any" />
              </label>
              <label class="field">
                <span>Longitude</span>
                <input type="number" name="longitude" [(ngModel)]="recherche.longitude" step="any" />
              </label>
              <label class="field">
                <span>Rayon (km)</span>
                <input type="number" name="rayonKm" [(ngModel)]="recherche.rayonKm" min="1" />
              </label>
              <button class="btn secondary" type="submit">Rechercher</button>
            </form>

            @if (centres().length === 0) {
              <p class="empty">Aucun centre trouvé.</p>
            } @else {
              <div class="grid-cards" style="margin-top:0.9rem">
                @for (c of centres(); track c.id) {
                  <article class="tile">
                    <div class="spread">
                      <h3>{{ c.nom }}</h3>
                      @if (c.distanceKm != null) {
                        <span class="badge">{{ c.distanceKm }} km</span>
                      }
                    </div>
                    <span class="muted">{{ c.ville || 'Ville inconnue' }}</span>
                    <span class="muted">{{ c.adresse || 'Adresse non précisée' }}</span>
                    <span class="muted">Matériaux : {{ c.materiauxAcceptes.join(', ') || '—' }}</span>
                    <span class="muted">Horaires : {{ c.horaires || '—' }}</span>
                  </article>
                }
              </div>
            }
          </div>

          <div class="card">
            <h2>Nouvelle demande de collecte</h2>
            <form class="toolbar" (ngSubmit)="creerCollecte()">
              <label class="field">
                <span>Objet à recycler</span>
                <select name="cObjetId" [(ngModel)]="collecte.objetId" required>
                  @for (o of objets(); track o.id) {
                    <option [ngValue]="o.id">{{ optionObjet(o) }}</option>
                  }
                </select>
              </label>
              @if (estAdmin()) {
                <label class="field">
                  <span>Demandeur</span>
                  <select name="cUtilisateurId" [(ngModel)]="collecte.utilisateurId" required>
                    @for (u of utilisateurs(); track u.id) {
                      <option [ngValue]="u.id">{{ optionUtilisateur(u) }}</option>
                    }
                  </select>
                </label>
              }
              <label class="field">
                <span>Centre (optionnel)</span>
                <select name="cCentreId" [(ngModel)]="collecte.centreId">
                  <option [ngValue]="undefined">À déterminer</option>
                  @for (c of centres(); track c.id) {
                    <option [ngValue]="c.id">{{ labelCentre(c) }} — {{ c.ville || '—' }}</option>
                  }
                </select>
              </label>
              <button class="btn" type="submit" [disabled]="enCours()">Demander la collecte</button>
            </form>
          </div>

          <div class="card">
            <div class="spread">
              <h2>{{ estAdmin() ? 'Toutes les collectes' : 'Mes demandes de collecte' }}</h2>
              <div class="toolbar">
                <label class="field">
                  <span>Statut</span>
                  <select name="statutMes" [(ngModel)]="filtreStatutMes">
                    <option value="">Tous</option>
                    <option value="DEMANDE">Demandée</option>
                    <option value="PLANIFIEE">Planifiée</option>
                    <option value="EN_COURS">En cours</option>
                    <option value="RECUE">Reçue</option>
                    <option value="ANNULEE">Annulée</option>
                  </select>
                </label>
                <button class="btn secondary small" type="button" (click)="chargerMesCollectes()">Filtrer</button>
              </div>
            </div>
            @if (mesCollectes().length === 0) {
              <p class="empty">Aucune collecte.</p>
            } @else {
              <table style="margin-top:0.9rem">
                <thead>
                  <tr>
                    <th>#</th>
                    <th>Objet</th>
                    <th>Centre</th>
                    <th>Statut</th>
                    <th>Demandée le</th>
                    <th></th>
                  </tr>
                </thead>
                <tbody>
                  @for (c of mesCollectes(); track c.id) {
                    <tr>
                      <td>{{ c.id }}</td>
                      <td>{{ nomObjet(c.objetId) }}</td>
                      <td>{{ nomCentre(c.centreId) }}</td>
                      <td>
                        <span [class]="'badge ' + c.statut.toLowerCase()">{{ libelle(c.statut) }}</span>
                      </td>
                      <td>{{ date(c.dateDemande) }}</td>
                      <td>
                        <a class="btn small secondary" [routerLink]="['collectes', c.id]">Ouvrir</a>
                      </td>
                    </tr>
                  }
                </tbody>
              </table>
            }
          </div>
        </div>
      }

      @if (espaceOrganisme()) {
        <div class="role-section">
          <h2 class="section-title">
            {{ estAdmin() ? 'Vue administrateur — organisme' : 'Espace organisme de recyclage' }}
          </h2>

          <div class="card">
            <div class="spread">
              <h2>Collectes à traiter</h2>
              <div class="toolbar">
                <label class="field">
                  <span>Statut</span>
                  <select name="statutOrga" [(ngModel)]="filtreStatutOrganisme">
                    <option value="">Tous</option>
                    <option value="DEMANDE">Demandée</option>
                    <option value="PLANIFIEE">Planifiée</option>
                    <option value="EN_COURS">En cours</option>
                    <option value="RECUE">Reçue</option>
                    <option value="ANNULEE">Annulée</option>
                  </select>
                </label>
                <button class="btn secondary small" type="button" (click)="chargerCollectesOrganisme()">
                  Filtrer
                </button>
              </div>
            </div>
            @if (!estAdmin() && session.centreId() == null) {
              <p class="empty">
                Votre identité organisme n'est rattachée à aucun centre. L'administrateur peut choisir un
                centre dans la barre du haut.
              </p>
            } @else if (collectesOrganisme().length === 0) {
              <p class="empty">Aucune collecte pour votre centre.</p>
            } @else {
              <table style="margin-top:0.9rem">
                <thead>
                  <tr>
                    <th>#</th>
                    <th>Objet</th>
                    <th>Demandeur</th>
                    <th>Centre</th>
                    <th>Statut</th>
                    <th>Demandée le</th>
                    <th></th>
                  </tr>
                </thead>
                <tbody>
                  @for (c of collectesOrganisme(); track c.id) {
                    <tr>
                      <td>{{ c.id }}</td>
                      <td>{{ nomObjet(c.objetId) }}</td>
                      <td>{{ nomUtilisateur(c.utilisateurId) }}</td>
                      <td>{{ nomCentre(c.centreId) }}</td>
                      <td>
                        <span [class]="'badge ' + c.statut.toLowerCase()">{{ libelle(c.statut) }}</span>
                      </td>
                      <td>{{ date(c.dateDemande) }}</td>
                      <td>
                        <a class="btn small" [routerLink]="['collectes', c.id]">Traiter</a>
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
            Cette page concerne les rôles <strong>Utilisateur</strong> (analyser / demander une collecte)
            et <strong>Organisme de recyclage</strong> (traiter les collectes). Changez de rôle dans la
            barre du haut.
          </p>
        </div>
      }
    </section>
  `,
})
export class FeatureRecyclageHome implements OnInit {
  private readonly api = inject(RecyclageApiService);
  protected readonly session = inject(SessionService);

  readonly analyse = signal<AnalyseRecyclabilite | null>(null);
  readonly centres = signal<CentreRecyclage[]>([]);
  readonly mesCollectes = signal<DemandeCollecte[]>([]);
  readonly collectesOrganisme = signal<DemandeCollecte[]>([]);
  readonly objets = signal<Objet[]>([]);
  readonly utilisateurs = signal<Utilisateur[]>([]);
  readonly erreur = signal<string | null>(null);
  readonly succes = signal<string | null>(null);
  readonly enCours = signal(false);

  protected readonly estAdmin = this.session.estAdmin;
  protected readonly espaceUtilisateur = computed(() => this.session.est('UTILISATEUR', 'ADMIN'));
  protected readonly espaceOrganisme = computed(() => this.session.est('ORGANISME', 'ADMIN'));
  protected readonly horsRole = computed(
    () => !this.espaceUtilisateur() && !this.espaceOrganisme(),
  );

  private readonly objetsIndex = computed(() => indexParId(this.objets()));
  private readonly utilisateursIndex = computed(() => indexParId(this.utilisateurs()));
  private readonly centresIndex = computed(() => indexParId(this.centres()));

  readonly libelle = libelleStatut;
  readonly date = formaterDate;
  readonly labelCentre = labelCentre;

  objetId = 0;
  filtreStatutMes: StatutCollecte | '' = '';
  filtreStatutOrganisme: StatutCollecte | '' = '';

  recherche = {
    materiau: '',
    latitude: 36.8065,
    longitude: 10.1815,
    rayonKm: 100,
  };

  collecte = { objetId: 0, utilisateurId: 0, centreId: undefined as number | undefined };

  constructor() {
    effect(() => {
      this.session.identite();
      untracked(() => {
        this.chargerMesCollectes();
        this.chargerCollectesOrganisme();
      });
    });
  }

  ngOnInit(): void {
    this.chargerReferentiels();
    this.chercherCentres();
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

  nomCentre(id?: number | null): string {
    if (id == null) {
      return 'Non assigné';
    }
    return labelCentre(this.centresIndex().get(id), id);
  }

  nomsCentres(ids: number[]): string {
    return ids.map((id) => this.nomCentre(id)).join(', ');
  }

  chargerReferentiels(): void {
    this.api.listerObjets().subscribe({
      next: (objets) => {
        this.objets.set(objets);
        if (!this.objetId && objets.length > 0) {
          this.objetId = objets[0].id;
        }
        if (!this.collecte.objetId && objets.length > 0) {
          this.collecte.objetId = objets[0].id;
        }
      },
      error: (err) => this.erreur.set(messageErreur(err)),
    });
    this.api.listerUtilisateurs().subscribe({
      next: (utilisateurs) => {
        this.utilisateurs.set(utilisateurs);
        if (!this.collecte.utilisateurId && utilisateurs.length > 0) {
          this.collecte.utilisateurId = utilisateurs[0].id;
        }
      },
      error: (err) => this.erreur.set(messageErreur(err)),
    });
  }

  analyser(): void {
    this.erreur.set(null);
    this.analyse.set(null);
    this.api.analyserRecyclabilite(this.objetId).subscribe({
      next: (analyse) => this.analyse.set(analyse),
      error: (err) => this.erreur.set(messageErreur(err)),
    });
  }

  chercherCentres(): void {
    this.erreur.set(null);
    this.api
      .rechercherCentres({
        materiau: this.recherche.materiau || undefined,
        latitude: this.recherche.latitude,
        longitude: this.recherche.longitude,
        rayonKm: this.recherche.rayonKm,
      })
      .subscribe({
        next: (centres) => this.centres.set(centres),
        error: (err) => this.erreur.set(messageErreur(err)),
      });
  }

  chargerMesCollectes(): void {
    this.erreur.set(null);
    const utilisateurId = this.session.est('UTILISATEUR') ? this.session.utilisateurId() : null;
    this.api
      .listerCollectes({
        statut: this.filtreStatutMes || undefined,
        utilisateurId: utilisateurId ?? undefined,
      })
      .subscribe({
        next: (collectes) => this.mesCollectes.set(collectes),
        error: (err) => this.erreur.set(messageErreur(err)),
      });
  }

  chargerCollectesOrganisme(): void {
    this.erreur.set(null);
    const centreId = this.session.est('ORGANISME') ? this.session.centreId() : null;
    if (this.session.est('ORGANISME') && centreId == null) {
      this.collectesOrganisme.set([]);
      return;
    }
    this.api
      .listerCollectes({
        statut: this.filtreStatutOrganisme || undefined,
        centreId: centreId ?? undefined,
      })
      .subscribe({
        next: (collectes) => this.collectesOrganisme.set(collectes),
        error: (err) => this.erreur.set(messageErreur(err)),
      });
  }

  creerCollecte(): void {
    this.erreur.set(null);
    this.succes.set(null);
    const utilisateurId = this.estAdmin() ? this.collecte.utilisateurId : this.session.utilisateurId();
    if (!this.collecte.objetId || !utilisateurId) {
      this.erreur.set('Sélectionne un objet et un demandeur.');
      return;
    }
    this.enCours.set(true);
    this.api
      .demanderCollecte({
        objetId: this.collecte.objetId,
        utilisateurId,
        centreId: this.collecte.centreId,
      })
      .subscribe({
        next: (collecte) => {
          this.enCours.set(false);
          this.succes.set(`Collecte #${collecte.id} demandée.`);
          this.collecte = { ...this.collecte, centreId: undefined };
          this.chargerMesCollectes();
          this.chargerCollectesOrganisme();
        },
        error: (err) => {
          this.enCours.set(false);
          this.erreur.set(messageErreur(err));
        },
      });
  }
}
