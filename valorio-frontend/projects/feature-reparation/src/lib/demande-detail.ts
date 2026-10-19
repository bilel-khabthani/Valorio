import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { ReparationApiService } from '@valorio/api-reparation';
import {
  DemandeReparation,
  Devis,
  DiagnosticIaResponse,
  Reparateur,
  SessionService,
  formaterDate,
  indexParId,
  labelReparateur,
  libelleStatut,
  messageErreur,
} from '@valorio/shared';

@Component({
  selector: 'valorio-demande-detail',
  imports: [FormsModule, RouterLink],
  template: `
    <section class="page">
      <header class="page-header">
        <div class="flex-row">
          <a class="btn small secondary" routerLink="/reparation">â† Retour</a>
          <h1>Demande #{{ id }}</h1>
          @if (demande(); as d) {
            <span [class]="'badge ' + d.statut.toLowerCase()">{{ libelle(d.statut) }}</span>
          }
        </div>
        <span class="muted">ConnectÃ© : {{ session.label() }}</span>
      </header>

      @if (erreur()) {
        <div class="alert error">{{ erreur() }}</div>
      }
      @if (succes()) {
        <div class="alert success">{{ succes() }}</div>
      }

      @if (demande(); as d) {
        <div class="card">
          <div class="spread">
            <h2>Informations</h2>
          </div>
          <div class="grid-cards">
            <div class="tile">
              <h3>Objet #{{ d.objetId }}</h3>
              <span class="muted">{{ objet()?.intitule || 'Chargement...' }}</span>
              <span class="muted">Ã‰tat : {{ objet()?.etat || 'â€”' }}</span>
            </div>
            <div class="tile">
              <h3>Demandeur</h3>
              <span class="muted">{{ utilisateur()?.nom || 'Chargement...' }}</span>
              <span class="muted">Ville : {{ utilisateur()?.ville || 'â€”' }}</span>
            </div>
            <div class="tile">
              <h3>RÃ©parateur</h3>
              <span class="muted">{{ nomReparateur(d.reparateurId) }}</span>
              <span class="muted">CrÃ©Ã©e le {{ date(d.dateCreation) }}</span>
              @if (d.dateIntervention) {
                <span class="muted">Intervention : {{ date(d.dateIntervention) }}</span>
              }
            </div>
          </div>
          <p class="muted" style="margin-top:0.7rem">{{ d.descriptionPanne }}</p>
        </div>

        @if (peutUtiliserIa()) {
          <div class="card">
            <div class="spread">
              <h2>Assistant IA de diagnostic</h2>
              <button class="btn" (click)="lancerDiagnostic()" [disabled]="diagnosticEnCours()">
                {{ diagnosticEnCours() ? 'Analyse...' : 'Analyser la panne' }}
              </button>
            </div>
            @if (!diagnostic() && !diagnosticEnCours()) {
              <p class="empty">
                L'IA analyse la description de la panne et propose une estimation indicative du devis.
              </p>
            }
            @if (diagnostic(); as ia) {
              <div class="flex-row">
                <span [class]="'badge ' + ia.gravite.toLowerCase()">{{ libelleGravite(ia.gravite) }}</span>
                <span class="muted">{{ ia.categoriePanne }} Â· {{ ia.source }} Â· {{ pourcentage(ia.confiance) }}</span>
              </div>
              <p style="margin-top:0.7rem">{{ ia.diagnostic }}</p>
              <ul class="timeline">
                @for (cause of ia.causesProbables; track cause) {
                  <li>
                    <span class="when">Cause</span>
                    <span>{{ cause }}</span>
                  </li>
                }
                <li>
                  <span class="when">Devis estimÃ©</span>
                  <span>
                    {{ ia.devisEstime.montantMin }}â€“{{ ia.devisEstime.montantMax }}
                    {{ ia.devisEstime.devise }} Â· ~{{ ia.devisEstime.delaiJours }} jour(s)
                  </span>
                </li>
              </ul>
              @if (ia.reparateursRecommandes.length) {
                <h3 style="margin-top:1rem">RÃ©parateurs recommandÃ©s par l'IA</h3>
                <ul class="timeline">
                  @for (r of ia.reparateursRecommandes; track r.reparateurId) {
                    <li>
                      <span class="when">{{ nomReparateur(r.reparateurId) }}</span>
                      <span class="muted">Score {{ r.score }}{{ r.ville ? ' Â· ' + r.ville : '' }}</span>
                    </li>
                  }
                </ul>
              }
              <div class="actions" style="margin-top:0.9rem">
                <button class="btn secondary" (click)="utiliserDevisIa()">PrÃ©-remplir le devis</button>
              </div>
              @for (a of ia.avertissements; track a) {
                <p class="muted">{{ a }}</p>
              }
            }
          </div>
        }

        <div class="card">
          <div class="spread">
            <h2>Devis</h2>
            @if (devis()) {
              <span [class]="'badge ' + devis()!.statut.toLowerCase()">{{
                libelle(devis()!.statut)
              }}</span>
            }
          </div>

          @if (devis(); as dev) {
            <div class="timeline">
              <li>
                <span class="when">Montant</span>
                <span>{{ dev.montant }} DT</span>
              </li>
              <li>
                <span class="when">DÃ©lai</span>
                <span>{{ dev.delaiJours ?? 'â€”' }} jour(s)</span>
              </li>
              <li>
                <span class="when">Commentaire</span>
                <span>{{ dev.commentaire || 'â€”' }}</span>
              </li>
            </div>
            @if (dev.statut === 'EN_ATTENTE' && peutRepondreDevis()) {
              <div class="actions" style="margin-top:0.9rem">
                <button class="btn success" (click)="repondreDevis(true)" [disabled]="enCours()">
                  Accepter le devis
                </button>
                <button class="btn danger" (click)="repondreDevis(false)" [disabled]="enCours()">
                  Refuser
                </button>
              </div>
            }
          } @else if (d.statut === 'DEMANDE' && peutCreerDevis()) {
            <form class="toolbar" (ngSubmit)="creerDevis()">
              <label class="field">
                <span>Montant (DT)</span>
                <input type="number" name="montant" [(ngModel)]="devisForm.montant" min="0" step="0.5" required />
              </label>
              <label class="field">
                <span>DÃ©lai (jours)</span>
                <input type="number" name="delaiJours" [(ngModel)]="devisForm.delaiJours" />
              </label>
              @if (estAdmin()) {
                <label class="field">
                  <span>RÃ©parateur</span>
                  <select name="reparateurId" [(ngModel)]="devisForm.reparateurId">
                    <option [ngValue]="undefined">Non assignÃ©</option>
                    @for (r of reparateurs(); track r.id) {
                      <option [ngValue]="r.id">{{ optionReparateur(r) }}</option>
                    }
                  </select>
                </label>
              }
              <label class="field" style="flex:1">
                <span>Commentaire</span>
                <input type="text" name="commentaire" [(ngModel)]="devisForm.commentaire" />
              </label>
              <button class="btn" type="submit" [disabled]="enCours()">Proposer le devis</button>
            </form>
          } @else if (d.statut === 'DEMANDE') {
            <p class="empty">En attente d'un devis de la part d'un rÃ©parateur.</p>
          } @else {
            <p class="empty">Aucun devis pour cette demande.</p>
          }
        </div>

        @if (peutPlanifier(d.statut)) {
          <div class="card">
            <h2>Date d'intervention</h2>
            <p class="muted">
              @if (d.dateIntervention) {
                Intervention planifiÃ©e le {{ date(d.dateIntervention) }}.
              } @else {
                Aucune date d'intervention choisie.
              }
            </p>
            <form class="toolbar" (ngSubmit)="planifierIntervention()">
              <label class="field">
                <span>Date et heure</span>
                <input type="datetime-local" name="dateIntervention" [(ngModel)]="dateIntervention" required />
              </label>
              <button class="btn" type="submit" [disabled]="enCours()">Confirmer l'intervention</button>
            </form>
          </div>
        }

        <div class="card">
          <div class="spread">
            <h2>Workflow</h2>
            @if (peutAvancer() && prochain()) {
              <button class="btn" (click)="avancerStatut()" [disabled]="enCours()">
                Passer Ã  {{ libelle(prochain()!) }}
              </button>
            }
          </div>
          @if (!prochain()) {
            <p class="empty">Demande terminÃ©e ({{ libelle(d.statut) }}).</p>
          } @else if (!peutAvancer()) {
            <p class="empty">
              Seul le rÃ©parateur (ou un administrateur) peut faire Ã©voluer le statut de la demande.
            </p>
          }

          <h3 style="margin-top:1rem">Historique</h3>
          <ul class="timeline">
            @for (h of d.historiqueStatuts ?? []; track h.date) {
              <li>
                <span class="when">{{ date(h.date) }}</span>
                <span [class]="'badge ' + h.statut.toLowerCase()">{{ libelle(h.statut) }}</span>
                <span class="muted">{{ h.commentaire || '' }}</span>
              </li>
            }
          </ul>
        </div>
      } @else {
        <p class="empty">Chargement de la demande...</p>
      }

      <a class="btn secondary" routerLink="/reparation">Retour Ã  la liste</a>
    </section>
  `,
})
export class DemandeDetail implements OnInit {
  private readonly api = inject(ReparationApiService);
  private readonly route = inject(ActivatedRoute);
  protected readonly session = inject(SessionService);

