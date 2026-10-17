import { Routes } from '@angular/router';
import { CatalogComponent } from './components/catalog/catalog.component';
import { CreateItemComponent } from './components/create-item/create-item.component';
import { ItemDetailComponent } from './components/item-detail/item-detail.component';
import { OffersDashboardComponent } from './components/offers-dashboard/offers-dashboard.component';
import { AiLabComponent } from './components/ai-lab/ai-lab.component';

export const routes: Routes = [
  { path: '', component: CatalogComponent },
  { path: 'create', component: CreateItemComponent },
  { path: 'items/:id', component: ItemDetailComponent },
  { path: 'offers', component: OffersDashboardComponent },
  { path: 'ai-lab', component: AiLabComponent },
  { path: '**', redirectTo: '' }
];
