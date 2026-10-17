import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { DashboardStats } from '../../models/stats.model';
import { Item } from '../../models/item.model';
import { StatsService } from '../../services/stats.service';
import { ItemService } from '../../services/item.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss'
})
export class DashboardComponent implements OnInit {
  stats: DashboardStats | null = null;
  recentItems: Item[] = [];
  loading = true;
  error = '';

  constructor(
    private statsService: StatsService,
    private itemService: ItemService
  ) {}

  ngOnInit(): void {
    this.loadData();
  }

  loadData(): void {
    this.loading = true;
    this.statsService.getStats().subscribe({
      next: (data) => {
        this.stats = data;
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Impossible de charger les statistiques. Assurez-vous que le backend est démarré.';
        this.loading = false;
      }
    });

    this.itemService.search({}).subscribe({
      next: (items) => {
        this.recentItems = items.slice(0, 6);
      }
    });
  }

  getStatusLabel(status: string): string {
    const map: Record<string, string> = {
      ACTIVE: 'Actif', CLAIMED: 'Réclamé', RETURNED: 'Restitué'
    };
    return map[status] || status;
  }

  getTypeLabel(type: string): string {
    return type === 'LOST' ? 'Perdu' : 'Trouvé';
  }

  getTypeClass(type: string): string {
    return type === 'LOST' ? 'badge-lost' : 'badge-found';
  }
}
