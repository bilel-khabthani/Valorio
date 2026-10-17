import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { AuthService, messageErreur } from '@valorio/shared';

@Component({
  selector: 'app-forgot-password',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './forgot-password.component.html',
  styleUrl: './forgot-password.component.css',
})
export class ForgotPasswordComponent {
  private readonly authService = inject(AuthService);

  email = '';
  loading = signal(false);
  errorMessage = signal<string | null>(null);
  successMessage = signal<string | null>(null);

  onSubmit(): void {
    if (!this.email.trim()) {
      this.errorMessage.set('Veuillez renseigner votre adresse email.');
      return;
    }

    this.loading.set(true);
    this.errorMessage.set(null);
    this.successMessage.set(null);

    this.authService.forgotPassword({ email: this.email.trim() }).subscribe({
      next: (res) => {
        this.loading.set(false);
        this.successMessage.set(res.message);
      },
      error: (err) => {
        this.loading.set(false);
        this.errorMessage.set(messageErreur(err, 'Une erreur est survenue lors de l\'envoi.'));
      },
    });
  }
}
