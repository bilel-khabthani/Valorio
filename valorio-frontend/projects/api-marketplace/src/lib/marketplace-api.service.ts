import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { VALORIO_ENV } from '@valorio/shared';
import {
  Category,
  ItemStatus,
  ItemType,
  MarketplaceItem,
  MarketplaceOffer,
  MarketplaceStats,
  OfferStatus,
} from './marketplace.models';

export interface MarketplaceFiltre {
  category?: Category;
  type?: ItemType;
  status?: ItemStatus;
  minPrice?: number;
  maxPrice?: number;
  search?: string;
}

@Injectable({ providedIn: 'root' })
export class MarketplaceApiService {
  private readonly http = inject(HttpClient);
  private readonly base = `${VALORIO_ENV.api.marketplace}/api/marketplace`;

  // --- Articles ---
  getItems(filtre: MarketplaceFiltre = {}): Observable<MarketplaceItem[]> {
    let params = new HttpParams();
    if (filtre.category) {
      params = params.set('category', filtre.category);
    }
    if (filtre.type) {
      params = params.set('type', filtre.type);
    }
    if (filtre.status) {
      params = params.set('status', filtre.status);
    }
    if (filtre.minPrice != null) {
      params = params.set('minPrice', filtre.minPrice);
    }
    if (filtre.maxPrice != null) {
      params = params.set('maxPrice', filtre.maxPrice);
    }
    if (filtre.search) {
      params = params.set('search', filtre.search);
    }
    return this.http.get<MarketplaceItem[]>(`${this.base}/items`, { params });
  }

  getItemById(id: number): Observable<MarketplaceItem> {
    return this.http.get<MarketplaceItem>(`${this.base}/items/${id}`);
  }

  createItem(item: MarketplaceItem): Observable<MarketplaceItem> {
    return this.http.post<MarketplaceItem>(`${this.base}/items`, item);
  }

  updateItem(id: number, item: MarketplaceItem): Observable<MarketplaceItem> {
    return this.http.put<MarketplaceItem>(`${this.base}/items/${id}`, item);
  }

  updateItemStatus(id: number, status: ItemStatus): Observable<MarketplaceItem> {
    return this.http.patch<MarketplaceItem>(`${this.base}/items/${id}/status`, null, {
      params: { status },
    });
  }

  deleteItem(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/items/${id}`);
  }

  getStats(): Observable<MarketplaceStats> {
    return this.http.get<MarketplaceStats>(`${this.base}/items/stats`);
  }

  // --- Offres ---
  getAllOffers(): Observable<MarketplaceOffer[]> {
    return this.http.get<MarketplaceOffer[]>(`${this.base}/offers`);
  }

  getOffersByItem(itemId: number): Observable<MarketplaceOffer[]> {
    return this.http.get<MarketplaceOffer[]>(`${this.base}/offers/item/${itemId}`);
  }

  createOffer(itemId: number, offer: MarketplaceOffer): Observable<MarketplaceOffer> {
    return this.http.post<MarketplaceOffer>(`${this.base}/offers/item/${itemId}`, offer);
  }

  updateOfferStatus(offerId: number, status: OfferStatus): Observable<MarketplaceOffer> {
    return this.http.patch<MarketplaceOffer>(`${this.base}/offers/${offerId}/status`, null, {
      params: { status },
    });
  }

  deleteOffer(offerId: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/offers/${offerId}`);
  }
}
