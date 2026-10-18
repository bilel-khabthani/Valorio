import { Component, OnInit, computed, effect, inject, signal, untracked } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { DonApiService } from '@valorio/api-don';
import {
  AnnonceDon,
  CorrespondanceDon,
  Objet,
  SessionService,
  StatutAnnonceDon,
  Utilisateur,
  formaterDate,
  indexParId,
  labelObjet,
  labelUtilisateur,
  libelleStatut,
  messageErreur,
} from '@valorio/shared';

@Component({
  selector: 'valorio-feature-don',
  imports: [FormsModule, RouterLink],
  template: `
    <section class="page">
      <header class="page-header">
        <div>
          <h1>Don</h1>
          <p>Publication d'un objet au don, matching bénéficiaire, distribution.</p>
        </div>
      </header>

      @if (erreur()) {
        <div class="alert error">{{ erreur() }}</div>
      }
      @if (succes()) {
        <div class="alert success">{{ succes() }}</div>
      }

      @if (espaceDonneur()) {
        <div class="role-section">
          <h2 class="section-title">
            {{ estAdmin() ? 'Vue administrateur — donneur' : 'Espace donneur' }}
          </h2>

          <div class="card">
            <h2>Publier un objet au don</h2>
            <form class="toolbar" (ngSubmit)="publier()">
              <label class="field">
                <span>Objet à donner</span>
                <select name="objetId" [(ngModel)]="nouvelle.objetId" required>
                  @for (o of objets(); track o.id) {
                    <option [ngValue]="o.id">{{ optionObjet(o) }}</option>
                  }
                </select>
              </label>
              @if (estAdmin()) {
                <label class="field">
                  <span>Donneur</span>
                  <select name="donneurId" [(ngModel)]="nouvelle.donneurId" required>
                    @for (u of utilisateurs(); track u.id) {
                      <option [ngValue]="u.id">{{ optionUtilisateur(u) }}</option>
                    }
                  </select>
                </label>
              }
              <label class="field" style="flex:1">
                <span>Titre</span>
                <input type="text" name="titre" [(ngModel)]="nouvelle.titre" placeholder="Table en bois" required />
              </label>
              <label class="field">
                <span>Ville</span>
                <input type="text" name="ville" [(ngModel)]="nouvelle.ville" />
              </label>
              <label class="field" style="flex:1">
                <span>Description</span>
                <input type="text" name="description" [(ngModel)]="nouvelle.description" placeholder="État, dimensions..." />
              </label>
              <label class="field" style="flex:1">
                <span>Conditions</span>
                <input type="text" name="conditions" [(ngModel)]="nouvelle.conditions" placeholder="Retrait sur place, personne/association..." />
              </label>
              <button class="btn" type="submit" [disabled]="enCours()">Publier</button>
            </form>
          </div>

          <div class="card">
            <div class="spread">
              <h2>{{ estAdmin() ? 'Toutes les annonces' : 'Mes annonces' }}</h2>
              <div class="toolbar">
                <label class="field">
                  <span>Ville</span>
                  <input type="text" name="fVille" [(ngModel)]="filtreVille" />
                </label>
                <label class="field">
                  <span>Statut</span>
                  <select name="fStatut" [(ngModel)]="filtreStatut">
                    <option value="">Tous</option>
                    <option value="PUBLIEE">Publiée</option>
                    <option value="RESERVEE">Réservée</option>
                    <option value="REMISE">Remise</option>
                    <option value="ANNULEE">Annulée</option>
                  </select>
                </label>
                <button class="btn secondary" type="button" (click)="chargerAnnonces()">Filtrer</button>
              </div>
            </div>

            @if (annonces().length === 0) {
              <p class="empty">Aucune annonce.</p>
            } @else {
              <div class="grid-cards" style="margin-top:0.9rem">
                @for (a of annonces(); track a.id) {
                  <article class="tile">
                    <div class="spread">
                      <h3>{{ a.titre }}</h3>
                      <span [class]="'badge ' + a.statut.toLowerCase()">{{ libelle(a.statut) }}</span>
                    </div>
                    <span class="muted">Objet : {{ nomObjet(a.objetId) }}</span>
                    <span class="muted">Donneur : {{ nomUtilisateur(a.donneurId) }}</span>
                    <span class="muted">{{ a.description || 'Sans description' }}</span>
                    @if (a.conditions) {
                      <span class="muted">Conditions : {{ a.conditions }}</span>
                    }
                    <span class="muted">{{ a.ville || 'Ville inconnue' }}</span>
                    <span class="muted">Publiée le {{ date(a.datePublication) }}</span>
                    <a class="btn small secondary" [routerLink]="['annonces', a.id]">Voir les demandes</a>
                  </article>
                }
              </div>
            }
          </div>
        </div>
      }

      @if (espaceBeneficiaire()) {
        <div class="role-section">
          <h2 class="section-title">
            {{ estAdmin() ? 'Vue administrateur — bénéficiaire' : 'Espace bénéficiaire' }}
          </h2>

          <div class="card">
            <h2>Matching bénéficiaire</h2>
            <form class="toolbar" (ngSubmit)="chercherMatching()">
              <label class="field">
                <span>Latitude</span>
                <input type="number" step="any" name="mLatitude" [(ngModel)]="matching.latitude" />
              </label>
              <label class="field">
                <span>Longitude</span>
                <input type="number" step="any" name="mLongitude" [(ngModel)]="matching.longitude" />
              </label>
              <label class="field">
                <span>Rayon (km)</span>
                <input type="number" name="rayonKm" [(ngModel)]="matching.rayonKm" min="1" />
              </label>
              <button class="btn secondary" type="submit">Rechercher</button>
            </form>

            @if (correspondances().length === 0) {
              <p class="empty">Aucune correspondance calculée.</p>
            } @else {
              <table style="margin-top:0.9rem">
                <thead>
                  <tr>
                    <th>Annonce</th>
                    <th>Objet</th>
                    <th>Ville</th>
                    <th>Distance</th>
                    <th>Score</th>
                  </tr>
                </thead>
                <tbody>
                  @for (c of correspondances(); track c.annonce.id) {
                    <tr>
                      <td>
                        <a [routerLink]="['annonces', c.annonce.id]">{{ c.annonce.titre }}</a>
                      </td>
                      <td>{{ nomObjet(c.annonce.objetId) }}</td>
                      <td>{{ c.annonce.ville || '—' }}</td>
                      <td>{{ c.distanceKm }} km</td>
                      <td>{{ c.score }}</td>
                    </tr>
                  }
                </tbody>
              </table>
            }
          </div>

          <div class="card">
            <h2>Annonces disponibles</h2>
            @if (annonces().length === 0) {
              <p class="empty">Aucune annonce disponible.</p>
            } @else {
              <div class="grid-cards" style="margin-top:0.9rem">
                @for (a of annonces(); track a.id) {
                  <article class="tile">
                    <div class="spread">
                      <h3>{{ a.titre }}</h3>
                      <span [class]="'badge ' + a.statut.toLowerCase()">{{ libelle(a.statut) }}</span>
                    </div>
                    <span class="muted">Objet : {{ nomObjet(a.objetId) }}</span>
                    <span class="muted">Donneur : {{ nomUtilisateur(a.donneurId) }}</span>
                    <span class="muted">{{ a.ville || 'Ville inconnue' }}</span>
                    <span class="muted">Publiée le {{ date(a.datePublication) }}</span>
                    <a class="btn small" [routerLink]="['annonces', a.id]">Demander cet objet</a>
                  </article>
                }
              </div>
            }
          </div>
        </div>
      }

      @if (horsRole()) {
        <div class="card">
          <p class="empty">
            Cette page concerne les rôles <strong>Utilisateur</strong> (publier/suivre vos annonces) et
            <strong>Acheteur</strong> (demander un objet). Changez de rôle dans la barre du haut.
          </p>
        </div>
      }
    </section>
  `,
})
export class FeatureDonHome implements OnInit {
  private readonly api = inject(DonApiService);
  private readonly session = inject(SessionService);

