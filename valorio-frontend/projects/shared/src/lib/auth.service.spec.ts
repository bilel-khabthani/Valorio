import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { AuthService } from './auth.service';
import { AuthResponse, LoginRequest, RegisterRequest } from './auth-models';

describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;

  const mockUser = {
    id: 1,
    email: 'amine@valorio.tn',
    nom: 'Ben Salah',
    prenom: 'Amine',
    nomComplet: 'Amine Ben Salah',
    role: 'USER' as const,
    emailVerifie: true,
    actif: true,
  };

  const mockAuthResponse: AuthResponse = {
    accessToken: 'test-access-token-123',
    tokenType: 'Bearer',
    expiresIn: 900,
    user: mockUser,
  };

  /** Marqueur de session non HttpOnly posé par le back au login. */
  const MARQUEUR_SESSION = 'valorio_session';

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        AuthService,
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([]),
      ],
    });

    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
    document.cookie = `${MARQUEUR_SESSION}=; path=/; max-age=0`;
  });

  it('démarre non authentifié par défaut', () => {
    expect(service.isAuthenticated()).toBeFalse();
    expect(service.currentUser()).toBeNull();
    expect(service.accessToken()).toBeNull();
  });

  it('authentifie l utilisateur lors du login', () => {
    const creds: LoginRequest = { email: 'amine@valorio.tn', password: 'Password12345!' };

    service.login(creds).subscribe((res) => {
      expect(res.accessToken).toBe('test-access-token-123');
      expect(service.isAuthenticated()).toBeTrue();
      expect(service.userName()).toBe('Amine Ben Salah');
      expect(service.isAdmin()).toBeFalse();
    });

    const req = httpMock.expectOne('/api/auth/login');
    expect(req.request.method).toBe('POST');
    req.flush(mockAuthResponse);
  });

  it('enregistre un nouvel utilisateur lors du register', () => {
    const regData: RegisterRequest = {
      nom: 'Ben Salah',
      prenom: 'Amine',
      email: 'amine@valorio.tn',
      password: 'Password12345!',
      confirmPassword: 'Password12345!',
      termsAccepted: true,
    };

    service.register(regData).subscribe((res) => {
      expect(res.user.email).toBe('amine@valorio.tn');
      expect(service.isAuthenticated()).toBeTrue();
    });

    const req = httpMock.expectOne('/api/auth/register');
    expect(req.request.method).toBe('POST');
    req.flush(mockAuthResponse);
  });

  it('restaure la session au démarrage via initialiserSession (refresh réussi)', async () => {
    document.cookie = `${MARQUEUR_SESSION}=1; path=/`;
    const promesse = service.initialiserSession();

    const req = httpMock.expectOne('/api/auth/refresh');
    expect(req.request.method).toBe('POST');
    req.flush(mockAuthResponse);

    await promesse;

    expect(service.isAuthenticated()).toBeTrue();
    expect(service.userName()).toBe('Amine Ben Salah');
  });

  it('résout sans session si le refresh initial échoue (401)', async () => {
    document.cookie = `${MARQUEUR_SESSION}=1; path=/`;
    const promesse = service.initialiserSession();

    const req = httpMock.expectOne('/api/auth/refresh');
    req.flush('Non connecté', { status: 401, statusText: 'Unauthorized' });

    await promesse;

    expect(service.isAuthenticated()).toBeFalse();
    expect(service.currentUser()).toBeNull();
  });

  it('n émet aucune requête de refresh quand aucune session n est candidate', async () => {
    await service.initialiserSession();

    httpMock.expectNone('/api/auth/refresh');
    expect(service.isAuthenticated()).toBeFalse();
    expect(service.currentUser()).toBeNull();
  });

  it('efface la session lors de la déconnexion', () => {
    // Simule une session existante
    service['appliquerSession'](mockAuthResponse);
    expect(service.isAuthenticated()).toBeTrue();

    service.logout().subscribe(() => {
      expect(service.isAuthenticated()).toBeFalse();
      expect(service.currentUser()).toBeNull();
      expect(service.accessToken()).toBeNull();
    });

    const req = httpMock.expectOne('/api/auth/logout');
    expect(req.request.method).toBe('POST');
    req.flush({ message: 'Déconnecté', success: true });
  });
});
