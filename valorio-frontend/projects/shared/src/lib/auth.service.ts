import { Injectable, computed, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, catchError, map, of, tap, throwError } from 'rxjs';
import {
  AuthResponse,
  ForgotPasswordRequest,
  LoginRequest,
  MessageResponse,
  RegisterRequest,
  ResetPasswordRequest,
  User,
  VerifyEmailRequest,
} from './auth-models';
import { SessionService } from './session';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);
  private readonly sessionService = inject(SessionService, { optional: true });

  private readonly _accessToken = signal<string | null>(null);
  private readonly _currentUser = signal<User | null>(null);

  readonly accessToken = this._accessToken.asReadonly();
  readonly currentUser = this._currentUser.asReadonly();
  readonly isAuthenticated = computed(() => !!this._accessToken());
  readonly isAdmin = computed(() => this._currentUser()?.role === 'ADMIN');
  readonly userName = computed(() => {
    const u = this._currentUser();
    return u ? (u.nomComplet || `${u.prenom} ${u.nom}`).trim() : '';
  });

  constructor() {
    this.restaurerSessionMemoire();
  }

  register(data: RegisterRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>('/api/auth/register', data, { withCredentials: true }).pipe(
      tap((res) => this.appliquerSession(res))
    );
  }

  login(credentials: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>('/api/auth/login', credentials, { withCredentials: true }).pipe(
      tap((res) => this.appliquerSession(res))
    );
  }

  refresh(): Observable<AuthResponse> {
    return this.http.post<AuthResponse>('/api/auth/refresh', {}, { withCredentials: true }).pipe(
      tap((res) => this.appliquerSession(res)),
      catchError((err) => {
        this.reinitialiserSession();
        return throwError(() => err);
      })
    );
  }

  logout(): Observable<void> {
    return this.http.post<MessageResponse>('/api/auth/logout', {}, { withCredentials: true }).pipe(
      map(() => void 0),
      catchError(() => of(void 0)),
      tap(() => {
        this.reinitialiserSession();
        this.router.navigate(['/login']);
      })
    );
  }

  me(): Observable<User> {
    return this.http.get<User>('/api/auth/me', { withCredentials: true }).pipe(
      tap((u) => {
        this._currentUser.set(u);
      })
    );
  }

  forgotPassword(data: ForgotPasswordRequest): Observable<MessageResponse> {
    return this.http.post<MessageResponse>('/api/auth/forgot-password', data);
  }

  resetPassword(data: ResetPasswordRequest): Observable<MessageResponse> {
    return this.http.post<MessageResponse>('/api/auth/reset-password', data);
  }

  verifyEmail(token: string): Observable<MessageResponse> {
    const payload: VerifyEmailRequest = { token };
    return this.http.post<MessageResponse>('/api/auth/verify-email', payload);
  }

  resendVerification(email: string): Observable<MessageResponse> {
    return this.http.post<MessageResponse>('/api/auth/resend-verification', { email });
  }

  private appliquerSession(res: AuthResponse): void {
    this._accessToken.set(res.accessToken);
    this._currentUser.set(res.user);

    // Synchronisation transparente avec SessionService pour la compatibilité avec les modules métier
    if (this.sessionService) {
      if (res.user.role === 'ADMIN') {
        this.sessionService.connecter({
          role: 'ADMIN',
          label: res.user.nomComplet || `${res.user.prenom} ${res.user.nom}`,
          utilisateurId: res.user.id,
        });
      } else {
        this.sessionService.connecter({
          role: 'UTILISATEUR',
          label: res.user.nomComplet || `${res.user.prenom} ${res.user.nom}`,
          utilisateurId: res.user.id,
        });
      }
    }
  }

  private reinitialiserSession(): void {
    this._accessToken.set(null);
    this._currentUser.set(null);
  }

  private restaurerSessionMemoire(): void {
    // Tente un rafraîchissement silencieux de démarrage au chargement de l'application via cookie HTTP-Only
    this.refresh().subscribe({
      next: () => {
        /* Session active restaurée avec succès */
      },
      error: () => {
        /* Utilisateur non authentifié, reste invité */
      },
    });
  }
}
