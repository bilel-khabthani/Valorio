import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () => import('./feature-objets').then((m) => m.FeatureObjetsHome),
  },
  {
    path: 'objet/:id',
    loadComponent: () => import('./objet-detail').then((m) => m.ObjetDetail),
  },
];
