import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { DonApiService } from '@valorio/api-don';
import {
  AnnonceDon,
  DemandeDon,
  Objet,
  STATUTS_DEMANDE_DON,
  SessionService,
  StatutDemandeDon,
  Utilisateur,
  formaterDate,
  indexParId,
  labelObjet,
  labelUtilisateur,
  libelleStatut,
  messageErreur,
} from '@valorio/shared';

@Component({
  selector: 'valorio-annonce-detail',
  imports: [FormsModule, RouterLink],
  template: `
    <section class="page">
      <header class="page-header">
        <div>
          <h1>{{ annonce()?.titre || 'Annonce' }} <span class="muted">#{{ annonceId }}</span></h1>
          <p>{{ annonce()?.description || 'Sans description' }}</p>
          @if (annonce(); as a) {
            <p class="muted">
              Objet : {{ nomObjet(a.objetId) }} · Donneur : {{ nomUtilisateur(a.donneurId) }}
              · {{ a.ville || 'Ville inconnue' }}
            </p>
            @if (a.conditions) {
              <p class="muted">Conditions : {{ a.conditions }}</p>
            }
          }
        </div>
        @if (annonce(); as a) {
          <div class="toolbar">
            <span [class]="'badge ' + a.statut.toLowerCase()">{{ libelle(a.statut) }}</span>
            @if (peutAnnuler()) {
              <button class="btn small danger" type="button" (click)="annuler()" [disabled]="enCours()">
                Annuler l'annonce
              </button>
            }
          </div>
        }
      </header>

      @if (erreur()) {
        <div class="alert error">{{ erreur() }}</div>
      }
      @if (succes()) {
        <div class="alert success">{{ succes() }}</div>
      }

      @if (espaceBeneficiaire()) {
        <div class="role-section">
          <h2 class="section-title">
            {{ estAdmin() ? 'Vue administrateur — bénéficiaire' : 'Espace bénéficiaire' }}
          </h2>

          <div class="card">
            <h2>Demander cet objet</h2>
            <form class="toolbar" (ngSubmit)="demander()">
              @if (estAdmin()) {
                <label class="field">
                  <span>Bénéficiaire</span>
                  <select name="beneficiaireId" [(ngModel)]="demande.beneficiaireId" required>
                    @for (u of utilisateurs(); track u.id) {
                      <option [ngValue]="u.id">{{ optionUtilisateur(u) }}</option>
                    }
                  </select>
                </label>
              }
              <label class="field" style="flex:1">
                <span>Message au donneur</span>
                <input type="text" name="message" [(ngModel)]="demande.message" placeholder="Pourquoi cet objet vous intéresse" />
              </label>
              <button class="btn" type="submit" [disabled]="enCours()">Envoyer ma demande</button>
            </form>
          </div>

          <div class="card">
            <h2>Mes demandes sur cette annonce</h2>
            @if (mesDemandes().length === 0) {
              <p class="empty">Vous n'avez pas encore demandé cet objet.</p>
            } @else {
              <ul class="timeline">
                @for (d of mesDemandes(); track d.id) {
                  <li>
                    <span class="when">{{ date(d.dateDemande) }}</span>
                    <span [class]="'badge ' + d.statut.toLowerCase()">{{ libelle(d.statut) }}</span>
                    <span class="muted">{{ d.message || 'Sans message' }}</span>
                  </li>
                }
              </ul>
            }
          </div>
        </div>
      }

      @if (espaceDonneur()) {
        <div class="role-section">
          <h2 class="section-title">
            {{ estAdmin() ? 'Vue administrateur — donneur' : 'Espace donneur' }}
          </h2>

          <div class="card">
            <h2>Demandes reçues</h2>
            @if (demandes().length === 0) {
              <p class="empty">Aucune demande pour cette annonce.</p>
            } @else {
              <table>
                <thead>
                  <tr>
                    <th>#</th>
                    <th>Bénéficiaire</th>
                    <th>Message</th>
                    <th>Statut</th>
                    <th>Reçue le</th>
                    <th>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  @for (d of demandes(); track d.id) {
                    <tr>
                      <td>{{ d.id }}</td>
                      <td>{{ nomUtilisateur(d.beneficiaireId) }}</td>
                      <td>{{ d.message || '—' }}</td>
                      <td>
                        <span [class]="'badge ' + d.statut.toLowerCase()">{{ libelle(d.statut) }}</span>
                      </td>
                      <td>{{ date(d.dateDemande) }}</td>
                      <td>
                        <div class="actions">
                          @if (prochain(d)) {
                            <button class="btn small" (click)="avancer(d)" [disabled]="enCours()">
                              {{ libelle(prochain(d)!) }}
                            </button>
                          }
                          @if (d.statut === 'DEMANDE' || d.statut === 'ACCEPTEE') {
                            <button class="btn small danger" (click)="refuser(d)" [disabled]="enCours()">
                              Refuser
                            </button>
                          }
                        </div>
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
            Cette page concerne les rôles <strong>Bénéficiaire</strong> (demander l'objet) et
            <strong>Donneur</strong> (traiter les demandes reçues). Changez de rôle dans la barre du haut.
          </p>
        </div>
      }

      <a class="btn secondary" routerLink="/don">Retour aux annonces</a>
    </section>
  `,
})
export class AnnonceDetail implements OnInit {
  private readonly api = inject(DonApiService);
  private readonly route = inject(ActivatedRoute);
  private readonly session = inject(SessionService);