  readonly demande = signal<DemandeReparation | null>(null);
  readonly devis = signal<Devis | null>(null);
  readonly objet = signal<{ intitule: string; etat: string } | null>(null);
  readonly utilisateur = signal<{ nom: string; ville: string } | null>(null);
  readonly reparateurs = signal<Reparateur[]>([]);
  readonly erreur = signal<string | null>(null);
  readonly succes = signal<string | null>(null);
  readonly diagnostic = signal<DiagnosticIaResponse | null>(null);
  readonly diagnosticEnCours = signal(false);
  readonly enCours = signal(false);

  private readonly reparateursIndex = computed(() => indexParId(this.reparateurs()));

  readonly libelle = libelleStatut;
  readonly date = formaterDate;
  protected readonly estAdmin = this.session.estAdmin;
  protected readonly peutCreerDevis = computed(() => this.session.est('REPARATEUR', 'ADMIN'));
  protected readonly peutRepondreDevis = computed(() => this.session.est('UTILISATEUR', 'ADMIN'));
  protected readonly peutAvancer = computed(() => this.session.est('REPARATEUR', 'ADMIN'));
  protected readonly peutUtiliserIa = computed(() => this.session.est('REPARATEUR', 'ADMIN'));


  dateIntervention = '';

  private demandeId = 0;

