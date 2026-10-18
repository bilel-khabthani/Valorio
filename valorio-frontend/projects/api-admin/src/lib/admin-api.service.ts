import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import {
  AnnonceDon,
  Categorie,
  CentreRecyclage,
  DashboardStats,
  Objet,
  Reparateur,
  VALORIO_ENV,
} from '@valorio/shared';
import {
  MarketplaceItem,
  MarketplaceStats,
  ModerationRequest,
  ModerationResponse,
  ItemStatus,
} from '@valorio/api-marketplace';
import {
  CategoriePayload,
  CentrePayload,
  UserStats,
  UtilisateurAdmin,
  UtilisateurFiltre,
} from './admin.models';

/** Passerelle d'administration agrégée (modules 1, 2, 4, 6, 7). */
@Injectable({ providedIn: 'root' })
export class AdminApiService {
  private readonly http = inject(HttpClient);
  private readonly users = VALORIO_ENV.api.users;
  private readonly objets = `${VALORIO_ENV.api.objets}/api`;
  private readonly don = `${VALORIO_ENV.api.don}/api`;
  private readonly recyclage = `${VALORIO_ENV.api.recyclage}/api`;
  private readonly reparation = `${VALORIO_ENV.api.reparation}/api`;
  private readonly marketplace = `${VALORIO_ENV.api.marketplace}/api/marketplace`;

  /* -------------------------- Utilisateurs -------------------------- */

  listerUtilisateurs(filtre: UtilisateurFiltre = {}): Observable<UtilisateurAdmin[]> {
    let params = new HttpParams();
    if (filtre.acteur) {
      params = params.set('acteur', filtre.acteur);
    }
    if (filtre.actif != null) {
      params = params.set('actif', filtre.actif);
    }
    return this.http.get<UtilisateurAdmin[]>(this.users, { params });
  }

  statsUtilisateurs(): Observable<UserStats> {
    return this.http.get<UserStats>(`${this.users}/stats`);
  }

  changerActif(id: number, actif: boolean): Observable<UtilisateurAdmin> {
    return this.http.put<UtilisateurAdmin>(`${this.users}/${id}/actif`, null, {
      params: { actif },
    });
  }

  changerActeur(id: number, acteur: string): Observable<UtilisateurAdmin> {
    return this.http.put<UtilisateurAdmin>(`${this.users}/${id}/acteur`, null, {
      params: { acteur },
    });
  }

  supprimerUtilisateur(id: number): Observable<void> {
    return this.http.delete<void>(`${this.users}/${id}`);
  }

  /* --------------------------- Réparateurs -------------------------- */

  listerReparateurs(): Observable<Reparateur[]> {
    return this.http.get<Reparateur[]>(`${this.reparation}/reparateurs`);
  }

  changerDisponibilite(id: number, disponible: boolean): Observable<Reparateur> {
    return this.http.put<Reparateur>(`${this.reparation}/reparateurs/${id}/disponibilite`, null, {
      params: { disponible },
    });
  }

  supprimerReparateur(id: number): Observable<void> {
    return this.http.delete<void>(`${this.reparation}/reparateurs/${id}`);
  }

  /* ---------------------------- Organismes -------------------------- */

  listerCentres(): Observable<CentreRecyclage[]> {
    return this.http.get<CentreRecyclage[]>(`${this.recyclage}/centres`);
  }

  creerCentre(payload: CentrePayload): Observable<CentreRecyclage> {
    return this.http.post<CentreRecyclage>(`${this.recyclage}/centres`, payload);
  }

  modifierCentre(id: number, payload: CentrePayload): Observable<CentreRecyclage> {
    return this.http.put<CentreRecyclage>(`${this.recyclage}/centres/${id}`, payload);
  }

  supprimerCentre(id: number): Observable<void> {
    return this.http.delete<void>(`${this.recyclage}/centres/${id}`);
  }

  /* ---------------------------- Catégories -------------------------- */

  listerCategories(): Observable<Categorie[]> {
    return this.http.get<Categorie[]>(`${this.objets}/categories`);
  }

  creerCategorie(payload: CategoriePayload): Observable<Categorie> {
    return this.http.post<Categorie>(`${this.objets}/categories`, payload);
  }

  modifierCategorie(id: number, payload: CategoriePayload): Observable<Categorie> {
    return this.http.put<Categorie>(`${this.objets}/categories/${id}`, payload);
  }

  supprimerCategorie(id: number): Observable<void> {
    return this.http.delete<void>(`${this.objets}/categories/${id}`);
  }

  /* ---------------------------- Annonces ---------------------------- */

  listerAnnoncesDon(): Observable<AnnonceDon[]> {
    return this.http.get<AnnonceDon[]>(`${this.don}/dons`);
  }

  supprimerAnnonceDon(id: number): Observable<void> {
    return this.http.delete<void>(`${this.don}/dons/${id}`);
  }

  listerObjets(): Observable<Objet[]> {
    return this.http.get<Objet[]>(`${this.objets}/objets`);
  }

  supprimerObjet(id: number): Observable<void> {
    return this.http.delete<void>(`${this.objets}/objets/${id}`);
  }

  listerObjetsMarketplace(): Observable<MarketplaceItem[]> {
    return this.http.get<MarketplaceItem[]>(`${this.marketplace}/items`);
  }

  supprimerObjetMarketplace(id: number): Observable<void> {
    return this.http.delete<void>(`${this.marketplace}/items/${id}`);
  }

  changerStatutMarketplace(id: number, status: ItemStatus): Observable<MarketplaceItem> {
    return this.http.patch<MarketplaceItem>(`${this.marketplace}/items/${id}/status`, null, {
      params: { status },
    });
  }

  /* ------------------------- Statistiques --------------------------- */

  statsObjets(): Observable<DashboardStats> {
    return this.http.get<DashboardStats>(`${this.objets}/stats`);
  }

  statsMarketplace(): Observable<MarketplaceStats> {
    return this.http.get<MarketplaceStats>(`${this.marketplace}/items/stats`);
  }

  /* --------------------------- Modération --------------------------- */

  moderer(payload: ModerationRequest): Observable<ModerationResponse> {
    return this.http.post<ModerationResponse>(`${this.marketplace}/ai/moderate`, payload);
  }
}
