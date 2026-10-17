import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { Claim, ClaimStatus } from '../../models/claim.model';
import { ClaimService } from '../../services/claim.service';

@Component({
  selector: 'app-claims',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  templateUrl: './claims.component.html',
  styleUrl: './claims.component.scss'
})
export class ClaimsComponent implements OnInit {

  claims: Claim[] = [];
  filtered: Claim[] = [];
  loading = false;
  error = '';
  successMessage = '';

  selectedStatus: ClaimStatus | '' = '';

  constructor(private claimService: ClaimService) {}

  ngOnInit(): void {
    this.loadClaims();
  }

  loadClaims(): void {
    this.loading = true;
    this.claimService.getAll().subscribe({
      next: (data) => {
        this.claims = data;
        this.applyFilter();
        this.loading = false;
      },
      error: () => {
        this.error = 'Impossible de charger les réclamations.';
        this.loading = false;
      }
    });
  }

  applyFilter(): void {
    if (!this.selectedStatus) {
      this.filtered = [...this.claims];
    } else {
      this.filtered = this.claims.filter(c => c.status === this.selectedStatus);
    }
  }

  approve(claim: Claim): void {
    if (confirm('Approuver cette réclamation ? L\'objet sera marqué comme restitué.')) {
      this.claimService.approve(claim.id!).subscribe({
        next: (updated) => {
          const idx = this.claims.findIndex(c => c.id === claim.id);
          if (idx > -1) this.claims[idx] = updated;
          this.applyFilter();
          this.successMessage = 'Réclamation approuvée.';
          setTimeout(() => this.successMessage = '', 3000);
        },
        error: () => { this.error = 'Erreur lors de l\'approbation.'; }
      });
    }
  }

  reject(claim: Claim): void {
    if (confirm('Rejeter cette réclamation ?')) {
      this.claimService.reject(claim.id!).subscribe({
        next: (updated) => {
          const idx = this.claims.findIndex(c => c.id === claim.id);
          if (idx > -1) this.claims[idx] = updated;
          this.applyFilter();
          this.successMessage = 'Réclamation rejetée.';
          setTimeout(() => this.successMessage = '', 3000);
        },
        error: () => { this.error = 'Erreur lors du rejet.'; }
      });
    }
  }

  deleteClaim(claim: Claim): void {
    if (confirm('Supprimer cette réclamation définitivement ?')) {
      this.claimService.delete(claim.id!).subscribe({
        next: () => {
          this.claims = this.claims.filter(c => c.id !== claim.id);
          this.applyFilter();
          this.successMessage = 'Réclamation supprimée.';
          setTimeout(() => this.successMessage = '', 3000);
        },
        error: () => { this.error = 'Erreur lors de la suppression.'; }
      });
    }
  }

  getStatusLabel(status: string): string {
    const map: Record<string, string> = { PENDING: 'En attente', APPROVED: 'Approuvée', REJECTED: 'Rejetée' };
    return map[status] || status;
  }

  getStatusClass(status: string): string {
    const map: Record<string, string> = {
      PENDING: 'status-active', APPROVED: 'status-returned', REJECTED: 'status-claimed'
    };
    return map[status] || '';
  }

  get pendingCount(): number { return this.claims.filter(c => c.status === 'PENDING').length; }
  get approvedCount(): number { return this.claims.filter(c => c.status === 'APPROVED').length; }
  get rejectedCount(): number { return this.claims.filter(c => c.status === 'REJECTED').length; }
}