  get id(): number {
    return this.demandeId;
  }

  devisForm: { montant: number; delaiJours?: number; commentaire?: string; reparateurId?: number } = {
    montant: 0,
  };

  prochain(): 'DEMANDE' | 'ACCEPTEE' | 'DIAGNOSTIC' | 'EN_REPARATION' | 'TERMINEE' | 'LIVREE' | null {
    const demande = this.demande();
    return demande ? nextStatut(demande.statut) : null;
  }

  ngOnInit(): void {
    this.demandeId = Number(this.route.snapshot.paramMap.get('id'));
    this.chargerReparateurs();
    this.charger();
  }

  optionReparateur(reparateur: Reparateur): string {
    const details = [reparateur.competences, reparateur.ville].filter(Boolean).join(' Â· ');
    return details ? `#${reparateur.id} Â· ${details}` : `RÃ©parateur #${reparateur.id}`;
  }

  nomReparateur(id?: number | null): string {
    if (id == null) {
      return 'Non assignÃ©';
    }
    return labelReparateur(this.reparateursIndex().get(id), id);
  }

  peutPlanifier(statut: DemandeReparation['statut']): boolean {
    return this.session.est('UTILISATEUR', 'ADMIN') && statut !== 'DEMANDE' && statut !== 'LIVREE';
  }

  planifierIntervention(): void {
    if (!this.dateIntervention) {
      this.erreur.set("Choisis une date et une heure d'intervention.");
      return;
    }
    this.erreur.set(null);
    this.succes.set(null);
    this.enCours.set(true);
    this.api.planifierIntervention(this.demandeId, new Date(this.dateIntervention).toISOString()).subscribe({
      next: () => {
        this.enCours.set(false);
        this.succes.set('Intervention planifiÃ©e.');
        this.dateIntervention = '';
        this.charger();
      },
      error: (err) => {
        this.enCours.set(false);
        this.erreur.set(messageErreur(err));
      },
    });
  }

  charger(): void {
    this.erreur.set(null);
    this.api.getDemande(this.demandeId).subscribe({
      next: (demande) => {
        this.demande.set(demande);
        this.chargerObjet(demande.objetId);
        this.chargerUtilisateur(demande.utilisateurId);
        this.chargerDevis();
      },
      error: (err) => this.erreur.set(messageErreur(err)),
    });
  }

  private chargerObjet(id: number): void {
    this.api.getObjet(id).subscribe({
      next: (objet) => this.objet.set({ intitule: objet.intitule, etat: objet.etat }),
      error: () => this.objet.set(null),
    });
  }

  private chargerUtilisateur(id: number): void {
    this.api.getUtilisateur(id).subscribe({
      next: (u) => this.utilisateur.set({ nom: u.nom, ville: u.ville }),
      error: () => this.utilisateur.set(null),
    });
  }

