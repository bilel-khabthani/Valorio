import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { SignalementsApiService } from './signalements-api.service';

describe('SignalementsApiService', () => {
  let service: SignalementsApiService;
  let http: HttpTestingController;
  const base = '/api/signalements';

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(SignalementsApiService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('liste les signalements avec filtres via GET /api/signalements', () => {
    service.lister({ statut: 'OUVERT', cibleType: 'OBJET' }).subscribe();
    const req = http.expectOne(`${base}?statut=OUVERT&cibleType=OBJET`);
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });

  it('crée un signalement via POST /api/signalements', () => {
    service.creer({ cibleType: 'OBJET', cibleId: 4, motif: 'Objet interdit' }).subscribe();
    const req = http.expectOne(base);
    expect(req.request.method).toBe('POST');
    expect(req.request.body.motif).toBe('Objet interdit');
    req.flush({});
  });

  it('traite un signalement via PUT /api/signalements/{id}/traiter', () => {
    service.traiter(7, { statut: 'TRAITE', note: 'ok' }).subscribe();
    const req = http.expectOne(`${base}/7/traiter`);
    expect(req.request.method).toBe('PUT');
    expect(req.request.body.statut).toBe('TRAITE');
    req.flush({});
  });

  it('charge les statistiques via GET /api/signalements/stats', () => {
    service.stats().subscribe();
    const req = http.expectOne(`${base}/stats`);
    expect(req.request.method).toBe('GET');
    req.flush({});
  });
});
