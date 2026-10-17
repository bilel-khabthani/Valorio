import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { FeatureRecyclageHome } from './feature-recyclage';

describe('FeatureRecyclageHome', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FeatureRecyclageHome],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])],
    }).compileComponents();
  });

  it('should create', () => {
    const fixture = TestBed.createComponent(FeatureRecyclageHome);
    expect(fixture.componentInstance).toBeTruthy();
  });
});
