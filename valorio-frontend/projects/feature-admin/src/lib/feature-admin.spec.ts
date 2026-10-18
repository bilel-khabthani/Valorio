import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { AdminDashboard } from './admin-dashboard';
import { routes } from './routes';

describe('FeatureAdmin', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AdminDashboard],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])],
    }).compileComponents();
  });

  it('crée le tableau de bord', () => {
    const fixture = TestBed.createComponent(AdminDashboard);
    expect(fixture.componentInstance).toBeTruthy();
  });

  it('expose les routes de l\u2019espace admin', () => {
    expect(routes.length).toBe(1);
    expect(routes[0].path).toBe('');
    expect(routes[0].children?.length).toBe(9);
  });
});
