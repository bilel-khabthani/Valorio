import { Component, computed, inject } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from '@valorio/shared';

interface LienAdmin {
  path: string;
  label: string;
  icone: string;
  exact?: boolean;
}

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  template: `
    <div class="admin-shell">
      <aside class="admin-side">
        <div class="admin-brand">
          <span class="admin-brand-title">Valorio</span>
          <span class="admin-brand-sub">Administration</span>
        </div>
        <nav class="admin-nav">
          @for (lien of liens; track lien.path) {
            <a
              [routerLink]="lien.path"
              routerLinkActive="active"
              [routerLinkActiveOptions]="{ exact: !!lien.exact }"
            >
              <span class="ico">{{ lien.icone }}</span>
              <span>{{ lien.label }}</span>
            </a>
          }
        </nav>
        <div class="admin-side-foot">
          <div class="admin-id">
            <span class="admin-avatar">{{ initiale() }}</span>
            <span class="admin-name">{{ auth.currentUser()?.nomComplet || 'Administrateur' }}</span>
          </div>
          <button type="button" class="admin-logout" (click)="onLogout()">Déconnexion</button>
        </div>
      </aside>

      <section class="admin-main">
        <router-outlet />
      </section>
    </div>
  `,
  styles: [
    `
      .admin-shell {
        display: grid;
        grid-template-columns: 250px 1fr;
        min-height: 100vh;
        background: var(--bg);
      }
      .admin-side {
        background: #0f172a;
        color: #cbd5e1;
        display: flex;
        flex-direction: column;
        padding: 1.25rem 0.9rem;
        gap: 1rem;
        position: sticky;
        top: 0;
        height: 100vh;
        overflow-y: auto;
      }
      .admin-brand {
        display: flex;
        flex-direction: column;
        padding: 0 0.6rem 0.75rem;
        border-bottom: 1px solid rgba(148, 163, 184, 0.2);
      }
      .admin-brand-title {
        font-size: 1.05rem;
        font-weight: 700;
        color: #f8fafc;
        letter-spacing: 0.02em;
      }
      .admin-brand-sub {
        font-size: 0.72rem;
        text-transform: uppercase;
        letter-spacing: 0.12em;
        color: #64748b;
      }
      .admin-nav {
        display: flex;
        flex-direction: column;
        gap: 0.15rem;
        flex: 1;
      }
      .admin-nav a {
        display: flex;
        align-items: center;
        gap: 0.65rem;
        padding: 0.6rem 0.7rem;
        border-radius: 8px;
        color: #cbd5e1;
        text-decoration: none;
        font-size: 0.92rem;
        transition: background 0.15s ease, color 0.15s ease;
      }
      .admin-nav a:hover {
        background: rgba(148, 163, 184, 0.14);
        color: #f8fafc;
      }
      .admin-nav a.active {
        background: var(--primary);
        color: #fff;
        font-weight: 600;
      }
      .admin-nav .ico {
        width: 1.3rem;
        text-align: center;
        font-size: 0.95rem;
      }
      .admin-side-foot {
        display: flex;
        flex-direction: column;
        gap: 0.65rem;
        padding: 0.85rem 0.6rem 0;
        border-top: 1px solid rgba(148, 163, 184, 0.2);
      }
      .admin-id {
        display: flex;
        align-items: center;
        gap: 0.6rem;
      }
      .admin-avatar {
        width: 1.9rem;
        height: 1.9rem;
        flex: 0 0 auto;
        border-radius: 50%;
        background: var(--primary);
        color: #fff;
        display: flex;
        align-items: center;
        justify-content: center;
        font-weight: 700;
        font-size: 0.82rem;
      }
      .admin-name {
        font-size: 0.82rem;
        font-weight: 600;
        color: #f8fafc;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
      .admin-logout {
        width: 100%;
        background: none;
        border: 1px solid #475569;
        color: #cbd5e1;
        font-size: 0.78rem;
        font-weight: 500;
        padding: 0.4rem 0.5rem;
        border-radius: 6px;
        cursor: pointer;
        transition: all 0.15s ease;
      }
      .admin-logout:hover {
        background: #dc2626;
        border-color: #dc2626;
        color: #fff;
      }
      .admin-main {
        min-width: 0;
        overflow-x: auto;
      }
      @media (max-width: 820px) {
        .admin-shell {
          grid-template-columns: 1fr;
        }
        .admin-side {
          flex-direction: row;
          flex-wrap: wrap;
          align-items: center;
        }
        .admin-brand { border-bottom: none; }
        .admin-nav {
          flex-direction: row;
          flex-wrap: wrap;
          flex: 1 1 100%;
        }
        .admin-side-foot { display: none; }
      }
    `,
  ],
})
export class AdminDashboard {
  protected readonly auth = inject(AuthService);

  protected readonly initiale = computed(() =>
    (this.auth.currentUser()?.nomComplet || 'A').charAt(0).toUpperCase()
  );

  onLogout(): void {
    this.auth.logout().subscribe();
  }

  protected readonly liens: LienAdmin[] = [
    { path: '/admin', label: "Vue d'ensemble", icone: '📊', exact: true },
    { path: '/admin/utilisateurs', label: 'Utilisateurs', icone: '👥' },
    { path: '/admin/reparateurs', label: 'Réparateurs', icone: '🔧' },
    { path: '/admin/organismes', label: 'Organismes', icone: '♻️' },
    { path: '/admin/categories', label: 'Catégories', icone: '🏷️' },
    { path: '/admin/annonces', label: 'Annonces', icone: '📢' },
    { path: '/admin/signalements', label: 'Signalements', icone: '🚩' },
    { path: '/admin/moderation', label: 'Modération', icone: '🛡️' },
    { path: '/admin/statistiques', label: 'Statistiques', icone: '📈' },
  ];
}
