import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./feature-recyclage').then((m) => m.FeatureRecyclageHome),
  },
  {
    path: 'collectes/:id',
    loadComponent: () => import('./collecte-detail').then((m) => m.CollecteDetail),
  },
];
