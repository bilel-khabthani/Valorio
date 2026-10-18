import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { VALORIO_ENV } from '@valorio/shared';
import { AiAssistantApiService } from './ai-assistant-api.service';
import { MarketplaceApiService } from './marketplace-api.service';

describe('MarketplaceApiService', () => {
  let service: MarketplaceApiService;
  let ai: AiAssistantApiService;
  let http: HttpTestingController;
  const base = `${VALORIO_ENV.api.marketplace}/api/marketplace`;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(MarketplaceApiService);
    ai = TestBed.inject(AiAssistantApiService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('should be created', () => {
    expect(service).toBeTruthy();
    expect(ai).toBeTruthy();
  });

  it('liste les articles avec filtres via GET /items', () => {
    service.getItems({ category: 'ELECTRONICS', type: 'SALE', search: 'iphone' }).subscribe();
    const req = http.expectOne(`${base}/items?category=ELECTRONICS&type=SALE&search=iphone`);
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });

  it('crée un article via POST /items', () => {
    service
      .createItem({
        title: 'iPhone 13',
        description: 'desc',
        category: 'ELECTRONICS',
        condition: 'GOOD',
        type: 'SALE',
        status: 'AVAILABLE',
        price: 290,
      })
      .subscribe();
    const req = http.expectOne(`${base}/items`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body.title).toBe('iPhone 13');
    req.flush({});
  });

  it('crée une offre via POST /offers/item/{id}', () => {
    service
      .createOffer(3, {
        buyerName: 'Lucas',
        buyerEmail: 'lucas@mail.fr',
        offeredPrice: 270,
        message: 'ok',
      })
      .subscribe();
    const req = http.expectOne(`${base}/offers/item/3`);
    expect(req.request.method).toBe('POST');
    req.flush({});
  });

  it('met à jour le statut d\'une offre via PATCH /offers/{id}/status', () => {
    service.updateOfferStatus(5, 'ACCEPTED').subscribe();
    const req = http.expectOne(`${base}/offers/5/status?status=ACCEPTED`);
    expect(req.request.method).toBe('PATCH');
    req.flush({});
  });

  it('demande une génération d\'annonce via POST /ai/generate-listing', () => {
    ai.generateListing({ prompt: 'iPhone 13' }).subscribe();
    const req = http.expectOne(`${base}/ai/generate-listing`);
    expect(req.request.method).toBe('POST');
    req.flush({});
  });
});