  private chargerReparateurs(): void {
    this.api.rechercherReparateurs().subscribe({
      next: (reparateurs) => this.reparateurs.set(reparateurs),
      error: () => this.reparateurs.set([]),
    });
  }

  private chargerDevis(): void {
    this.devis.set(null);
    this.api.getDevis(this.demandeId).subscribe({
      next: (devis) => this.devis.set(devis),
      error: () => this.devis.set(null),
    });
  }

  creerDevis(): void {
    this.erreur.set(null);
    this.succes.set(null);
    if (!this.devisForm.montant || this.devisForm.montant <= 0) {
      this.erreur.set('Le montant doit Ãªtre supÃ©rieur Ã  0.');
      return;
    }
    const payload = this.estAdmin()
      ? this.devisForm
      : { ...this.devisForm, reparateurId: this.session.reparateurId() ?? undefined };
    this.enCours.set(true);
    this.api.creerDevis(this.demandeId, payload).subscribe({
      next: () => {
        this.enCours.set(false);
        this.succes.set('Devis proposÃ©.');
        this.devisForm = { montant: 0 };
        this.charger();
      },
      error: (err) => {
        this.enCours.set(false);
        this.erreur.set(messageErreur(err));
      },
    });
  }

  repondreDevis(accepte: boolean): void {
    const devis = this.devis();
    if (!devis) {
      return;
    }
    this.erreur.set(null);
    this.enCours.set(true);
    this.api.repondreDevis(devis.id, accepte).subscribe({
      next: () => {
        this.enCours.set(false);
        this.succes.set(accepte ? 'Devis acceptÃ©.' : 'Devis refusÃ©.');
        this.charger();
      },
      error: (err) => {
        this.enCours.set(false);
        this.erreur.set(messageErreur(err));
      },
    });
  }

  avancer(): void {
    const next = this.prochainStatut(this.demande()!.statut);
    if (!next) {
      return;
    }
    this.enCours.set(true);
    this.api.avancerStatut(this.id, next).subscribe({
      next: (demande) => {
        this.demande.set(demande);
        this.enCours.set(false);
      },
      error: () => this.enCours.set(false),
    });
  }

  private prochainStatut(statut: DemandeReparation['statut']): DemandeReparation['statut'] | null {
    const ordre: DemandeReparation['statut'][] = [
      'DEMANDE',
      'ACCEPTEE',
      'DIAGNOSTIC',
      'EN_REPARATION',
      'TERMINEE',
      'LIVREE',
    ];
    const index = ordre.indexOf(statut);
    return index >= 0 && index < ordre.length - 1 ? ordre[index + 1] : null;
  }

  avancerStatut(): void {
    this.avancer();
  }

  libelleGravite(gravite: DiagnosticIaResponse['gravite']): string {
    return gravite.charAt(0).toUpperCase() + gravite.slice(1).toLowerCase();
  }

  pourcentage(valeur: number): string {
    return (Math.round(valeur * 100) / 100).toString().replace('.', ',') + '%';
  }

  utiliserDevisIa(): void {
    const ia = this.diagnostic();
    if (!ia) {
      return;
    }
    this.devisForm = {
      montant: Math.round(ia.devisEstime.montantMin),
      commentaire: ia.diagnostic.slice(0, 180),
    };
  }

  lancerDiagnostic(): void {
    this.erreur.set(null);
    this.diagnosticEnCours.set(true);
    const d = this.demande();
    this.api
      .diagnostiquer({
        objetId: d?.objetId,
        descriptionPanne: d?.descriptionPanne ?? '',
        intitule: this.objet()?.intitule,
        etat: this.objet()?.etat,
        ville: this.utilisateur()?.ville,
      })
      .subscribe({
        next: (res) => {
          this.diagnostic.set(res);
          this.diagnosticEnCours.set(false);
        },
        error: (err) => {
          this.diagnosticEnCours.set(false);
          this.erreur.set(messageErreur(err));
        },
      });
  }
}
function nextStatut(statut: DemandeReparation['statut']): DemandeReparation['statut'] | null {
  const ordre: DemandeReparation['statut'][] = [
    'DEMANDE',
    'ACCEPTEE',
    'DIAGNOSTIC',
    'EN_REPARATION',
    'TERMINEE',
    'LIVREE',
  ];
  const index = ordre.indexOf(statut);
  return index >= 0 && index < ordre.length - 1 ? ordre[index + 1] : null;
}
