import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ActivatedRoute } from '@angular/router';
import { RecyclageApiService } from '@valorio/api-recyclage';
import {
  CentreRecyclage,
  DemandeCollecte,
  Objet,
  STATUTS_COLLECTE,
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
  selector: 'valorio-collecte-detail',
  imports: [RouterLink],
  template: `
    <section class="page">
      <header class="page-header">
        <div>
          <h1>Collecte #{{ collecteId }}</h1>
          <p>Suivi de la collecte et réception horodatée.</p>
        </div>
        @if (collecte(); as c) {
          <span [class]="'badge ' + c.statut.toLowerCase()">{{ libelle(c.statut) }}</span>
        }
      </header>

      @if (erreur()) {
        <div class="alert error">{{ erreur() }}</div>
      }
      @if (succes()) {
        <div class="alert success">{{ succes() }}</div>
      }

      @if (collecte(); as c) {
        <div class="card">
          <h2>Informations</h2>
          <div class="grid-cards">
            <div class="tile">
              <h3>{{ nomObjet(c.objetId) }}</h3>
              <span class="muted">Demandeur : {{ nomUtilisateur(c.utilisateurId) }}</span>
            </div>
            <div class="tile">
              <h3>Centre</h3>
              <span class="muted">{{ nomCentre(c.centreId) }}</span>
            </div>
            <div class="tile">
              <h3>Dates</h3>
              <span class="muted">Demande : {{ date(c.dateDemande) }}</span>
              <span class="muted">Prévue : {{ date(c.datePrevue) }}</span>
              <span class="muted">Réception : {{ date(c.dateReception) }}</span>
            </div>
          </div>
        </div>

        @if (espaceOrganisme()) {
          <div class="role-section">
            <h2 class="section-title">
              {{ estAdmin() ? 'Vue administrateur — organisme' : 'Espace organisme' }}
            </h2>
            <div class="card">
              <h2>Traiter la collecte</h2>
              @if (!centreAutorise()) {
                <p class="empty">
                  Cette collecte concerne un autre centre que le vôtre. Seul le centre concerné (ou un
                  administrateur) peut la traiter.
                </p>
              } @else {
                <div class="flex-row">
                  @if (prochain(); as suivant) {
                    <button class="btn" (click)="avancer()" [disabled]="enCours()">
                      Passer à {{ libelle(suivant) }}
                    </button>
                  } @else {
                    <span class="muted">Collecte terminée.</span>
                  }
                  @if (peutAnnuler()) {
                    <button class="btn danger" (click)="annuler()" [disabled]="enCours()">
                      Annuler la collecte
                    </button>
                  }
                </div>
              }
            </div>
          </div>
        }

        @if (espaceUtilisateur()) {
          <div class="role-section">
            <h2 class="section-title">
              {{ estAdmin() ? 'Vue administrateur — utilisateur' : 'Espace utilisateur' }}
            </h2>
            <div class="card">
              <h2>Ma demande</h2>
              @if (peutAnnuler()) {
                <p class="muted">
                  Tant que la collecte n'est pas effectuée, vous pouvez l'annuler.
                </p>
                <button class="btn danger" (click)="annuler()" [disabled]="enCours()">
                  Annuler ma demande
                </button>
              } @else {
                <p class="empty">
                  Cette collecte ne peut plus être annulée (statut : {{ libelle(c.statut) }}).
                </p>
              }
            </div>
          </div>
        }

        <div class="card">
          <h2>Historique</h2>
          <ul class="timeline">
            @for (h of c.historique ?? []; track h.date) {
              <li>
                <span class="when">{{ date(h.date) }}</span>
                <span [class]="'badge ' + h.statut.toLowerCase()">{{ libelle(h.statut) }}</span>
                <span class="muted">{{ h.commentaire || '' }}</span>
              </li>
            }
          </ul>
        </div>

        @if (horsRole()) {
          <div class="card">
            <p class="empty">
              Cette page concerne les rôles <strong>Utilisateur</strong> (suivre/annuler sa demande) et
              <strong>Organisme de recyclage</strong> (traiter la collecte). Changez de rôle dans la barre
              du haut.
            </p>
          </div>
        }
      } @else {
        <p class="empty">Chargement de la collecte...</p>
      }

      <a class="btn secondary" routerLink="/recyclage">Retour</a>
    </section>
  `,
})
export class CollecteDetail implements OnInit {
  private readonly api = inject(RecyclageApiService);
  private readonly route = inject(ActivatedRoute);
  private readonly session = inject(SessionService);

