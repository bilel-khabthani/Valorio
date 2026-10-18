import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./admin-dashboard').then((m) => m.AdminDashboard),
    children: [
      {
        path: '',
        loadComponent: () =>
          import('./overview/admin-overview').then((m) => m.AdminOverview),
      },
      {
        path: 'utilisateurs',
        loadComponent: () =>
          import('./utilisateurs/admin-utilisateurs').then((m) => m.AdminUtilisateurs),
      },
      {
        path: 'reparateurs',
        loadComponent: () =>
          import('./reparateurs/admin-reparateurs').then((m) => m.AdminReparateurs),
      },
      {
        path: 'organismes',
        loadComponent: () =>
          import('./organismes/admin-organismes').then((m) => m.AdminOrganismes),
      },
      {
        path: 'categories',
        loadComponent: () =>
          import('./categories/admin-categories').then((m) => m.AdminCategories),
      },
      {
        path: 'annonces',
        loadComponent: () =>
          import('./annonces/admin-annonces').then((m) => m.AdminAnnonces),
      },
      {
        path: 'signalements',
        loadComponent: () =>
          import('./signalements/admin-signalements').then((m) => m.AdminSignalements),
      },
      {
        path: 'moderation',
        loadComponent: () =>
          import('./moderation/admin-moderation').then((m) => m.AdminModeration),
      },
      {
        path: 'statistiques',
        loadComponent: () =>
          import('./statistiques/admin-statistiques').then((m) => m.AdminStatistiques),
      },
    ],
  },
];
