import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { VALORIO_ENV } from '@valorio/shared';
import { DonApiService } from './don-api.service';

describe('DonApiService', () => {
  let service: DonApiService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(DonApiService);
    http = TestBed.inject(HttpTestingController);
  });

  it('should be created', () => {
    expect(TestBed.inject(DonApiService)).toBeTruthy();
  });

  it('annule une annonce via PUT /api/dons/{id}/annuler', () => {
    const base = VALORIO_ENV.api.don;
    service.annulerAnnonce(3, 5).subscribe();
    const req = http.expectOne(`${base}/api/dons/3/annuler?donneurId=5`);
    expect(req.request.method).toBe('PUT');
    req.flush({});
  });
});