  readonly collecte = signal<DemandeCollecte | null>(null);
  readonly objets = signal<Objet[]>([]);
  readonly utilisateurs = signal<Utilisateur[]>([]);
  readonly centres = signal<CentreRecyclage[]>([]);
  readonly erreur = signal<string | null>(null);
  readonly succes = signal<string | null>(null);
  readonly enCours = signal(false);

  protected readonly estAdmin = this.session.estAdmin;
  protected readonly espaceOrganisme = computed(() => this.session.est('ORGANISME', 'ADMIN'));
  protected readonly espaceUtilisateur = computed(() => this.session.est('UTILISATEUR', 'ADMIN'));
  protected readonly horsRole = computed(
    () => !this.espaceOrganisme() && !this.espaceUtilisateur(),
  );

  private readonly objetsIndex = computed(() => indexParId(this.objets()));
  private readonly utilisateursIndex = computed(() => indexParId(this.utilisateurs()));
  private readonly centresIndex = computed(() => indexParId(this.centres()));

  readonly libelle = libelleStatut;
  readonly date = formaterDate;

  collecteId = 0;

  ngOnInit(): void {
    this.collecteId = Number(this.route.snapshot.paramMap.get('id'));
    this.chargerReferentiels();
    this.charger();
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
      return 'Non affecté';
    }
    return labelCentre(this.centresIndex().get(id), id);
  }

  prochain(): StatutCollecte | null {
    const statut = this.collecte()?.statut;
    if (!statut) {
      return null;
    }
    const index = STATUTS_COLLECTE.indexOf(statut);
    return index >= 0 && index < STATUTS_COLLECTE.length - 1 ? STATUTS_COLLECTE[index + 1] : null;
  }

  centreAutorise(): boolean {
    if (this.estAdmin()) {
      return true;
    }
    const centreId = this.session.centreId();
    const collecte = this.collecte();
    return centreId == null || collecte?.centreId == null || collecte.centreId === centreId;
  }

  peutAnnuler(): boolean {
    const statut = this.collecte()?.statut;
    if (statut !== 'DEMANDE' && statut !== 'PLANIFIEE') {
      return false;
    }
    return this.estAdmin() || this.session.est('UTILISATEUR', 'ORGANISME');
  }

  chargerReferentiels(): void {
    this.api.listerObjets().subscribe({
      next: (objets) => this.objets.set(objets),
      error: (err) => this.erreur.set(messageErreur(err)),
    });
    this.api.listerUtilisateurs().subscribe({
      next: (utilisateurs) => this.utilisateurs.set(utilisateurs),
      error: (err) => this.erreur.set(messageErreur(err)),
    });
    this.api.rechercherCentres({}).subscribe({
      next: (centres) => this.centres.set(centres),
      error: (err) => this.erreur.set(messageErreur(err)),
    });
  }

  private maj(statut: StatutCollecte): void {
    this.erreur.set(null);
    this.succes.set(null);
    this.enCours.set(true);
    this.api
      .changerStatutCollecte(this.collecteId, statut, undefined, 'Mise à jour depuis le front')
      .subscribe({
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

  avancer(): void {
    const cible = this.prochain();
    if (cible) {
      this.maj(cible);
    }
  }

  annuler(): void {
    this.maj('ANNULEE');
  }

  private charger(): void {
    this.erreur.set(null);
    this.api.getCollecte(this.collecteId).subscribe({
      next: (collecte) => this.collecte.set(collecte),
      error: (err) => this.erreur.set(messageErreur(err)),
    });
  }
}
