import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { RecommandationApiService } from './recommandation-api.service';
import { ValorisationRequest } from './reco-models';

describe('RecommandationApiService', () => {
  let service: RecommandationApiService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(RecommandationApiService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    http.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('recommande valorisation', () => {
    const body: ValorisationRequest = { objetId: 1 };
    service.recommanderValorisation(body).subscribe((res) => {
      expect(res.routeRecommandee).toBe('REPARATION');
    });
    const req = http.expectOne('/reco/api/recommandations/valorisation');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(body);
    req.flush({ id: 1, routeRecommandee: 'REPARATION', probabilite: 0.8, source: 'ML' });
  });

  it('recommande acteurs', () => {
    service.recommanderActeurs({ typeActeur: 'REPARATEUR' }).subscribe();
    const req = http.expectOne('/reco/api/recommandations/acteurs');
    expect(req.request.method).toBe('POST');
    req.flush({ typeActeur: 'REPARATEUR', source: 'ML' });
  });

  it('articles similaires', () => {
    service.articlesSimilaires({ requete: { id: 1, titre: 'test' } }).subscribe();
    const req = http.expectOne('/reco/api/recommandations/marche/similaires');
    expect(req.request.method).toBe('POST');
    req.flush({ articles: [], source: 'ML' });
  });

  it('feedback', () => {
    service.envoyerFeedback({ recommandationId: 1, utile: true }).subscribe();
    const req = http.expectOne('/reco/api/recommandations/feedback');
    expect(req.request.method).toBe('POST');
    req.flush({ id: 1, recommandationId: 1, utile: true, enregistreLe: '2026-10-18T00:00:00Z' });
  });

  it('metadata', () => {
    service.metadataModele().subscribe();
    const req = http.expectOne('/reco/api/recommandations/modele/metadata');
    expect(req.request.method).toBe('GET');
    req.flush({ actif: true, source: 'ML' });
  });

  it('historique', () => {
    service.historique(1).subscribe();
    const req = http.expectOne('/reco/api/recommandations/historique?objetId=1');
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });

  it('historique sans filtre', () => {
    service.historique().subscribe();
    const req = http.expectOne('/reco/api/recommandations/historique');
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });

  it('expliquer', () => {
    service.expliquer(5).subscribe();
    const req = http.expectOne('/reco/api/recommandations/5/explication');
    expect(req.request.method).toBe('POST');
    req.flush({ recommandationId: 5, explication: 'exp', source: 'ML' });
  });
});
