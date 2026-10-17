import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, ActivatedRoute } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { Item, ItemSearchParams, ItemType, ItemStatus } from '../../models/item.model';
import { ItemService } from '../../services/item.service';

@Component({
  selector: 'app-items-list',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  templateUrl: './items-list.component.html',
  styleUrl: './items-list.component.scss'
})
export class ItemsListComponent implements OnInit {

  items: Item[] = [];
  categories: string[] = [];
  locations: string[] = [];
  loading = false;
  error = '';
  successMessage = '';

  // Filtres de recherche
  keyword = '';
  selectedCategory = '';
  selectedLocation = '';
  selectedType: ItemType | '' = '';
  selectedStatus: ItemStatus | '' = '';

  // Filtre par type depuis la route (/items/lost ou /items/found)
  routeType: ItemType | null = null;

  constructor(
    private itemService: ItemService,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    // Détecter le type depuis la route
    this.route.url.subscribe(segments => {
      const last = segments[segments.length - 1]?.path;
      if (last === 'lost') {
        this.routeType = 'LOST';
        this.selectedType = 'LOST';
      } else if (last === 'found') {
        this.routeType = 'FOUND';
        this.selectedType = 'FOUND';
      }
    });

    this.loadItems();
    this.loadMeta();
  }

  loadItems(): void {
    this.loading = true;
    this.error = '';
    const params: ItemSearchParams = {};
    if (this.keyword) params.keyword = this.keyword;
    if (this.selectedCategory) params.category = this.selectedCategory;
    if (this.selectedLocation) params.location = this.selectedLocation;
    if (this.selectedType) params.type = this.selectedType as ItemType;
    if (this.selectedStatus) params.status = this.selectedStatus as ItemStatus;

    this.itemService.search(params).subscribe({
      next: (data) => {
        this.items = data;
        this.loading = false;
      },
      error: () => {
        this.error = 'Impossible de charger les objets. Vérifiez que le backend est démarré.';
        this.loading = false;
      }
    });
  }

  loadMeta(): void {
    this.itemService.getCategories().subscribe(c => this.categories = c);
    this.itemService.getLocations().subscribe(l => this.locations = l);
  }

  onSearch(): void {
    this.loadItems();
  }

  resetFilters(): void {
    this.keyword = '';
    this.selectedCategory = '';
    this.selectedLocation = '';
    this.selectedType = this.routeType ?? '';
    this.selectedStatus = '';
    this.loadItems();
  }

  confirmDelete(item: Item): void {
    if (confirm(`Supprimer "${item.title}" ?`)) {
      this.itemService.delete(item.id!).subscribe({
        next: () => {
          this.successMessage = `"${item.title}" supprimé avec succès.`;
          this.loadItems();
          setTimeout(() => this.successMessage = '', 3000);
        },
        error: () => {
          this.error = 'Erreur lors de la suppression.';
          setTimeout(() => this.error = '', 3000);
        }
      });
    }
  }

  confirmReturn(item: Item): void {
    if (confirm(`Confirmer la restitution de "${item.title}" ?`)) {
      this.itemService.confirmReturn(item.id!).subscribe({
        next: () => {
          this.successMessage = `"${item.title}" marqué comme restitué.`;
          this.loadItems();
          setTimeout(() => this.successMessage = '', 3000);
        },
        error: () => {
          this.error = 'Erreur lors de la confirmation.';
          setTimeout(() => this.error = '', 3000);
        }
      });
    }
  }

  getTypeLabel(type: string): string {
    return type === 'LOST' ? 'Perdu' : 'Trouvé';
  }

  getTypeClass(type: string): string {
    return type === 'LOST' ? 'badge-lost' : 'badge-found';
  }

  getStatusLabel(status: string): string {
    const map: Record<string, string> = {
      ACTIVE: 'Actif', CLAIMED: 'Réclamé', RETURNED: 'Restitué'
    };
    return map[status] || status;
  }

  getStatusClass(status: string): string {
    const map: Record<string, string> = {
      ACTIVE: 'status-active', CLAIMED: 'status-claimed', RETURNED: 'status-returned'
    };
    return map[status] || '';
  }

  get pageTitle(): string {
    if (this.routeType === 'LOST') return 'Objets perdus';
    if (this.routeType === 'FOUND') return 'Objets trouvés';
    return 'Tous les objets';
  }
}
