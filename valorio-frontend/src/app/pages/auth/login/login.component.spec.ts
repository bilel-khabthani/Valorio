import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { LoginComponent } from './login.component';
import { AuthService, AuthResponse } from '@valorio/shared';

describe('LoginComponent', () => {
  let component: LoginComponent;
  let fixture: ComponentFixture<LoginComponent>;
  let authService: AuthService;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [LoginComponent],
      providers: [
        AuthService,
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([]),
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(LoginComponent);
    component = fixture.componentInstance;
    authService = TestBed.inject(AuthService);
    fixture.detectChanges();
  });

  it('doit être créé avec les champs vides', () => {
    expect(component).toBeTruthy();
    expect(component.email).toBe('');
    expect(component.password).toBe('');
    expect(component.errorMessage()).toBeNull();
  });

  it('affiche une erreur si le formulaire est soumis vide', () => {
    component.onSubmit();
    expect(component.errorMessage()).toContain('renseigner votre email');
  });

  it('bascule la visibilité du mot de passe', () => {
    expect(component.showPassword()).toBeFalse();
    component.toggleShowPassword();
    expect(component.showPassword()).toBeTrue();
  });

  it('appelle authService.login lors d une saisie valide', () => {
    component.email = 'amine@valorio.tn';
    component.password = 'Secret123456!';

    const mockResponse: AuthResponse = {
      accessToken: 'jwt-token',
      tokenType: 'Bearer',
      expiresIn: 900,
      user: {
        id: 1,
        email: 'amine@valorio.tn',
        nom: 'Ben Salah',
        prenom: 'Amine',
        nomComplet: 'Amine Ben Salah',
        role: 'USER',
        emailVerifie: true,
        actif: true,
      },
    };

    spyOn(authService, 'login').and.returnValue(of(mockResponse));
    component.onSubmit();

    expect(authService.login).toHaveBeenCalledWith({
      email: 'amine@valorio.tn',
      password: 'Secret123456!',
      rememberMe: false,
    });
  });
});
