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
  { path: '**', redirectTo: 'reparation' },
];
