import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { VALORIO_ENV } from '@valorio/shared';
import {
  AnomalyDetectionResult,
  EstimatePriceRequest,
  EstimatePriceResponse,
  GenerateListingRequest,
  GenerateListingResponse,
  MarketplaceItem,
  ModerationRequest,
  ModerationResponse,
} from './marketplace.models';

export interface AiHealth {
  status: string;
  engine: string;
  capabilities: string[];
  version: string;
  operationalMode: string;
}

@Injectable({ providedIn: 'root' })
export class AiAssistantApiService {
  private readonly http = inject(HttpClient);
  private readonly base = `${VALORIO_ENV.api.marketplace}/api/marketplace/ai`;

  generateListing(request: GenerateListingRequest): Observable<GenerateListingResponse> {
    return this.http.post<GenerateListingResponse>(`${this.base}/generate-listing`, request);
  }

  estimatePrice(request: EstimatePriceRequest): Observable<EstimatePriceResponse> {
    return this.http.post<EstimatePriceResponse>(`${this.base}/estimate-price`, request);
  }

  moderateContent(request: ModerationRequest): Observable<ModerationResponse> {
    return this.http.post<ModerationResponse>(`${this.base}/moderate`, request);
  }

  detectAnomaly(item: MarketplaceItem | Partial<MarketplaceItem>): Observable<AnomalyDetectionResult> {
    return this.http.post<AnomalyDetectionResult>(`${this.base}/detect-anomaly`, item);
  }

  health(): Observable<AiHealth> {
    return this.http.get<AiHealth>(`${this.base}/health`);
  }
}
