import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideRouter } from '@angular/router';
import { MarketplaceApiService, MarketplaceItem, MarketplaceStats } from '@valorio/api-marketplace';
import { MarketplaceCatalogue } from './marketplace-catalogue';
import { of } from 'rxjs';

describe('MarketplaceCatalogue', () => {
  const stats: MarketplaceStats = {
    totalItems: 2,
    availableItems: 1,
    soldItems: 1,
    pendingOffers: 3,
    averageSafetyScore: 92,
    aiAssistedPercentage: 50,
    itemsByCategory: { BICYCLES_VEHICLES: 2 },
    itemsByType: { SALE: 2 },
  };

  const annonce: MarketplaceItem = {
    id: 7,
    title: 'Vélo de ville',
    description: 'Vélo révisé, prêt à rouler',
    category: 'BICYCLES_VEHICLES',
    condition: 'GOOD',
    type: 'SALE',
    status: 'AVAILABLE',
    price: 120,
    unclaimedDays: 12,
    viewsCount: 5,
    createdAt: '2026-10-12T10:00:00Z',
  };

  let getItems: jasmine.Spy;
  let getStats: jasmine.Spy;
  let createOffer: jasmine.Spy;

  beforeEach(async () => {
    getItems = jasmine.createSpy('getItems').and.returnValue(of([annonce]));
    getStats = jasmine.createSpy('getStats').and.returnValue(of(stats));
    createOffer = jasmine
      .createSpy('createOffer')
      .and.returnValue(of({ id: 11, buyerName: 'Acheteur', offeredPrice: 120, status: 'PENDING' }));

    await TestBed.configureTestingModule({
      imports: [MarketplaceCatalogue],
      providers: [
        provideHttpClient(),
        provideRouter([]),
        { provide: MarketplaceApiService, useValue: { getItems, getStats, createOffer } },
      ],
    }).compileComponents();
  });

  it('crée le catalogue et charge articles et statistiques', () => {
    const fixture = TestBed.createComponent(MarketplaceCatalogue);
    fixture.detectChanges();

    expect(fixture.componentInstance).toBeTruthy();
    expect(getStats).toHaveBeenCalled();
    expect(getItems).toHaveBeenCalled();
    expect(fixture.componentInstance.articles().length).toBe(1);
    expect(fixture.componentInstance.stats()?.averageSafetyScore).toBe(92);
    expect(fixture.componentInstance.articles()[0].title).toBe('Vélo de ville');
  });

  it('applique puis réinitialise les filtres', () => {
    const fixture = TestBed.createComponent(MarketplaceCatalogue);
    fixture.detectChanges();

    fixture.componentInstance.choisirType('DONATION');
    expect(fixture.componentInstance.type()).toBe('DONATION');
    expect(getItems).toHaveBeenCalledWith(
      jasmine.objectContaining({ type: 'DONATION', category: undefined, search: undefined }),
    );

    fixture.componentInstance.choisirCategorie('ELECTRONICS');
    expect(fixture.componentInstance.categorie()).toBe('ELECTRONICS');

    fixture.componentInstance.recherche = '  vélo  ';
    expect(fixture.componentInstance.filtresActifs()).toBeTrue();

    fixture.componentInstance.reinitialiser();
    expect(fixture.componentInstance.type()).toBeUndefined();
    expect(fixture.componentInstance.categorie()).toBeUndefined();
    expect(fixture.componentInstance.recherche).toBe('');
    expect(fixture.componentInstance.filtresActifs()).toBeFalse();
    expect(getItems).toHaveBeenCalledWith(
      jasmine.objectContaining({ type: undefined, category: undefined, search: undefined }),
    );
  });

  it('pré-remplit et transmet une offre pour un article payant', () => {
    const fixture = TestBed.createComponent(MarketplaceCatalogue);
    fixture.detectChanges();

    fixture.componentInstance.ouvrirOffre(annonce);
    expect(fixture.componentInstance.articleOffre()?.id).toBe(7);
    expect(fixture.componentInstance.offre.offeredPrice).toBe(120);
    expect(fixture.componentInstance.offre.message).toContain('Vélo de ville');

    fixture.componentInstance.envoyerOffre();

    expect(createOffer).toHaveBeenCalledWith(7, fixture.componentInstance.offre);
    expect(fixture.componentInstance.articleOffre()).toBeNull();
    expect(fixture.componentInstance.envoiOffre()).toBeFalse();
    expect(fixture.componentInstance.succes()).toBe('Votre proposition a été transmise.');
  });

  it('propose un prix nul et un message solidaire pour un don', () => {
    const fixture = TestBed.createComponent(MarketplaceCatalogue);
    fixture.detectChanges();

    fixture.componentInstance.ouvrirOffre({ ...annonce, id: 8, type: 'DONATION', price: 0 });

    expect(fixture.componentInstance.offre.offeredPrice).toBe(0);
    expect(fixture.componentInstance.offre.message).toContain('attribution solidaire');
    expect(fixture.componentInstance.libelleEquite('SOLIDARITY_FREE')).toBe('Solidaire');
    expect(fixture.componentInstance.libelleEquite('EXCELLENT_DEAL')).toBe('Affaire certifiée');
    expect(fixture.componentInstance.libelleEquite(undefined)).toBe('Prix équitable IA');
  });

  it('ferme la modale sans transmettre', () => {
    const fixture = TestBed.createComponent(MarketplaceCatalogue);
    fixture.detectChanges();

    fixture.componentInstance.ouvrirOffre(annonce);
    fixture.componentInstance.fermerOffre();

    expect(fixture.componentInstance.articleOffre()).toBeNull();
    expect(createOffer).not.toHaveBeenCalled();
  });
});
