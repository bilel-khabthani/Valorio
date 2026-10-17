import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () => import('./feature-don').then((m) => m.FeatureDonHome),
  },
  {
    path: 'annonces/:id',
    loadComponent: () => import('./annonce-detail').then((m) => m.AnnonceDetail),
  },
];
