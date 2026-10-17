import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Claim } from '../models/claim.model';

@Injectable({ providedIn: 'root' })
export class ClaimService {
  private readonly api = '/api/claims';

  constructor(private http: HttpClient) {}

  getAll(): Observable<Claim[]> {
    return this.http.get<Claim[]>(this.api);
  }

  getById(id: number): Observable<Claim> {
    return this.http.get<Claim>(`${this.api}/${id}`);
  }

  getByItemId(itemId: number): Observable<Claim[]> {
    return this.http.get<Claim[]>(`${this.api}/item/${itemId}`);
  }

  create(claim: Partial<Claim>): Observable<Claim> {
    return this.http.post<Claim>(this.api, claim);
  }

  approve(id: number): Observable<Claim> {
    return this.http.patch<Claim>(`${this.api}/${id}/approve`, {});
  }

  reject(id: number): Observable<Claim> {
    return this.http.patch<Claim>(`${this.api}/${id}/reject`, {});
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.api}/${id}`);
  }
}
