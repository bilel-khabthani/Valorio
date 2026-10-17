import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./feature-reparation').then((m) => m.FeatureReparationHome),
  },
  {
    path: 'demandes/:id',
    loadComponent: () => import('./demande-detail').then((m) => m.DemandeDetail),
  },
];
