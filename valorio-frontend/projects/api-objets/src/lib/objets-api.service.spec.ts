import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { VALORIO_ENV } from '@valorio/shared';
import { ObjetsApiService } from './objets-api.service';

describe('ObjetsApiService', () => {
  let service: ObjetsApiService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(ObjetsApiService);
    http = TestBed.inject(HttpTestingController);
  });

  it('should be created', () => {
    expect(TestBed.inject(ObjetsApiService)).toBeTruthy();
  });

  it('liste les objets via GET /api/objets', () => {
    const base = VALORIO_ENV.api.objets;
    service.listerObjets({ categorieId: 2, statut: 'DISPONIBLE' }).subscribe();
    const req = http.expectOne(`${base}/api/objets?categorieId=2&statut=DISPONIBLE`);
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });

  it('enregistre un événement historique via POST /api/objets/{id}/historique', () => {
    const base = VALORIO_ENV.api.objets;
    service
      .enregistrerEvenement(3, { type: 'DON', libelle: 'Don remis', acteurId: 2, statutCible: 'DONNE' })
      .subscribe();
    const req = http.expectOne(`${base}/api/objets/3/historique`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({
      type: 'DON',
      libelle: 'Don remis',
      acteurId: 2,
      statutCible: 'DONNE',
    });
    req.flush({});
  });
});
