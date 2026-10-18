import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () => import('./feature-recommandation').then((m) => m.FeatureRecommandationComponent),
  },
];
