import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { 
  AnomalyDetectionResult, 
  EstimatePriceRequest, 
  EstimatePriceResponse, 
  GenerateListingRequest, 
  GenerateListingResponse, 
  MarketplaceItem, 
  ModerationRequest, 
  ModerationResponse 
} from '../models/marketplace.model';

@Injectable({
  providedIn: 'root'
})
export class AiAssistantService {
  private readonly baseUrl = 'http://localhost:8085/api/marketplace/ai';

  constructor(private http: HttpClient) {}

  generateListing(request: GenerateListingRequest): Observable<GenerateListingResponse> {
    return this.http.post<GenerateListingResponse>(`${this.baseUrl}/generate-listing`, request);
  }

  estimatePrice(request: EstimatePriceRequest): Observable<EstimatePriceResponse> {
    return this.http.post<EstimatePriceResponse>(`${this.baseUrl}/estimate-price`, request);
  }

  moderateContent(request: ModerationRequest): Observable<ModerationResponse> {
    return this.http.post<ModerationResponse>(`${this.baseUrl}/moderate`, request);
  }

  detectAnomaly(item: MarketplaceItem): Observable<AnomalyDetectionResult> {
    return this.http.post<AnomalyDetectionResult>(`${this.baseUrl}/detect-anomaly`, item);
  }

  getAiHealth(): Observable<any> {
    return this.http.get<any>(`${this.baseUrl}/health`);
  }
}
