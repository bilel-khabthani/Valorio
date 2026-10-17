import { Component, computed, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService, RegisterRequest, messageErreur } from '@valorio/shared';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './register.component.html',
  styleUrl: './register.component.css',
})
export class RegisterComponent {
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  nom = '';
  prenom = '';
  email = '';
  password = signal('');
  confirmPassword = signal('');
  termsAccepted = false;

  showPassword = signal(false);
  showConfirmPassword = signal(false);
  loading = signal(false);
  errorMessage = signal<string | null>(null);
  successMessage = signal<string | null>(null);

  // Indicateur dynamique de force du mot de passe
  readonly passwordLengthOk = computed(() => this.password().length >= 12);
  readonly hasUppercase = computed(() => /[A-Z]/.test(this.password()));
  readonly hasLowercase = computed(() => /[a-z]/.test(this.password()));
  readonly hasNumber = computed(() => /[0-9]/.test(this.password()));
  readonly hasSpecial = computed(() => /[^A-Za-z0-9]/.test(this.password()));

  readonly strengthScore = computed(() => {
    let score = 0;
    if (this.passwordLengthOk()) score += 25;
    if (this.hasUppercase()) score += 20;
    if (this.hasLowercase()) score += 15;
    if (this.hasNumber()) score += 20;
    if (this.hasSpecial()) score += 20;
    return score;
  });

  readonly strengthLabel = computed(() => {
    const s = this.strengthScore();
    if (s < 40) return { label: 'Très faible', color: '#dc2626' };
    if (s < 60) return { label: 'Faible', color: '#ea580c' };
    if (s < 80) return { label: 'Moyen', color: '#d97706' };
    if (s < 100) return { label: 'Bon', color: '#2563eb' };
    return { label: 'Excellent & Sécurisé', color: '#16a34a' };
  });

  readonly passwordsMatch = computed(() => {
    return !!this.password() && !!this.confirmPassword() && this.password() === this.confirmPassword();
  });

  toggleShowPassword(): void {
    this.showPassword.update((v) => !v);
  }

  toggleShowConfirmPassword(): void {
    this.showConfirmPassword.update((v) => !v);
  }

  onPasswordChange(val: string): void {
    this.password.set(val);
  }

  onConfirmPasswordChange(val: string): void {
    this.confirmPassword.set(val);
  }

  onSubmit(): void {
    if (!this.nom.trim() || !this.prenom.trim() || !this.email.trim() || !this.password()) {
      this.errorMessage.set('Veuillez renseigner tous les champs obligatoires.');
      return;
    }

    if (this.password().length < 12) {
      this.errorMessage.set('Le mot de passe doit comporter au moins 12 caractères.');
      return;
    }

    if (this.password() !== this.confirmPassword()) {
      this.errorMessage.set('Les deux mots de passe ne sont pas identiques.');
      return;
    }

    if (!this.termsAccepted) {
      this.errorMessage.set('Vous devez accepter les conditions générales d\'utilisation.');
      return;
    }

    this.loading.set(true);
    this.errorMessage.set(null);
    this.successMessage.set(null);

    const payload: RegisterRequest = {
      nom: this.nom.trim(),
      prenom: this.prenom.trim(),
      email: this.email.trim(),
      password: this.password(),
      confirmPassword: this.confirmPassword(),
      termsAccepted: this.termsAccepted,
    };

    this.authService.register(payload).subscribe({
      next: () => {
        this.loading.set(false);
        this.successMessage.set(
          'Compte créé avec succès ! Un email de confirmation vous a été envoyé. Redirection en cours...'
        );
        setTimeout(() => {
          this.router.navigate(['/reparation']);
        }, 1500);
      },
      error: (err) => {
        this.loading.set(false);
        this.errorMessage.set(
          messageErreur(err, 'Une erreur est survenue lors de l\'inscription.')
        );
      },
    });
  }
}
