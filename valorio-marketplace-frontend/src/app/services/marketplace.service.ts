import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Category, ItemStatus, ItemType, MarketplaceItem, MarketplaceOffer, MarketplaceStats } from '../models/marketplace.model';

@Injectable({
  providedIn: 'root'
})
export class MarketplaceService {
  private readonly baseUrl = 'http://localhost:8085/api/marketplace';

  constructor(private http: HttpClient) {}

  getItems(filters?: {
    category?: Category;
    type?: ItemType;
    status?: ItemStatus;
    minPrice?: number;
    maxPrice?: number;
    search?: string;
  }): Observable<MarketplaceItem[]> {
    let params = new HttpParams();
    if (filters) {
      if (filters.category) params = params.set('category', filters.category);
      if (filters.type) params = params.set('type', filters.type);
      if (filters.status) params = params.set('status', filters.status);
      if (filters.minPrice != null) params = params.set('minPrice', filters.minPrice.toString());
      if (filters.maxPrice != null) params = params.set('maxPrice', filters.maxPrice.toString());
      if (filters.search) params = params.set('search', filters.search);
    }
    return this.http.get<MarketplaceItem[]>(`${this.baseUrl}/items`, { params });
  }

  getItemById(id: number): Observable<MarketplaceItem> {
    return this.http.get<MarketplaceItem>(`${this.baseUrl}/items/${id}`);
  }

  createItem(item: MarketplaceItem): Observable<MarketplaceItem> {
    return this.http.post<MarketplaceItem>(`${this.baseUrl}/items`, item);
  }

  updateItem(id: number, item: MarketplaceItem): Observable<MarketplaceItem> {
    return this.http.put<MarketplaceItem>(`${this.baseUrl}/items/${id}`, item);
  }

  updateItemStatus(id: number, status: ItemStatus): Observable<MarketplaceItem> {
    return this.http.patch<MarketplaceItem>(`${this.baseUrl}/items/${id}/status`, null, {
      params: { status }
    });
  }

  deleteItem(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/items/${id}`);
  }

  getStats(): Observable<MarketplaceStats> {
    return this.http.get<MarketplaceStats>(`${this.baseUrl}/items/stats`);
  }

  // Offers
  getAllOffers(): Observable<MarketplaceOffer[]> {
    return this.http.get<MarketplaceOffer[]>(`${this.baseUrl}/offers`);
  }

  getOffersByItem(itemId: number): Observable<MarketplaceOffer[]> {
    return this.http.get<MarketplaceOffer[]>(`${this.baseUrl}/offers/item/${itemId}`);
  }

  createOffer(itemId: number, offer: MarketplaceOffer): Observable<MarketplaceOffer> {
    return this.http.post<MarketplaceOffer>(`${this.baseUrl}/offers/item/${itemId}`, offer);
  }

  updateOfferStatus(offerId: number, status: string): Observable<MarketplaceOffer> {
    return this.http.patch<MarketplaceOffer>(`${this.baseUrl}/offers/${offerId}/status`, null, {
      params: { status }
    });
  }

  deleteOffer(offerId: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/offers/${offerId}`);
  }
}
