import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { VALORIO_ENV } from '@valorio/shared';
import { AdminApiService } from './admin-api.service';

describe('AdminApiService', () => {
  let service: AdminApiService;
  let http: HttpTestingController;
  const users = VALORIO_ENV.api.users;
  const objets = `${VALORIO_ENV.api.objets}/api`;
  const recyclage = `${VALORIO_ENV.api.recyclage}/api`;
  const reparation = `${VALORIO_ENV.api.reparation}/api`;
  const marketplace = `${VALORIO_ENV.api.marketplace}/api/marketplace`;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(AdminApiService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('liste les utilisateurs avec filtres via GET /api/users', () => {
    service.listerUtilisateurs({ acteur: 'REPARATEUR', actif: true }).subscribe();
    const req = http.expectOne(`${users}?acteur=REPARATEUR&actif=true`);
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });

  it('charge les statistiques utilisateurs via GET /api/users/stats', () => {
    service.statsUtilisateurs().subscribe();
    const req = http.expectOne(`${users}/stats`);
    expect(req.request.method).toBe('GET');
    req.flush({ total: 0, actifs: 0, inactifs: 0, verifies: 0, parActeur: {}, parRole: {} });
  });

  it('active/désactive un compte via PUT /api/users/{id}/actif', () => {
    service.changerActif(5, false).subscribe();
    const req = http.expectOne(`${users}/5/actif?actif=false`);
    expect(req.request.method).toBe('PUT');
    req.flush({});
  });

  it('modifie le rôle via PUT /api/users/{id}/acteur', () => {
    service.changerActeur(5, 'ORGANISME').subscribe();
    const req = http.expectOne(`${users}/5/acteur?acteur=ORGANISME`);
    expect(req.request.method).toBe('PUT');
    req.flush({});
  });

  it('supprime un compte via DELETE /api/users/{id}', () => {
    service.supprimerUtilisateur(5).subscribe();
    const req = http.expectOne(`${users}/5`);
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });

  it('bascule la disponibilité d\u2019un réparateur via PUT /reparateurs/{id}/disponibilite', () => {
    service.changerDisponibilite(2, true).subscribe();
    const req = http.expectOne(`${reparation}/reparateurs/2/disponibilite?disponible=true`);
    expect(req.request.method).toBe('PUT');
    req.flush({});
  });

  it('crée une catégorie via POST /api/categories', () => {
    service.creerCategorie({ nom: 'Mobilier' }).subscribe();
    const req = http.expectOne(`${objets}/categories`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body.nom).toBe('Mobilier');
    req.flush({});
  });

  it('supprime une catégorie via DELETE /api/categories/{id}', () => {
    service.supprimerCategorie(3).subscribe();
    const req = http.expectOne(`${objets}/categories/3`);
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });

  it('crée un centre via POST /api/centres', () => {
    service.creerCentre({ nom: 'Centre A', materiauxAcceptes: ['Verre'] }).subscribe();
    const req = http.expectOne(`${recyclage}/centres`);
    expect(req.request.method).toBe('POST');
    req.flush({});
  });

  it('supprime un centre via DELETE /api/centres/{id}', () => {
    service.supprimerCentre(3).subscribe();
    const req = http.expectOne(`${recyclage}/centres/3`);
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });

  it('agrège les stats objets via GET /api/stats', () => {
    service.statsObjets().subscribe();
    const req = http.expectOne(`${objets}/stats`);
    expect(req.request.method).toBe('GET');
    req.flush({});
  });

  it('modère une annonce via POST /api/marketplace/ai/moderate', () => {
    service.moderer({ title: 'Montre', description: 'desc' }).subscribe();
    const req = http.expectOne(`${marketplace}/ai/moderate`);
    expect(req.request.method).toBe('POST');
    req.flush({ approved: true });
  });

  it('change le statut d\u2019un article via PATCH /items/{id}/status', () => {
    service.changerStatutMarketplace(9, 'ARCHIVED').subscribe();
    const req = http.expectOne(`${marketplace}/items/9/status?status=ARCHIVED`);
    expect(req.request.method).toBe('PATCH');
    req.flush({});
  });
});
