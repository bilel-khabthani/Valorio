import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { AuthService, messageErreur } from '@valorio/shared';

@Component({
  selector: 'app-verify-email',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './verify-email.component.html',
  styleUrl: './verify-email.component.css',
})
export class VerifyEmailComponent implements OnInit {
  private readonly authService = inject(AuthService);
  private readonly route = inject(ActivatedRoute);

  token = '';
  resendEmail = '';

  loading = signal(false);
  successMessage = signal<string | null>(null);
  errorMessage = signal<string | null>(null);

  ngOnInit(): void {
    this.token = this.route.snapshot.queryParams['token'] || '';
    if (this.token) {
      this.verifierToken();
    }
  }

  verifierToken(): void {
    this.loading.set(true);
    this.errorMessage.set(null);
    this.successMessage.set(null);

    this.authService.verifyEmail(this.token).subscribe({
      next: (res) => {
        this.loading.set(false);
        this.successMessage.set(res.message);
      },
      error: (err) => {
        this.loading.set(false);
        this.errorMessage.set(messageErreur(err, 'Ce lien de confirmation est invalide ou a expiré.'));
      },
    });
  }

  onResend(): void {
    if (!this.resendEmail.trim()) {
      this.errorMessage.set('Veuillez saisir votre adresse email pour réexpédier le lien.');
      return;
    }

    this.loading.set(true);
    this.errorMessage.set(null);
    this.successMessage.set(null);

    this.authService.resendVerification(this.resendEmail.trim()).subscribe({
      next: (res) => {
        this.loading.set(false);
        this.successMessage.set(res.message);
      },
      error: (err) => {
        this.loading.set(false);
        this.errorMessage.set(messageErreur(err, 'Erreur lors du renvoi de l\'email.'));
      },
    });
  }
}
