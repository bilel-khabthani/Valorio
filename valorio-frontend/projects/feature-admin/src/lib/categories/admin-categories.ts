import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Categorie, messageErreur } from '@valorio/shared';
import { AdminApiService, CategoriePayload } from '@valorio/api-admin';

type BrouillonCategorie = CategoriePayload & { id?: number };

@Component({
  selector: 'app-admin-categories',
  standalone: true,
  imports: [FormsModule],
  template: `
    <div class="page">
      <div class="page-header">
        <div>
          <h1>Catégories</h1>
          <p>Référentiel des catégories d'objets utilisées dans la plateforme.</p>
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
        <h2>{{ brouillon.id ? 'Modifier la catégorie #' + brouillon.id : 'Nouvelle catégorie' }}</h2>
        <label class="field">
          <span>Nom *</span>
          <input name="nom" [(ngModel)]="brouillon.nom" />
        </label>
        <label class="field">
          <span>Description</span>
          <textarea name="description" [(ngModel)]="brouillon.description"></textarea>
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
        } @else if (categories().length === 0) {
          <p class="empty">Aucune catégorie.</p>
        } @else {
          <table>
            <thead>
              <tr>
                <th>Nom</th>
                <th>Description</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              @for (c of categories(); track c.id) {
                <tr>
                  <td>{{ c.nom }}</td>
                  <td>{{ c.description || '—' }}</td>
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
export class AdminCategories {
  private readonly adminApi = inject(AdminApiService);

  protected readonly categories = signal<Categorie[]>([]);
  protected readonly chargement = signal(true);
  protected readonly enregistrement = signal(false);
  protected readonly erreur = signal<string | null>(null);
  protected readonly message = signal<string | null>(null);

  protected brouillon: BrouillonCategorie = this.vide();

  constructor() {
    this.charger();
  }

  private vide(): BrouillonCategorie {
    return { nom: '', description: '' };
  }

  protected charger(): void {
    this.chargement.set(true);
    this.erreur.set(null);
    this.adminApi.listerCategories().subscribe({
      next: (liste) => {
        this.categories.set(liste);
        this.chargement.set(false);
      },
      error: (err) => {
        this.erreur.set(messageErreur(err));
        this.chargement.set(false);
      },
    });
  }

  protected editer(c: Categorie): void {
    this.brouillon = { id: c.id, nom: c.nom, description: c.description ?? '' };
    this.erreur.set(null);
    this.message.set(null);
  }

  protected reinitialiser(): void {
    this.brouillon = { nom: '', description: '' };
  }

  protected enregistrer(): void {
    if (!this.brouillon.nom.trim()) {
      this.erreur.set('Le nom de la catégorie est obligatoire.');
      return;
    }
    const payload: CategoriePayload = {
      nom: this.brouillon.nom.trim(),
      description: this.brouillon.description?.trim() || undefined,
    };
    const id = this.brouillon.id;
    this.enregistrement.set(true);
    const requete = id
      ? this.adminApi.modifierCategorie(id, payload)
      : this.adminApi.creerCategorie(payload);
    requete.subscribe({
      next: () => {
        this.message.set(id ? 'Catégorie mise à jour.' : 'Catégorie créée.');
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

  protected supprimer(c: Categorie): void {
    if (!confirm(`Supprimer la catégorie « ${c.nom} » ?`)) {
      return;
    }
    this.adminApi.supprimerCategorie(c.id).subscribe({
      next: () => {
        this.message.set(`Catégorie « ${c.nom} » supprimée.`);
        this.charger();
      },
      error: (err) => this.erreur.set(messageErreur(err)),
    });
  }
}
