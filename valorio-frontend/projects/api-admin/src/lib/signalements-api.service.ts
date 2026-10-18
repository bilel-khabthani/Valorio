import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import {
  Signalement,
  SignalementFiltre,
  SignalementRequest,
  SignalementStats,
  TraitementSignalementRequest,
} from './admin.models';

/** Signalements (module 1, sous-module 1.3), hébergés par service-auth. */
@Injectable({ providedIn: 'root' })
export class SignalementsApiService {
  private readonly http = inject(HttpClient);
  private readonly base = '/api/signalements';

  lister(filtre: SignalementFiltre = {}): Observable<Signalement[]> {
    let params = new HttpParams();
    if (filtre.statut) {
      params = params.set('statut', filtre.statut);
    }
    if (filtre.cibleType) {
      params = params.set('cibleType', filtre.cibleType);
    }
    return this.http.get<Signalement[]>(this.base, { params });
  }

  detail(id: number): Observable<Signalement> {
    return this.http.get<Signalement>(`${this.base}/${id}`);
  }

  creer(payload: SignalementRequest): Observable<Signalement> {
    return this.http.post<Signalement>(this.base, payload);
  }

  traiter(id: number, payload: TraitementSignalementRequest): Observable<Signalement> {
    return this.http.put<Signalement>(`${this.base}/${id}/traiter`, payload);
  }

  stats(): Observable<SignalementStats> {
    return this.http.get<SignalementStats>(`${this.base}/stats`);
  }
}
