import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideRouter } from '@angular/router';
import { FeatureRecommandationComponent } from './feature-recommandation';
import { RecommandationApiService } from '@valorio/api-recommandation';
import { of } from 'rxjs';

describe('FeatureRecommandationComponent', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FeatureRecommandationComponent],
      providers: [
        provideHttpClient(),
        provideRouter([]),
        { provide: RecommandationApiService, useValue: { recommanderValorisation: () => of({ id: 1, routeRecommandee: 'DON', probabilite: 0.85, alternatives: [], raisons: [], source: 'ML' as const, modeleVersion: 'v1' }), metadataModele: () => of({ actif: true, source: 'ML' as const, versionValorisation: 'v1', versionActeurs: 'v1', versionMarche: 'v1', metriques: { accuracy: 0.9 }, topFeatures: ['coutReparationEstime'] }), recommanderActeurs: () => of({ typeActeur: 'REPARATEUR', classement: [], source: 'ML' as const }), articlesSimilaires: () => of({ articles: [], source: 'ML' as const }), envoyerFeedback: () => of({ id: 1, recommandationId: 1, utile: true, enregistreLe: '2026-10-18T00:00:00Z' }), historique: () => of([]), expliquer: () => of({ recommandationId: 1, explication: 'exp', source: 'ML' as const }) } },
      ],
    }).compileComponents();
  });

  it('should create', () => {
    const fixture = TestBed.createComponent(FeatureRecommandationComponent);
    expect(fixture.componentInstance).toBeTruthy();
  });

  it('switches tabs and submits valorisation', () => {
    const fixture = TestBed.createComponent(FeatureRecommandationComponent);
    fixture.detectChanges();
    fixture.componentInstance.tab.set('valorisation');
    fixture.componentInstance.objetId.set(1);
    fixture.componentInstance.coutReparationEstime.set(100);
    fixture.componentInstance.submettreValorisation();
    fixture.detectChanges();
    expect(fixture.componentInstance.resultatValorisation()).toBeTruthy();
  });

  it('posts feedback when clicked', () => {
    const fixture = TestBed.createComponent(FeatureRecommandationComponent);
    fixture.componentInstance.resultatValorisation.set({ id: 1, routeRecommandee: 'DON', probabilite: 0.85, alternatives: [], raisons: [], source: 'ML', modeleVersion: 'v1' });
    fixture.componentInstance.feedbackDonne.set(false);
    fixture.componentInstance.donnerFeedback(true);
    expect(fixture.componentInstance.feedbackDonne()).toBeTrue();
    expect(fixture.componentInstance.succes()).toBe('Feedback enregistré');
  });
});
