import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { AiAssistantService } from '../../services/ai-assistant.service';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <header class="navbar">
      <div class="container nav-container">
        <a routerLink="/" class="nav-brand">
          <div class="brand-icon">
            <i class="fa-solid fa-boxes-packing"></i>
          </div>
          <div class="brand-text">
            <span class="brand-title">Lost & Found <span class="highlight">Marketplace</span></span>
            <span class="brand-badge"><i class="fa-solid fa-wand-magic-sparkles"></i> Module IA Revalorisation</span>
          </div>
        </a>

        <nav class="nav-links">
          <a routerLink="/" routerLinkActive="active" [routerLinkActiveOptions]="{exact: true}" class="nav-link">
            <i class="fa-solid fa-store"></i> Annonces
          </a>
          <a routerLink="/create" routerLinkActive="active" class="nav-link">
            <i class="fa-solid fa-circle-plus"></i> Déposer (IA Assistant)
          </a>
          <a routerLink="/offers" routerLinkActive="active" class="nav-link">
            <i class="fa-solid fa-handshake"></i> Négociations & Offres
          </a>
          <a routerLink="/ai-lab" routerLinkActive="active" class="nav-link nav-link-ai">
            <i class="fa-solid fa-microchip"></i> Lab IA & Tests Qualité
          </a>
        </nav>

        <div class="nav-ai-status">
          <span class="pulse-indicator" [class.pulse-active]="isAiHealthy"></span>
          <span class="ai-status-label">{{ isAiHealthy ? 'IA Opérationnelle' : 'IA Locale Standby' }}</span>
        </div>
      </div>
    </header>
  `,
  styles: [`
    .navbar {
      background: rgba(11, 15, 25, 0.85);
      backdrop-filter: blur(20px);
      border-bottom: 1px solid var(--border-glass);
      position: sticky;
      top: 0;
      z-index: 100;
      padding: 14px 0;
    }
    .nav-container {
      display: flex;
      align-items: center;
      justify-content: space-between;
      gap: 20px;
    }
    .nav-brand {
      display: flex;
      align-items: center;
      gap: 12px;
      text-decoration: none;
    }
    .brand-icon {
      width: 44px;
      height: 44px;
      border-radius: var(--radius-md);
      background: var(--primary-gradient);
      display: flex;
      align-items: center;
      justify-content: center;
      color: #fff;
      font-size: 1.3rem;
      box-shadow: 0 4px 14px rgba(99, 102, 241, 0.4);
    }
    .brand-text {
      display: flex;
      flex-direction: column;
    }
    .brand-title {
      font-family: var(--font-heading);
      font-size: 1.15rem;
      font-weight: 700;
      color: #fff;
    }
    .highlight {
      background: var(--primary-gradient);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
    }
    .brand-badge {
      font-size: 0.72rem;
      color: #c084fc;
      font-weight: 600;
    }
    .nav-links {
      display: flex;
      align-items: center;
      gap: 8px;
    }
    .nav-link {
      display: inline-flex;
      align-items: center;
      gap: 8px;
      padding: 8px 16px;
      font-size: 0.9rem;
      font-weight: 600;
      color: var(--text-muted);
      border-radius: var(--radius-md);
      transition: all var(--transition-fast);
      text-decoration: none;
    }
    .nav-link:hover {
      color: #fff;
      background: rgba(255, 255, 255, 0.05);
    }
    .nav-link.active {
      color: #fff;
      background: rgba(99, 102, 241, 0.15);
      border: 1px solid rgba(99, 102, 241, 0.3);
    }
    .nav-link-ai.active {
      background: rgba(168, 85, 247, 0.18);
      border-color: rgba(168, 85, 247, 0.4);
      color: #e9d5ff;
    }
    .nav-ai-status {
      display: flex;
      align-items: center;
      gap: 8px;
      padding: 6px 14px;
      background: rgba(16, 185, 129, 0.08);
      border: 1px solid rgba(16, 185, 129, 0.25);
      border-radius: var(--radius-full);
      font-size: 0.78rem;
      font-weight: 600;
      color: #34d399;
    }
    .pulse-indicator {
      width: 8px;
      height: 8px;
      border-radius: 50%;
      background: #10b981;
      box-shadow: 0 0 8px #10b981;
    }
    .pulse-active {
      animation: pulse 2s infinite;
    }
    @keyframes pulse {
      0% { opacity: 1; transform: scale(1); }
      50% { opacity: 0.4; transform: scale(1.3); }
      100% { opacity: 1; transform: scale(1); }
    }
    @media (max-width: 900px) {
      .nav-container { flex-direction: column; align-items: stretch; }
      .nav-links { flex-wrap: wrap; justify-content: center; }
      .nav-ai-status { align-self: center; }
    }
  `]
})
export class NavbarComponent implements OnInit {
  isAiHealthy = true;

  constructor(private aiService: AiAssistantService) {}

  ngOnInit(): void {
    this.aiService.getAiHealth().subscribe({
      next: () => this.isAiHealthy = true,
      error: () => this.isAiHealthy = false
    });
  }
}
