import { Routes } from '@angular/router';

export const routes: Routes = [
  { path: '', redirectTo: '/dashboard', pathMatch: 'full' },
  {
    path: 'dashboard',
    loadComponent: () =>
      import('./pages/dashboard/dashboard.component').then(m => m.DashboardComponent)
  },
  {
    path: 'items',
    loadComponent: () =>
      import('./pages/items-list/items-list.component').then(m => m.ItemsListComponent)
  },
  {
    path: 'items/lost',
    loadComponent: () =>
      import('./pages/items-list/items-list.component').then(m => m.ItemsListComponent)
  },
  {
    path: 'items/found',
    loadComponent: () =>
      import('./pages/items-list/items-list.component').then(m => m.ItemsListComponent)
  },
  {
    path: 'items/new',
    loadComponent: () =>
      import('./pages/item-form/item-form.component').then(m => m.ItemFormComponent)
  },
  {
    path: 'items/:id/edit',
    loadComponent: () =>
      import('./pages/item-form/item-form.component').then(m => m.ItemFormComponent)
  },
  {
    path: 'items/:id',
    loadComponent: () =>
      import('./pages/item-detail/item-detail.component').then(m => m.ItemDetailComponent)
  },
  {
    path: 'claims',
    loadComponent: () =>
      import('./pages/claims/claims.component').then(m => m.ClaimsComponent)
  },
  { path: '**', redirectTo: '/dashboard' }
];
