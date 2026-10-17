import { Routes } from '@angular/router';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'reparation' },
  {
    path: 'reparation',
    loadChildren: () => import('@valorio/feature-reparation').then((m) => m.routes),
  },
  {
    path: 'don',
    loadChildren: () => import('@valorio/feature-don').then((m) => m.routes),
  },
  {
    path: 'recyclage',
    loadChildren: () => import('@valorio/feature-recyclage').then((m) => m.routes),
  },
  {
    path: 'login',
    loadComponent: () =>
      import('./pages/auth/login/login.component').then((m) => m.LoginComponent),
  },
  {
    path: 'register',
    loadComponent: () =>
      import('./pages/auth/register/register.component').then((m) => m.RegisterComponent),
  },
  {
    path: 'forgot-password',
    loadComponent: () =>
      import('./pages/auth/forgot-password/forgot-password.component').then(
        (m) => m.ForgotPasswordComponent
      ),
  },
  {
    path: 'reset-password',
    loadComponent: () =>
      import('./pages/auth/reset-password/reset-password.component').then(
        (m) => m.ResetPasswordComponent
      ),
  },
  {
    path: 'verify-email',
    loadComponent: () =>
      import('./pages/auth/verify-email/verify-email.component').then(
        (m) => m.VerifyEmailComponent
      ),
  },
  { path: '**', redirectTo: 'reparation' },
];

