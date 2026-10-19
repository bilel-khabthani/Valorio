import { Injectable, computed, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, catchError, firstValueFrom, map, of, tap, throwError } from 'rxjs';
import {
  Acteur,
  AuthResponse,
  ForgotPasswordRequest,
  LoginRequest,
  MessageResponse,
  RegisterRequest,
  ResetPasswordRequest,
  User,
  VerifyEmailRequest,
  acteurDepuisRole,
} from './auth-models';
import { SessionService } from './session';

/** Marqueur de session non HttpOnly posé par le back (AuthService côté Java). */
const COOKIE_SESSION_MARQUEUR = 'valorio_session';

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
  readonly acteur = computed<Acteur | null>(() => {
    const u = this._currentUser();
    if (!u) {
      return null;
    }
    return u.acteur ?? acteurDepuisRole(u.role);
  });
  readonly userName = computed(() => {
    const u = this._currentUser();
    return u ? (u.nomComplet || `${u.prenom} ${u.nom}`).trim() : '';
  });

  private promesseInitialisation?: Promise<void>;

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
      this.sessionService.connecter({
        role: res.user.acteur ?? acteurDepuisRole(res.user.role),
        label: res.user.nomComplet || `${res.user.prenom} ${res.user.nom}`,
        utilisateurId: res.user.id,
      });
    }
  }

  private reinitialiserSession(): void {
    this._accessToken.set(null);
    this._currentUser.set(null);
    if (this.sessionService) {
      this.sessionService.deconnexion();
    }
  }

  /**
   * Restauration silencieuse de la session au démarrage (cookie refresh HTTP-Only).
   * Appelée par l'initialiseur d'application : le bootstrap attend sa résolution
   * afin que les guards voient la session restaurée avant toute navigation
   * (évite la redirection vers /login lors d'un rafraîchissement de page).
   *
   * La requête n'est émise que si le marqueur de session (cookie non HttpOnly posé
   * par le back lors du login/refresh) est présent : une visite anonyme ne déclenche
   * donc plus de POST /api/auth/refresh en 401 dans la console.
   */
  initialiserSession(): Promise<void> {
    if (!this.promesseInitialisation) {
      this.promesseInitialisation = this.aSessionExistante()
        ? firstValueFrom(this.refresh().pipe(catchError(() => of(null)))).then(() => undefined)
        : this.fermerSessionAnonyme();
    }
    return this.promesseInitialisation;
  }

  /** État anonyme garanti quand aucune session n'est candidate (pas de requête, pas de 401). */
  private fermerSessionAnonyme(): Promise<void> {
    this.reinitialiserSession();
    return Promise.resolve();
  }

  private aSessionExistante(): boolean {
    try {
      const cookies: string = globalThis.document?.cookie ?? '';
      return cookies
        .split(';')
        .some((c) => c.trim() === `${COOKIE_SESSION_MARQUEUR}=1`);
    } catch {
      // Environnement sans accès aux cookies (tests SSR) : on tente le refresh.
      return true;
    }
  }
}
