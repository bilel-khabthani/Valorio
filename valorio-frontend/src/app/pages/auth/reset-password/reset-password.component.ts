import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AuthService, ResetPasswordRequest, messageErreur } from '@valorio/shared';

@Component({
  selector: 'app-reset-password',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './reset-password.component.html',
  styleUrl: './reset-password.component.css',
})
export class ResetPasswordComponent implements OnInit {
  private readonly authService = inject(AuthService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  token = '';
  hasTokenFromUrl = signal(false);
  newPassword = signal('');
  confirmPassword = signal('');

  showPassword = signal(false);
  showConfirmPassword = signal(false);
  loading = signal(false);
  errorMessage = signal<string | null>(null);
  successMessage = signal<string | null>(null);

  readonly passwordLengthOk = computed(() => this.newPassword().length >= 12);
  readonly hasUppercase = computed(() => /[A-Z]/.test(this.newPassword()));
  readonly hasLowercase = computed(() => /[a-z]/.test(this.newPassword()));
  readonly hasNumber = computed(() => /[0-9]/.test(this.newPassword()));
  readonly hasSpecial = computed(() => /[^A-Za-z0-9]/.test(this.newPassword()));

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
    return { label: 'Sécurisé', color: '#16a34a' };
  });

  readonly passwordsMatch = computed(() => {
    return this.newPassword().length > 0 && this.newPassword() === this.confirmPassword();
  });

  ngOnInit(): void {
    const qToken = this.route.snapshot.queryParams['token'] || '';
    if (qToken) {
      this.token = qToken;
      this.hasTokenFromUrl.set(true);
    } else {
      this.errorMessage.set('Veuillez renseigner le code ou jeton reçu par email.');
    }
  }

  toggleShowPassword(): void {
    this.showPassword.update((v) => !v);
  }

  toggleShowConfirmPassword(): void {
    this.showConfirmPassword.update((v) => !v);
  }

  onPasswordChange(val: string): void {
    this.newPassword.set(val);
  }

  onConfirmPasswordChange(val: string): void {
    this.confirmPassword.set(val);
  }

  onSubmit(): void {
    if (!this.token.trim()) {
      this.errorMessage.set('Jeton de réinitialisation manquant.');
      return;
    }

    if (this.newPassword().length < 12) {
      this.errorMessage.set('Le mot de passe doit comporter au moins 12 caractères.');
      return;
    }

    if (this.newPassword() !== this.confirmPassword()) {
      this.errorMessage.set('Les deux mots de passe ne correspondent pas.');
      return;
    }

    this.loading.set(true);
    this.errorMessage.set(null);
    this.successMessage.set(null);

    const payload: ResetPasswordRequest = {
      token: this.token.trim(),
      newPassword: this.newPassword(),
      confirmPassword: this.confirmPassword(),
    };

    this.authService.resetPassword(payload).subscribe({
      next: (res) => {
        this.loading.set(false);
        this.successMessage.set(res.message);
        setTimeout(() => {
          this.router.navigate(['/login']);
        }, 2000);
      },
      error: (err) => {
        this.loading.set(false);
        this.errorMessage.set(messageErreur(err, 'Impossible de réinitialiser le mot de passe.'));
      },
    });
  }
}