  readonly annonces = signal<AnnonceDon[]>([]);
  readonly correspondances = signal<CorrespondanceDon[]>([]);
  readonly objets = signal<Objet[]>([]);
  readonly utilisateurs = signal<Utilisateur[]>([]);
  readonly erreur = signal<string | null>(null);
  readonly succes = signal<string | null>(null);
  readonly enCours = signal(false);

  protected readonly estAdmin = this.session.estAdmin;
  protected readonly espaceDonneur = computed(() => this.session.est('UTILISATEUR', 'ADMIN'));
  protected readonly espaceBeneficiaire = computed(() => this.session.est('ACHETEUR', 'ADMIN'));
  protected readonly horsRole = computed(
    () => !this.espaceDonneur() && !this.espaceBeneficiaire(),
  );

  private readonly objetsIndex = computed(() => indexParId(this.objets()));
  private readonly utilisateursIndex = computed(() => indexParId(this.utilisateurs()));

  readonly libelle = libelleStatut;
  readonly date = formaterDate;

  filtreVille = '';
  filtreStatut: StatutAnnonceDon | '' = '';

  nouvelle = {
    objetId: 0,
    donneurId: 0,
    titre: '',
    description: '',
    conditions: '',
    ville: 'Tunis',
    latitude: undefined as number | undefined,
    longitude: undefined as number | undefined,
  };