  readonly annonce = signal<AnnonceDon | null>(null);
  readonly demandes = signal<DemandeDon[]>([]);
  readonly utilisateurs = signal<Utilisateur[]>([]);
  readonly objets = signal<Objet[]>([]);
  readonly erreur = signal<string | null>(null);
  readonly succes = signal<string | null>(null);
  readonly enCours = signal(false);

  protected readonly estAdmin = this.session.estAdmin;
  protected readonly espaceDonneur = computed(() => this.session.est('DONNEUR', 'ADMIN'));
  protected readonly espaceBeneficiaire = computed(() => this.session.est('BENEFICIAIRE', 'ADMIN'));
  protected readonly horsRole = computed(
    () => !this.espaceDonneur() && !this.espaceBeneficiaire(),
  );

  readonly mesDemandes = computed(() => {
    const moi = this.session.utilisateurId();
    if (moi == null) {
      return [];
    }
    return this.demandes().filter((d) => d.beneficiaireId === moi);
  });

  private readonly utilisateursIndex = computed(() => indexParId(this.utilisateurs()));
  private readonly objetsIndex = computed(() => indexParId(this.objets()));

  readonly libelle = libelleStatut;
  readonly date = formaterDate;

  annonceId = 0;
  demande: { beneficiaireId: number; message?: string } = { beneficiaireId: 0 };

  ngOnInit(): void {
    this.annonceId = Number(this.route.snapshot.paramMap.get('id'));
    this.chargerReferentiels();
    this.charger();
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

  peutAnnuler(): boolean {
    const statut = this.annonce()?.statut;
    return this.espaceDonneur() && (statut === 'PUBLIEE' || statut === 'RESERVEE');
  }

  chargerReferentiels(): void {
    this.api.listerUtilisateurs().subscribe({
      next: (utilisateurs) => {
        this.utilisateurs.set(utilisateurs);
        if (!this.demande.beneficiaireId && utilisateurs.length > 0) {
          this.demande.beneficiaireId = utilisateurs[0].id;
        }
      },
      error: (err) => this.erreur.set(messageErreur(err)),
    });
    this.api.listerObjets().subscribe({
      next: (objets) => this.objets.set(objets),
      error: (err) => this.erreur.set(messageErreur(err)),
    });
  }

  charger(): void {
    this.erreur.set(null);
    this.api.getAnnonce(this.annonceId).subscribe({
      next: (annonce) => this.annonce.set(annonce),
      error: (err) => this.erreur.set(messageErreur(err)),
    });
    this.api.demandesDeAnnonce(this.annonceId).subscribe({
      next: (demandes) => this.demandes.set(demandes),
      error: (err) => this.erreur.set(messageErreur(err)),
    });
  }

  prochain(demande: DemandeDon): StatutDemandeDon | null {
    const index = STATUTS_DEMANDE_DON.indexOf(demande.statut);
    return index >= 0 && index < STATUTS_DEMANDE_DON.length - 1
      ? STATUTS_DEMANDE_DON[index + 1]
      : null;
  }

  demander(): void {
    this.erreur.set(null);
    this.succes.set(null);
    const beneficiaireId = this.estAdmin() ? this.demande.beneficiaireId : this.session.utilisateurId();
    if (!beneficiaireId) {
      this.erreur.set('Sélectionne un bénéficiaire.');
      return;
    }
    this.enCours.set(true);
    this.api.demander(this.annonceId, beneficiaireId, this.demande.message).subscribe({
      next: (demande) => {
        this.enCours.set(false);
        this.succes.set(`Demande #${demande.id} envoyée.`);
        this.demande = { ...this.demande, message: '' };
        this.charger();
      },
      error: (err) => {
        this.enCours.set(false);
        this.erreur.set(messageErreur(err));
      },
    });
  }

  annuler(): void {
    this.erreur.set(null);
    this.succes.set(null);
    this.enCours.set(true);
    const donneurId = this.estAdmin() ? undefined : (this.session.utilisateurId() ?? undefined);
    this.api.annulerAnnonce(this.annonceId, donneurId).subscribe({
      next: () => {
        this.enCours.set(false);
        this.succes.set('Annonce annulée.');
        this.charger();
      },
      error: (err) => {
        this.enCours.set(false);
        this.erreur.set(messageErreur(err));
      },
    });
  }

  avancer(demande: DemandeDon): void {
    const cible = this.prochain(demande);
    if (!cible) {
      return;
    }
    this.maj(demande.id, cible);
  }

  refuser(demande: DemandeDon): void {
    this.maj(demande.id, 'REFUSEE');
  }

  private maj(id: number, statut: StatutDemandeDon): void {
    this.erreur.set(null);
    this.enCours.set(true);
    this.api.changerStatutDemande(id, statut, 'Mise à jour depuis le front').subscribe({
      next: () => {
        this.enCours.set(false);
        this.succes.set('Statut mis à jour.');
        this.charger();
      },
      error: (err) => {
        this.enCours.set(false);
        this.erreur.set(messageErreur(err));
      },
    });
  }
}
