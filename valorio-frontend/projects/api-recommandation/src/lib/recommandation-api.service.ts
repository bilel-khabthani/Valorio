import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { messageErreur, VALORIO_ENV } from '@valorio/shared';
import {
  ActeursRequest,
  ExplicationResponse,
  FeedbackRequest,
  FeedbackResponse,
  HistoriqueReco,
  MarcheRequest,
  ModeleMetadata,
  RecoActeursResponse,
  RecoMarcheResponse,
  RecoValorisationResponse,
  ValorisationRequest,
} from './reco-models';

@Injectable({ providedIn: 'root' })
export class RecommandationApiService {
  private readonly http = inject(HttpClient);
  private readonly base = '/reco';

  recommanderValorisation(body: ValorisationRequest): Observable<RecoValorisationResponse> {
    return this.http.post<RecoValorisationResponse>(`${this.base}/api/recommandations/valorisation`, body);
  }

  recommanderActeurs(body: ActeursRequest): Observable<RecoActeursResponse> {
    return this.http.post<RecoActeursResponse>(`${this.base}/api/recommandations/acteurs`, body);
  }

  articlesSimilaires(body: MarcheRequest): Observable<RecoMarcheResponse> {
    return this.http.post<RecoMarcheResponse>(`${this.base}/api/recommandations/marche/similaires`, body);
  }

  envoyerFeedback(body: FeedbackRequest): Observable<FeedbackResponse> {
    return this.http.post<FeedbackResponse>(`${this.base}/api/recommandations/feedback`, body);
  }

  metadataModele(): Observable<ModeleMetadata> {
    return this.http.get<ModeleMetadata>(`${this.base}/api/recommandations/modele/metadata`);
  }

  historique(objetId?: number): Observable<HistoriqueReco[]> {
    let params = new HttpParams();
    if (objetId != null) {
      params = params.set('objetId', objetId);
    }
    return this.http.get<HistoriqueReco[]>(`${this.base}/api/recommandations/historique`, { params });
  }

  expliquer(id: number): Observable<ExplicationResponse> {
    return this.http.post<ExplicationResponse>(`${this.base}/api/recommandations/${id}/explication`, {});
  }
}
