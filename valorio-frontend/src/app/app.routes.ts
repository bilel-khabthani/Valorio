import { Routes } from '@angular/router';
import { accueilGuard, acteurGuard } from '@valorio/shared';

export const routes: Routes = [
  {
    path: '',
    pathMatch: 'full',
    canActivate: [accueilGuard],
    loadComponent: () =>
      import('./pages/accueil/accueil.component').then((m) => m.AccueilComponent),
  },
  {
    path: 'utilisateur',
    canActivate: [acteurGuard(['UTILISATEUR'])],
    loadComponent: () =>
      import('./pages/espaces/espace-utilisateur.component').then(
        (m) => m.EspaceUtilisateurComponent
      ),
  },
  {
    path: 'reparateur',
    canActivate: [acteurGuard(['REPARATEUR'])],
    loadComponent: () =>
      import('./pages/espaces/espace-reparateur.component').then(
        (m) => m.EspaceReparateurComponent
      ),
  },
  {
    path: 'acheteur',
    canActivate: [acteurGuard(['ACHETEUR'])],
    loadComponent: () =>
      import('./pages/espaces/espace-acheteur.component').then(
        (m) => m.EspaceAcheteurComponent
      ),
  },
  {
    path: 'organisme',
    canActivate: [acteurGuard(['ORGANISME'])],
    loadComponent: () =>
      import('./pages/espaces/espace-organisme.component').then(
        (m) => m.EspaceOrganismeComponent
      ),
  },
  {
    path: 'admin',
    canActivate: [acteurGuard(['ADMIN'])],
    loadChildren: () => import('@valorio/feature-admin').then((m) => m.routes),
  },
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
    path: 'objets',
    loadChildren: () => import('@valorio/feature-objets').then((m) => m.routes),
  },
  {
    path: 'marketplace',
    canActivate: [acteurGuard(['ACHETEUR'])],
    loadChildren: () => import('@valorio/feature-marketplace').then((m) => m.routes),
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
  { path: '**', redirectTo: '' },
];
