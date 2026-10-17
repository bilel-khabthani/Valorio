import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { VALORIO_ENV } from '@valorio/shared';
import { ReparationApiService } from './reparation-api.service';

describe('ReparationApiService', () => {
  let service: ReparationApiService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(ReparationApiService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('planifie une intervention via PUT /api/demandes/{id}/intervention', () => {
    const base = VALORIO_ENV.api.reparation;
    service.planifierIntervention(7, '2026-06-01T10:00:00.000Z').subscribe();
    const req = http.expectOne(`${base}/api/demandes/7/intervention`);
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual({ dateIntervention: '2026-06-01T10:00:00.000Z' });
    req.flush({});
  });
});
