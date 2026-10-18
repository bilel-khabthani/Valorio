import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { FeatureObjetsHome } from './feature-objets';

describe('FeatureObjetsHome', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FeatureObjetsHome],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])],
    }).compileComponents();
  });

  it('should create', () => {
    const fixture = TestBed.createComponent(FeatureObjetsHome);
    expect(fixture.componentInstance).toBeTruthy();
  });
});
