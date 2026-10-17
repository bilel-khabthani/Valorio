import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { FeatureReparationHome } from './feature-reparation';

describe('FeatureReparationHome', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FeatureReparationHome],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])],
    }).compileComponents();
  });

  it('should create', () => {
    const fixture = TestBed.createComponent(FeatureReparationHome);
    expect(fixture.componentInstance).toBeTruthy();
  });
});
