import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CentreRecyclage, messageErreur } from '@valorio/shared';
import { AdminApiService, CentrePayload } from '@valorio/api-admin';

type BrouillonCentre = CentrePayload & { id?: number };

@Component({
  selector: 'app-admin-organismes',
  standalone: true,
  imports: [FormsModule],
  template: `
    <div class="page">
      <div class="page-header">
        <div>
          <h1>Organismes de recyclage</h1>
          <p>Gérer les centres de collecte et leurs matières acceptées.</p>
        </div>
        <button class="btn secondary" type="button" (click)="charger()" [disabled]="chargement()">
          Rafraîchir
        </button>
      </div>

      @if (message(); as m) {
        <div class="alert success">{{ m }}</div>
      }
      @if (erreur(); as e) {
        <div class="alert error">{{ e }}</div>
      }

      <div class="card">
        <h2>{{ brouillon.id ? 'Modifier le centre #' + brouillon.id : 'Nouveau centre' }}</h2>
        <div class="toolbar">
          <label class="field">
            <span>Nom *</span>
            <input name="nom" [(ngModel)]="brouillon.nom" />
          </label>
          <label class="field">
            <span>Ville</span>
            <input name="ville" [(ngModel)]="brouillon.ville" />
          </label>
          <label class="field">
            <span>Adresse</span>
            <input name="adresse" [(ngModel)]="brouillon.adresse" />
          </label>
          <label class="field">
            <span>Horaires</span>
            <input name="horaires" [(ngModel)]="brouillon.horaires" />
          </label>
        </div>
        <label class="field">
          <span>Matières acceptées (séparées par des virgules) *</span>
          <input name="materiaux" [(ngModel)]="materiauxTexte" placeholder="Plastique, Verre, Métal" />
        </label>
        <div class="actions">
          <button class="btn" type="button" (click)="enregistrer()" [disabled]="enregistrement()">
            {{ brouillon.id ? 'Enregistrer' : 'Créer' }}
          </button>
          @if (brouillon.id) {
            <button class="btn secondary" type="button" (click)="reinitialiser()">Annuler</button>
          }
        </div>
      </div>

      <div class="card">
        @if (chargement()) {
          <p class="muted">Chargement…</p>
        } @else if (centres().length === 0) {
          <p class="empty">Aucun centre enregistré.</p>
        } @else {
          <table>
            <thead>
              <tr>
                <th>Nom</th>
                <th>Ville</th>
                <th>Matières</th>
                <th>Horaires</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              @for (c of centres(); track c.id) {
                <tr>
                  <td>{{ c.nom }}</td>
                  <td>{{ c.ville || '—' }}</td>
                  <td>{{ c.materiauxAcceptes.join(', ') || '—' }}</td>
                  <td>{{ c.horaires || '—' }}</td>
                  <td>
                    <div class="actions">
                      <button class="btn small secondary" type="button" (click)="editer(c)">Modifier</button>
                      <button class="btn small danger" type="button" (click)="supprimer(c)">Supprimer</button>
                    </div>
                  </td>
                </tr>
              }
            </tbody>
          </table>
        }
      </div>
    </div>
  `,
})
export class AdminOrganismes {
  private readonly adminApi = inject(AdminApiService);

  protected readonly centres = signal<CentreRecyclage[]>([]);
  protected readonly chargement = signal(true);
  protected readonly enregistrement = signal(false);
  protected readonly erreur = signal<string | null>(null);
  protected readonly message = signal<string | null>(null);

  protected brouillon: BrouillonCentre = this.vide();
  protected materiauxTexte = '';

  constructor() {
    this.charger();
  }

  private vide(): BrouillonCentre {
    return { nom: '', ville: '', adresse: '', horaires: '', materiauxAcceptes: [] };
  }

  protected charger(): void {
    this.chargement.set(true);
    this.erreur.set(null);
    this.adminApi
      .listerCentres()
      .subscribe({
        next: (liste) => {
          this.centres.set(liste);
          this.chargement.set(false);
        },
        error: (err) => {
          this.erreur.set(messageErreur(err));
          this.chargement.set(false);
        },
      });
  }

  protected editer(c: CentreRecyclage): void {
    this.brouillon = {
      id: c.id,
      organismeId: c.organismeId ?? undefined,
      nom: c.nom,
      ville: c.ville ?? '',
      adresse: c.adresse ?? '',
      latitude: c.latitude,
      longitude: c.longitude,
      horaires: c.horaires ?? '',
      materiauxAcceptes: [...c.materiauxAcceptes],
    };
    this.materiauxTexte = c.materiauxAcceptes.join(', ');
    this.erreur.set(null);
    this.message.set(null);
  }

  protected reinitialiser(): void {
    this.brouillon = this.vide();
    this.materiauxTexte = '';
  }

  protected enregistrer(): void {
    const materiaux = this.materiauxTexte
      .split(',')
      .map((m) => m.trim())
      .filter((m) => m.length > 0);

    if (!this.brouillon.nom.trim()) {
      this.erreur.set('Le nom du centre est obligatoire.');
      return;
    }
    if (materiaux.length === 0) {
      this.erreur.set('Au moins une matière acceptée est requise.');
      return;
    }

    const payload: CentrePayload = { ...this.brouillon, materiauxAcceptes: materiaux };
    this.enregistrement.set(true);
    const requete = this.brouillon.id
      ? this.adminApi.modifierCentre(this.brouillon.id, payload)
      : this.adminApi.creerCentre(payload);

    requete.subscribe({
      next: () => {
        this.message.set(this.brouillon.id ? 'Centre mis à jour.' : 'Centre créé.');
        this.reinitialiser();
        this.enregistrement.set(false);
        this.charger();
      },
      error: (err) => {
        this.erreur.set(messageErreur(err));
        this.enregistrement.set(false);
      },
    });
  }

  protected supprimer(c: CentreRecyclage): void {
    if (!confirm(`Supprimer le centre « ${c.nom} » ?`)) {
      return;
    }
    this.adminApi.supprimerCentre(c.id).subscribe({
      next: () => {
        this.message.set(`Centre « ${c.nom} » supprimé.`);
        this.charger();
      },
      error: (err) => this.erreur.set(messageErreur(err)),
    });
  }
}
