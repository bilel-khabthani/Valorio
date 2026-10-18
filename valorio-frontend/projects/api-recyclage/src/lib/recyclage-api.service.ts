import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import {
  AnalyseRecyclabilite,
  Categorie,
  CentreRecyclage,
  DemandeCollecte,
  Objet,
  StatutCollecte,
  Utilisateur,
  VALORIO_ENV,
} from '@valorio/shared';

export interface CentreRecyclagePayload {
  organismeId?: number;
  nom: string;
  ville?: string;
  adresse?: string;
  latitude?: number;
  longitude?: number;
  materiauxAcceptes: string[];
  horaires?: string;
}

export interface RechercheCentreFiltre {
  materiau?: string;
  latitude?: number;
  longitude?: number;
  rayonKm?: number;
}

export interface CreerCollectePayload {
  objetId: number;
  utilisateurId: number;
  centreId?: number;
  datePrevue?: string;
}

export interface CollecteFiltre {
  statut?: StatutCollecte;
  utilisateurId?: number;
  centreId?: number;
}

@Injectable({ providedIn: 'root' })
export class RecyclageApiService {
  private readonly http = inject(HttpClient);
  private readonly base = VALORIO_ENV.api.recyclage;

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

  getCategorie(id: number): Observable<Categorie> {
    return this.http.get<Categorie>(`${this.base}/api/categories/${id}`);
  }

  analyserRecyclabilite(objetId: number): Observable<AnalyseRecyclabilite> {
    return this.http.get<AnalyseRecyclabilite>(`${this.base}/api/objets/${objetId}/recyclabilite`);
  }

  rechercherCentres(filtre: RechercheCentreFiltre = {}): Observable<CentreRecyclage[]> {
    let params = new HttpParams();
    if (filtre.materiau) {
      params = params.set('materiau', filtre.materiau);
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
    return this.http.get<CentreRecyclage[]>(`${this.base}/api/centres`, { params });
  }

  getCentre(id: number): Observable<CentreRecyclage> {
    return this.http.get<CentreRecyclage>(`${this.base}/api/centres/${id}`);
  }

  creerCentre(payload: CentreRecyclagePayload): Observable<CentreRecyclage> {
    return this.http.post<CentreRecyclage>(`${this.base}/api/centres`, payload);
  }

  modifierCentre(id: number, payload: CentreRecyclagePayload): Observable<CentreRecyclage> {
    return this.http.put<CentreRecyclage>(`${this.base}/api/centres/${id}`, payload);
  }

  listerCollectes(filtre: CollecteFiltre = {}): Observable<DemandeCollecte[]> {
    let params = new HttpParams();
    if (filtre.statut) {
      params = params.set('statut', filtre.statut);
    }
    if (filtre.utilisateurId != null) {
      params = params.set('utilisateurId', filtre.utilisateurId);
    }
    if (filtre.centreId != null) {
      params = params.set('centreId', filtre.centreId);
    }
    return this.http.get<DemandeCollecte[]>(`${this.base}/api/collectes`, { params });
  }

  getCollecte(id: number): Observable<DemandeCollecte> {
    return this.http.get<DemandeCollecte>(`${this.base}/api/collectes/${id}`);
  }

  demanderCollecte(payload: CreerCollectePayload): Observable<DemandeCollecte> {
    return this.http.post<DemandeCollecte>(`${this.base}/api/collectes`, payload);
  }

  changerStatutCollecte(
    id: number,
    statut: StatutCollecte,
    datePrevue?: string,
    commentaire?: string,
  ): Observable<DemandeCollecte> {
    return this.http.put<DemandeCollecte>(`${this.base}/api/collectes/${id}/statut`, {
      statut,
      datePrevue,
      commentaire,
    });
  }
}
