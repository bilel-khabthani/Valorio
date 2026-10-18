import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import {
  Categorie,
  DashboardStats,
  DiagnosticObjet,
  EtatObjet,
  EvenementHistorique,
  Objet,
  RecommandationObjet,
  StatutObjet,
  TypeEvenement,
  Utilisateur,
  VALORIO_ENV,
} from '@valorio/shared';

export interface ObjetFiltre {
  categorieId?: number;
  etat?: EtatObjet;
  ville?: string;
  statut?: StatutObjet;
  q?: string;
}

export interface ObjetPayload {
  intitule: string;
  description?: string;
  categorieId: number;
  etat: EtatObjet;
  ageAnnees?: number;
  valeurEstimee?: number;
  ville?: string;
  latitude?: number;
  longitude?: number;
  materiaux?: string[];
  proprietaireId: number;
}

export interface CategoriePayload {
  nom: string;
  description?: string;
}

export interface DiagnosticPayload {
  panneDeclaree?: string;
  problemes?: string[];
}

export interface EvenementPayload {
  type: TypeEvenement;
  libelle: string;
  acteurId?: number;
  statutCible?: StatutObjet;
}

@Injectable({ providedIn: 'root' })
export class ObjetsApiService {
  private readonly http = inject(HttpClient);
  private readonly base = VALORIO_ENV.api.objets;

  listerObjets(filtre: ObjetFiltre = {}): Observable<Objet[]> {
    let params = new HttpParams();
    if (filtre.categorieId != null) {
      params = params.set('categorieId', filtre.categorieId);
    }
    if (filtre.etat) {
      params = params.set('etat', filtre.etat);
    }
    if (filtre.ville) {
      params = params.set('ville', filtre.ville);
    }
    if (filtre.statut) {
      params = params.set('statut', filtre.statut);
    }
    if (filtre.q) {
      params = params.set('q', filtre.q);
    }
    return this.http.get<Objet[]>(`${this.base}/api/objets`, { params });
  }

  villes(): Observable<string[]> {
    return this.http.get<string[]>(`${this.base}/api/objets/villes`);
  }

  getObjet(id: number): Observable<Objet> {
    return this.http.get<Objet>(`${this.base}/api/objets/${id}`);
  }

  declarer(payload: ObjetPayload): Observable<Objet> {
    return this.http.post<Objet>(`${this.base}/api/objets`, payload);
  }

  modifier(id: number, payload: ObjetPayload): Observable<Objet> {
    return this.http.put<Objet>(`${this.base}/api/objets/${id}`, payload);
  }

  supprimer(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/api/objets/${id}`);
  }

  changerStatut(
    id: number,
    statut: StatutObjet,
    commentaire?: string,
    acteurId?: number,
  ): Observable<Objet> {
    let params = new HttpParams();
    if (acteurId != null) {
      params = params.set('acteurId', acteurId);
    }
    return this.http.put<Objet>(`${this.base}/api/objets/${id}/statut`, { statut, commentaire }, { params });
  }

  ajouterPhotos(id: number, photos: File[]): Observable<Objet> {
    const form = new FormData();
    for (const photo of photos) {
      form.append('photos', photo, photo.name);
    }
    return this.http.post<Objet>(`${this.base}/api/objets/${id}/photos`, form);
  }

  supprimerPhoto(id: number, photoId: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/api/objets/${id}/photos/${photoId}`);
  }

  historique(id: number): Observable<EvenementHistorique[]> {
    return this.http.get<EvenementHistorique[]>(`${this.base}/api/objets/${id}/historique`);
  }

  enregistrerEvenement(id: number, payload: EvenementPayload): Observable<EvenementHistorique> {
    return this.http.post<EvenementHistorique>(`${this.base}/api/objets/${id}/historique`, payload);
  }

  listerCategories(): Observable<Categorie[]> {
    return this.http.get<Categorie[]>(`${this.base}/api/categories`);
  }

  creerCategorie(payload: CategoriePayload): Observable<Categorie> {
    return this.http.post<Categorie>(`${this.base}/api/categories`, payload);
  }

  diagnostiquer(id: number, payload: DiagnosticPayload): Observable<DiagnosticObjet> {
    return this.http.post<DiagnosticObjet>(`${this.base}/api/objets/${id}/diagnostics`, payload);
  }

  listerDiagnostics(id: number): Observable<DiagnosticObjet[]> {
    return this.http.get<DiagnosticObjet[]>(`${this.base}/api/objets/${id}/diagnostics`);
  }

  recommandation(id: number): Observable<RecommandationObjet> {
    return this.http.get<RecommandationObjet>(`${this.base}/api/objets/${id}/recommandation`);
  }

  stats(): Observable<DashboardStats> {
    return this.http.get<DashboardStats>(`${this.base}/api/stats`);
  }

  listerUtilisateurs(): Observable<Utilisateur[]> {
    return this.http.get<Utilisateur[]>(VALORIO_ENV.api.users);
  }

  urlPhoto(photo: string): string {
    if (/^https?:\/\//.test(photo)) {
      return photo;
    }
    return `${this.base}${photo.startsWith('/') ? '' : '/'}${photo}`;
  }
}
