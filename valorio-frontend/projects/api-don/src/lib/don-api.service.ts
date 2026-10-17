import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import {
  AnnonceDon,
  CorrespondanceDon,
  DemandeDon,
  Objet,
  StatutAnnonceDon,
  StatutDemandeDon,
  Utilisateur,
  VALORIO_ENV,
} from '@valorio/shared';

export interface AnnonceDonPayload {
  objetId: number;
  donneurId: number;
  titre: string;
  description?: string;
  conditions?: string;
  ville?: string;
  latitude?: number;
  longitude?: number;
}

export interface AnnonceDonFiltre {
  categorieId?: number;
  ville?: string;
  statut?: StatutAnnonceDon;
}

export interface MatchingFiltre {
  categorieId?: number;
  latitude?: number;
  longitude?: number;
  rayonKm?: number;
}

@Injectable({ providedIn: 'root' })
export class DonApiService {
  private readonly http = inject(HttpClient);
  private readonly base = VALORIO_ENV.api.don;

  readonly useStaticApis = VALORIO_ENV.useStaticApis;

  getObjet(id: number): Observable<Objet> {
    return this.http.get<Objet>(`${this.base}/api/objets/${id}`);
  }

  listerObjets(categorieId?: number): Observable<Objet[]> {
    let params = new HttpParams();
    if (categorieId != null) {
      params = params.set('categorieId', categorieId);
    }
    return this.http.get<Objet[]>(`${this.base}/api/objets`, { params });
  }

  getUtilisateur(id: number): Observable<Utilisateur> {
    return this.http.get<Utilisateur>(`${this.base}/api/utilisateurs/${id}`);
  }

  listerUtilisateurs(): Observable<Utilisateur[]> {
    return this.http.get<Utilisateur[]>(`${this.base}/api/utilisateurs`);
  }

  listerAnnonces(filtre: AnnonceDonFiltre = {}): Observable<AnnonceDon[]> {
    let params = new HttpParams();
    if (filtre.categorieId != null) {
      params = params.set('categorieId', filtre.categorieId);
    }
    if (filtre.ville) {
      params = params.set('ville', filtre.ville);
    }
    if (filtre.statut) {
      params = params.set('statut', filtre.statut);
    }
    return this.http.get<AnnonceDon[]>(`${this.base}/api/dons`, { params });
  }

  getAnnonce(id: number): Observable<AnnonceDon> {
    return this.http.get<AnnonceDon>(`${this.base}/api/dons/${id}`);
  }

  publierAnnonce(payload: AnnonceDonPayload): Observable<AnnonceDon> {
    return this.http.post<AnnonceDon>(`${this.base}/api/dons`, payload);
  }

  modifierAnnonce(id: number, payload: AnnonceDonPayload): Observable<AnnonceDon> {
    return this.http.put<AnnonceDon>(`${this.base}/api/dons/${id}`, payload);
  }

  supprimerAnnonce(id: number, donneurId?: number): Observable<void> {
    let params = new HttpParams();
    if (donneurId != null) {
      params = params.set('donneurId', donneurId);
    }
    return this.http.delete<void>(`${this.base}/api/dons/${id}`, { params });
  }

  annulerAnnonce(id: number, donneurId?: number): Observable<AnnonceDon> {
    let params = new HttpParams();
    if (donneurId != null) {
      params = params.set('donneurId', donneurId);
    }
    return this.http.put<AnnonceDon>(`${this.base}/api/dons/${id}/annuler`, null, { params });
  }

  matching(filtre: MatchingFiltre = {}): Observable<CorrespondanceDon[]> {
    let params = new HttpParams();
    if (filtre.categorieId != null) {
      params = params.set('categorieId', filtre.categorieId);
    }
    if (filtre.latitude != null) {
      params = params.set('latitude', filtre.latitude);
    }
    if (filtre.longitude != null) {
      params = params.set('longitude', filtre.longitude);
    }
    if (filtre.rayonKm != null) {
      params = params.set('rayonKm', filtre.rayonKm);
    }
    return this.http.get<CorrespondanceDon[]>(`${this.base}/api/dons/matching`, { params });
  }

  demander(annonceId: number, beneficiaireId: number, message?: string): Observable<DemandeDon> {
    return this.http.post<DemandeDon>(`${this.base}/api/dons/${annonceId}/demandes`, {
      beneficiaireId,
      message,
    });
  }

  demandesDeAnnonce(annonceId: number): Observable<DemandeDon[]> {
    return this.http.get<DemandeDon[]>(`${this.base}/api/dons/${annonceId}/demandes`);
  }

  getDemande(id: number): Observable<DemandeDon> {
    return this.http.get<DemandeDon>(`${this.base}/api/demandes-don/${id}`);
  }

  changerStatutDemande(
    id: number,
    statut: StatutDemandeDon,
    commentaire?: string,
  ): Observable<DemandeDon> {
    return this.http.put<DemandeDon>(`${this.base}/api/demandes-don/${id}/statut`, {
      statut,
      commentaire,
    });
  }
}
