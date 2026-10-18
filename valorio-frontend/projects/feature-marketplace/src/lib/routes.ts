import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () => import('./marketplace-catalogue').then((m) => m.MarketplaceCatalogue),
  },
  {
    path: 'items/:id',
    loadComponent: () => import('./marketplace-item-detail').then((m) => m.MarketplaceItemDetail),
  },
  {
    path: 'create',
    loadComponent: () => import('./marketplace-create').then((m) => m.MarketplaceCreate),
  },
  {
    path: 'offers',
    loadComponent: () => import('./marketplace-offers').then((m) => m.MarketplaceOffers),
  },
  {
    path: 'ai-lab',
    loadComponent: () => import('./marketplace-ai-lab').then((m) => m.MarketplaceAiLab),
  },
];
