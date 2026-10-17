import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { RegisterComponent } from './register.component';
import { AuthService } from '@valorio/shared';

describe('RegisterComponent', () => {
  let component: RegisterComponent;
  let fixture: ComponentFixture<RegisterComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RegisterComponent],
      providers: [
        AuthService,
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([]),
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(RegisterComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('doit être initialisé correctement', () => {
    expect(component).toBeTruthy();
    expect(component.strengthScore()).toBe(0);
    expect(component.passwordLengthOk()).toBeFalse();
  });

  it('calcule la force du mot de passe au fil de la frappe', () => {
    component.onPasswordChange('court');
    expect(component.passwordLengthOk()).toBeFalse();
    expect(component.strengthScore()).toBe(15); // seulement minuscules

    component.onPasswordChange('MotDePasseLongEtRobuste123!');
    expect(component.passwordLengthOk()).toBeTrue();
    expect(component.hasUppercase()).toBeTrue();
    expect(component.hasLowercase()).toBeTrue();
    expect(component.hasNumber()).toBeTrue();
    expect(component.hasSpecial()).toBeTrue();
    expect(component.strengthScore()).toBe(100);
    expect(component.strengthLabel().label).toContain('Sécurisé');
  });

  it('détecte si les mots de passe correspondent', () => {
    component.onPasswordChange('Secret123456!');
    component.onConfirmPasswordChange('Secret123456!');
    expect(component.passwordsMatch()).toBeTrue();

    component.onConfirmPasswordChange('DifferentPassword!');
    expect(component.passwordsMatch()).toBeFalse();
  });

  it('rejette la soumission si les conditions ne sont pas acceptées', () => {
    component.nom = 'Ben Salah';
    component.prenom = 'Amine';
    component.email = 'amine@valorio.tn';
    component.onPasswordChange('Secret123456!');
    component.onConfirmPasswordChange('Secret123456!');
    component.termsAccepted = false;

    component.onSubmit();
    expect(component.errorMessage()).toContain('conditions générales');
  });
});
