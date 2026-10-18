import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import {
  DemandeReparation,
  Devis,
  Objet,
  Reparateur,
  StatutDemande,
  Utilisateur,
  VALORIO_ENV,
} from '@valorio/shared';

export interface FiltreDemandes {
  statut?: StatutDemande;
  utilisateurId?: number;
  reparateurId?: number;
}

export interface CreerDevisPayload {
  montant: number;
  delaiJours?: number;
  commentaire?: string;
  reparateurId?: number;
}

@Injectable({ providedIn: 'root' })
export class ReparationApiService {
  private readonly http = inject(HttpClient);
  private readonly base = VALORIO_ENV.api.reparation;

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

  listerDemandes(filtre: FiltreDemandes = {}): Observable<DemandeReparation[]> {
    let params = new HttpParams();
    if (filtre.statut) {
      params = params.set('statut', filtre.statut);
    }
    if (filtre.utilisateurId != null) {
      params = params.set('utilisateurId', filtre.utilisateurId);
    }
    if (filtre.reparateurId != null) {
      params = params.set('reparateurId', filtre.reparateurId);
    }
    return this.http.get<DemandeReparation[]>(`${this.base}/api/demandes`, { params });
  }

  getDemande(id: number): Observable<DemandeReparation> {
    return this.http.get<DemandeReparation>(`${this.base}/api/demandes/${id}`);
  }

  creerDemande(
    objetId: number,
    utilisateurId: number,
    descriptionPanne: string,
  ): Observable<DemandeReparation> {
    return this.http.post<DemandeReparation>(`${this.base}/api/demandes`, {
      objetId,
      utilisateurId,
      descriptionPanne,
    });
  }

  changerStatutDemande(
    id: number,
    statut: StatutDemande,
    commentaire?: string,
  ): Observable<DemandeReparation> {
    return this.http.put<DemandeReparation>(`${this.base}/api/demandes/${id}/statut`, {
      statut,
      commentaire,
    });
  }

  planifierIntervention(id: number, dateIntervention: string): Observable<DemandeReparation> {
    return this.http.put<DemandeReparation>(`${this.base}/api/demandes/${id}/intervention`, {
      dateIntervention,
    });
  }

  creerDevis(demandeId: number, payload: CreerDevisPayload): Observable<Devis> {
    return this.http.post<Devis>(`${this.base}/api/demandes/${demandeId}/devis`, payload);
  }

  getDevis(demandeId: number): Observable<Devis> {
    return this.http.get<Devis>(`${this.base}/api/demandes/${demandeId}/devis`);
  }

  repondreDevis(devisId: number, accepte: boolean): Observable<Devis> {
    return this.http.put<Devis>(`${this.base}/api/devis/${devisId}/reponse`, { accepte });
  }

  rechercherReparateurs(filtre: {
    categorie?: string;
    competence?: string;
    ville?: string;
  } = {}): Observable<Reparateur[]> {
    let params = new HttpParams();
    if (filtre.categorie) {
      params = params.set('categorie', filtre.categorie);
    }
    if (filtre.competence) {
      params = params.set('competence', filtre.competence);
    }
    if (filtre.ville) {
      params = params.set('ville', filtre.ville);
    }
    return this.http.get<Reparateur[]>(`${this.base}/api/reparateurs/recherche`, { params });
  }

  getReparateur(id: number): Observable<Reparateur> {
    return this.http.get<Reparateur>(`${this.base}/api/reparateurs/${id}`);
  }
}
