import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Item, ItemSearchParams } from '../models/item.model';

@Injectable({ providedIn: 'root' })
export class ItemService {
  private readonly api = '/api/items';

  constructor(private http: HttpClient) {}

  getAll(): Observable<Item[]> {
    return this.http.get<Item[]>(this.api);
  }

  getById(id: number): Observable<Item> {
    return this.http.get<Item>(`${this.api}/${id}`);
  }

  search(params: ItemSearchParams): Observable<Item[]> {
    let httpParams = new HttpParams();
    if (params.keyword) httpParams = httpParams.set('keyword', params.keyword);
    if (params.category) httpParams = httpParams.set('category', params.category);
    if (params.location) httpParams = httpParams.set('location', params.location);
    if (params.type) httpParams = httpParams.set('type', params.type);
    if (params.status) httpParams = httpParams.set('status', params.status);
    return this.http.get<Item[]>(`${this.api}/search`, { params: httpParams });
  }

  create(item: Partial<Item>): Observable<Item> {
    return this.http.post<Item>(this.api, item);
  }

  createWithPhoto(item: Partial<Item>, photo: File): Observable<Item> {
    const formData = new FormData();
    formData.append('item', new Blob([JSON.stringify(item)], { type: 'application/json' }));
    formData.append('photo', photo);
    return this.http.post<Item>(`${this.api}/with-photo`, formData);
  }

  update(id: number, item: Partial<Item>): Observable<Item> {
    return this.http.put<Item>(`${this.api}/${id}`, item);
  }

  updateWithPhoto(id: number, item: Partial<Item>, photo: File): Observable<Item> {
    const formData = new FormData();
    formData.append('item', new Blob([JSON.stringify(item)], { type: 'application/json' }));
    formData.append('photo', photo);
    return this.http.put<Item>(`${this.api}/${id}/with-photo`, formData);
  }

  updateStatus(id: number, status: string): Observable<Item> {
    return this.http.patch<Item>(`${this.api}/${id}/status?status=${status}`, {});
  }

  confirmReturn(id: number): Observable<Item> {
    return this.http.patch<Item>(`${this.api}/${id}/return`, {});
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.api}/${id}`);
  }

  getCategories(): Observable<string[]> {
    return this.http.get<string[]>(`${this.api}/categories`);
  }

  getLocations(): Observable<string[]> {
    return this.http.get<string[]>(`${this.api}/locations`);
  }
}
