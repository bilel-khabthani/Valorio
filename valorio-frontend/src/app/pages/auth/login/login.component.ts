import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AuthService, LoginRequest, messageErreur } from '@valorio/shared';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css',
})
export class LoginComponent {
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  email = '';
  password = '';
  rememberMe = false;

  showPassword = signal(false);
  loading = signal(false);
  errorMessage = signal<string | null>(null);

  toggleShowPassword(): void {
    this.showPassword.update((v) => !v);
  }

  onSubmit(): void {
    if (!this.email || !this.password) {
      this.errorMessage.set('Veuillez renseigner votre email et votre mot de passe.');
      return;
    }

    this.loading.set(true);
    this.errorMessage.set(null);

    const credentials: LoginRequest = {
      email: this.email.trim(),
      password: this.password,
      rememberMe: this.rememberMe,
    };

    this.authService.login(credentials).subscribe({
      next: () => {
        this.loading.set(false);
        const returnUrl = this.route.snapshot.queryParams['returnUrl'] || '/reparation';
        this.router.navigateByUrl(returnUrl);
      },
      error: (err) => {
        this.loading.set(false);
        this.errorMessage.set(messageErreur(err, 'Identifiants invalides ou service indisponible.'));
      },
    });
  }
}