  matching = {
    latitude: 36.8065,
    longitude: 10.1815,
    rayonKm: 50,
  };

  constructor() {
    effect(() => {
      this.session.identite();
      untracked(() => this.chargerAnnonces());
    });
  }

  ngOnInit(): void {
    this.chargerReferentiels();
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

  chargerReferentiels(): void {
    this.api.listerObjets().subscribe({
      next: (objets) => {
        this.objets.set(objets);
        if (!this.nouvelle.objetId && objets.length > 0) {
          this.nouvelle.objetId = objets[0].id;
        }
      },
      error: (err) => this.erreur.set(messageErreur(err)),
    });
    this.api.listerUtilisateurs().subscribe({
      next: (utilisateurs) => {
        this.utilisateurs.set(utilisateurs);
        if (!this.nouvelle.donneurId && utilisateurs.length > 0) {
          this.nouvelle.donneurId = utilisateurs[0].id;
        }
      },
      error: (err) => this.erreur.set(messageErreur(err)),
    });
  }

  chargerAnnonces(): void {
    this.erreur.set(null);
    this.api
      .listerAnnonces({
        ville: this.filtreVille || undefined,
        statut: this.filtreStatut || undefined,
      })
      .subscribe({
        next: (annonces) => {
          const moi = this.session.utilisateurId();
          const liste =
            this.session.est('UTILISATEUR') && moi != null
              ? annonces.filter((a) => a.donneurId === moi)
              : annonces;
          this.annonces.set(liste);
        },
        error: (err) => this.erreur.set(messageErreur(err)),
      });
  }

  publier(): void {
    this.erreur.set(null);
    this.succes.set(null);
    if (!this.nouvelle.titre.trim()) {
      this.erreur.set('Le titre est obligatoire.');
      return;
    }
    const donneurId = this.estAdmin() ? this.nouvelle.donneurId : this.session.utilisateurId();
    if (!this.nouvelle.objetId || !donneurId) {
      this.erreur.set('Sélectionne un objet et un donneur.');
      return;
    }
    this.enCours.set(true);
    this.api
      .publierAnnonce({
        objetId: this.nouvelle.objetId,
        donneurId,
        titre: this.nouvelle.titre,
        description: this.nouvelle.description || undefined,
        conditions: this.nouvelle.conditions || undefined,
        ville: this.nouvelle.ville || undefined,
        latitude: this.nouvelle.latitude,
        longitude: this.nouvelle.longitude,
      })
      .subscribe({
        next: (annonce) => {
          this.enCours.set(false);
          this.succes.set(`Annonce #${annonce.id} publiée.`);
          this.nouvelle = { ...this.nouvelle, titre: '', description: '', conditions: '' };
          this.chargerAnnonces();
        },
        error: (err) => {
          this.enCours.set(false);
          this.erreur.set(messageErreur(err));
        },
      });
  }

  chercherMatching(): void {
    this.erreur.set(null);
    this.api
      .matching({
        latitude: this.matching.latitude,
        longitude: this.matching.longitude,
        rayonKm: this.matching.rayonKm,
      })
      .subscribe({
        next: (correspondances) => this.correspondances.set(correspondances),
        error: (err) => this.erreur.set(messageErreur(err)),
      });
  }
}
