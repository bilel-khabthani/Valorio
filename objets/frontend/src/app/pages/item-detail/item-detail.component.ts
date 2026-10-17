import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, ActivatedRoute, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { Item } from '../../models/item.model';
import { Claim } from '../../models/claim.model';
import { ItemService } from '../../services/item.service';
import { ClaimService } from '../../services/claim.service';

@Component({
  selector: 'app-item-detail',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  templateUrl: './item-detail.component.html',
  styleUrl: './item-detail.component.scss'
})
export class ItemDetailComponent implements OnInit {

  item: Item | null = null;
  claims: Claim[] = [];
  loading = true;
  error = '';
  successMessage = '';

  showClaimForm = false;
  claimForm = {
    claimantName: '',
    claimantContact: '',
    description: ''
  };
  savingClaim = false;
  claimError = '';

  constructor(
    private itemService: ItemService,
    private claimService: ClaimService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.loadItem(+id);
      this.loadClaims(+id);
    }
  }

  loadItem(id: number): void {
    this.loading = true;
    this.itemService.getById(id).subscribe({
      next: (item) => { this.item = item; this.loading = false; },
      error: () => { this.error = 'Objet introuvable.'; this.loading = false; }
    });
  }

  loadClaims(id: number): void {
    this.claimService.getByItemId(id).subscribe({
      next: (claims) => { this.claims = claims; }
    });
  }

  confirmReturn(): void {
    if (!this.item) return;
    if (confirm(`Confirmer la restitution de "${this.item.title}" ?`)) {
      this.itemService.confirmReturn(this.item.id!).subscribe({
        next: (updated) => {
          this.item = updated;
          this.successMessage = 'Objet marqué comme restitué avec succès.';
          setTimeout(() => this.successMessage = '', 3000);
        },
        error: () => { this.error = 'Erreur lors de la confirmation.'; }
      });
    }
  }

  deleteItem(): void {
    if (!this.item) return;
    if (confirm(`Supprimer "${this.item.title}" définitivement ?`)) {
      this.itemService.delete(this.item.id!).subscribe({
        next: () => this.router.navigate(['/items']),
        error: () => { this.error = 'Erreur lors de la suppression.'; }
      });
    }
  }

  submitClaim(): void {
    if (!this.claimForm.claimantName.trim()) { this.claimError = 'Le nom est obligatoire.'; return; }
    if (!this.claimForm.claimantContact.trim()) { this.claimError = 'Le contact est obligatoire.'; return; }

    this.savingClaim = true;
    this.claimError = '';

    this.claimService.create({
      itemId: this.item!.id,
      ...this.claimForm
    }).subscribe({
      next: (claim) => {
        this.claims.push(claim);
        this.savingClaim = false;
        this.showClaimForm = false;
        this.claimForm = { claimantName: '', claimantContact: '', description: '' };
        this.successMessage = 'Réclamation soumise avec succès.';
        // Recharger l'item pour mettre à jour le statut
        this.loadItem(this.item!.id!);
        setTimeout(() => this.successMessage = '', 3000);
      },
      error: (err) => {
        this.savingClaim = false;
        this.claimError = err?.error?.error || 'Erreur lors de la soumission.';
      }
    });
  }

  approveClaim(claim: Claim): void {
    if (confirm('Approuver cette réclamation ? L\'objet sera marqué comme restitué.')) {
      this.claimService.approve(claim.id!).subscribe({
        next: (updated) => {
          const idx = this.claims.findIndex(c => c.id === claim.id);
          if (idx > -1) this.claims[idx] = updated;
          this.loadItem(this.item!.id!);
          this.successMessage = 'Réclamation approuvée.';
          setTimeout(() => this.successMessage = '', 3000);
        }
      });
    }
  }

  rejectClaim(claim: Claim): void {
    if (confirm('Rejeter cette réclamation ?')) {
      this.claimService.reject(claim.id!).subscribe({
        next: (updated) => {
          const idx = this.claims.findIndex(c => c.id === claim.id);
          if (idx > -1) this.claims[idx] = updated;
          this.loadItem(this.item!.id!);
          this.successMessage = 'Réclamation rejetée.';
          setTimeout(() => this.successMessage = '', 3000);
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
    const map: Record<string, string> = { ACTIVE: 'Actif', CLAIMED: 'Réclamé', RETURNED: 'Restitué' };
    return map[status] || status;
  }

  getStatusClass(status: string): string {
    const map: Record<string, string> = {
      ACTIVE: 'status-active', CLAIMED: 'status-claimed', RETURNED: 'status-returned'
    };
    return map[status] || '';
  }

  getClaimStatusLabel(status: string): string {
    const map: Record<string, string> = { PENDING: 'En attente', APPROVED: 'Approuvée', REJECTED: 'Rejetée' };
    return map[status] || status;
  }

  getClaimStatusClass(status: string): string {
    const map: Record<string, string> = {
      PENDING: 'status-active', APPROVED: 'status-returned', REJECTED: 'status-claimed'
    };
    return map[status] || '';
  }
}
