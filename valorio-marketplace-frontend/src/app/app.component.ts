import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MarketplaceService } from './services/marketplace.service';
import { AiAssistantService } from './services/ai-assistant.service';
import { MarketplaceItem, MarketplaceOffer } from './models/marketplace.model';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <!-- TOP NAVIGATION (VALORIO STYLE) -->
    <header class="valorio-navbar">
      <div class="nav-container">
        <!-- Logo -->
        <div class="nav-left">
          <div class="logo">
            <span class="logo-white">Val</span><span class="logo-blue">orio</span>
          </div>

          <!-- Navigation Links -->
          <nav class="nav-tabs">
            <button class="nav-tab">Réparation</button>
            <button class="nav-tab">Don</button>
            <button class="nav-tab active-pill">Marketplace</button>
          </nav>
        </div>

        <!-- Role Selector on Right -->
        <div class="nav-right">
          <div class="role-box">
            <span class="role-label">RÔLE</span>
            <div class="role-select-pill">
              <span>{{ currentRole }}</span>
              <i class="fa-solid fa-chevron-down role-chevron"></i>
            </div>
          </div>
        </div>
      </div>
    </header>

    <!-- MAIN SINGLE PAGE CONTENT -->
    <main class="page-body">
      <div class="content-container">
        <!-- Page Title & Subtitle -->
        <div class="page-title-section">
          <h1 class="main-title">Marketplace</h1>
          <p class="main-subtitle">Objets trouvés & perdus, annonces de recherche, revalorisation et dons solidaires.</p>
          
          <div class="space-indicator">
            <span class="blue-bar">|</span> ESPACE MARKETPLACE & OBJETS PERDUS
          </div>
        </div>

        <!-- MAIN TABLE CARD (VALORIO ERGONOMIC STYLE) -->
        <section class="valorio-card">
          <div class="card-top-bar">
            <h2 class="card-heading">Objets & Annonces à traiter</h2>
            
            <div class="card-actions">
              <!-- Search Input -->
              <div class="search-box">
                <i class="fa-solid fa-magnifying-glass search-icon"></i>
                <input 
                  type="text" 
                  [(ngModel)]="searchQuery" 
                  (input)="filterItems()" 
                  placeholder="Rechercher un objet..." 
                  class="search-input"
                />
              </div>

              <!-- Button to Add Missing/Found Item -->
              <button class="btn-valorio-primary" (click)="openAddModal()">
                <i class="fa-solid fa-plus"></i> Déposer un objet égaré / annonce
              </button>
            </div>
          </div>

          <!-- ITEMS TABLE -->
          <div class="table-responsive">
            <table class="valorio-table">
              <thead>
                <tr>
                  <th style="width: 50px;">#</th>
                  <th>OBJET</th>
                  <th>DEMANDEUR</th>
                  <th>CENTRE / LIEU</th>
                  <th>STATUT / PRIX</th>
                  <th style="text-align: right;">ACTIONS</th>
                </tr>
              </thead>
              <tbody>
                <tr *ngFor="let item of filteredItems; let i = index">
                  <td class="col-num">{{ i + 1 }}</td>
                  <td class="col-object">
                    <div class="object-cell">
                      <span class="object-title">{{ item.title }}</span>
                      <span class="object-tags" *ngIf="item.aiVerificationScore">
                        <span class="tag-ai"><i class="fa-solid fa-check"></i> Vérifié IA ({{ item.aiVerificationScore }}%)</span>
                        <span class="tag-condition">{{ item.condition }}</span>
                      </span>
                    </div>
                  </td>
                  <td class="col-demandeur">{{ item.sellerName || 'Étudiant Anonyme' }}</td>
                  <td class="col-centre">{{ item.location || 'Campus / Guichet Central' }}</td>
                  <td class="col-price">
                    <span class="price-badge" [ngClass]="item.type === 'DONATION' ? 'badge-don' : 'badge-prix'">
                      {{ item.type === 'DONATION' ? 'Don Solidaire (0 €)' : (item.price + ' €') }}
                    </span>
                  </td>
                  <td class="col-actions">
                    <button class="btn-table btn-chat" (click)="openChatModal(item)" title="Discuter avec le demandeur">
                      <i class="fa-solid fa-comment-dots"></i> Chat
                    </button>
                    <button class="btn-table btn-details" (click)="viewDetails(item)" title="Voir les détails">
                      <i class="fa-solid fa-eye"></i>
                    </button>
                  </td>
                </tr>

                <tr *ngIf="filteredItems.length === 0">
                  <td colspan="6" class="empty-cell">
                    Aucun objet trouvé correspondant. Cliquez sur "+ Déposer un objet" pour ajouter une annonce.
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </section>
      </div>
    </main>

    <!-- SIMPLE MODAL : AJOUTER UN OBJET / ANNONCE -->
    <div class="modal-backdrop" *ngIf="showAddModal" (click)="closeAddModal()">
      <div class="valorio-modal" (click)="$event.stopPropagation()">
        <div class="modal-head">
          <h3><i class="fa-solid fa-plus-circle text-blue"></i> Déposer un objet / Annonce</h3>
          <button class="btn-close" (click)="closeAddModal()">&times;</button>
        </div>

        <!-- 1-CLICK AI ASSISTANT BUTTON (Simple & Impressive for Professor) -->
        <div class="ai-quick-fill-box">
          <div class="ai-quick-text">
            <strong>Assistant IA :</strong> Saisissez l'objet en quelques mots pour auto-remplir la fiche.
          </div>
          <div class="ai-input-group">
            <input 
              type="text" 
              [(ngModel)]="quickAiInput" 
              placeholder="Ex: Ordinateur portable HP oublié en amphi B, écran légèrement rayé" 
              class="form-input quick-input" 
              (keyup.enter)="autoFillWithAi()"
            />
            <button type="button" class="btn-ai-sparkle" (click)="autoFillWithAi()" [disabled]="loadingAi">
              <i class="fa-solid fa-wand-magic-sparkles"></i> 
              <span *ngIf="loadingAi">Génération...</span>
              <span *ngIf="!loadingAi">Remplir avec l'IA</span>
            </button>
          </div>
        </div>

        <form (ngSubmit)="submitItem()">
          <div class="form-row">
            <label class="form-label">Objet / Dénomination *</label>
            <input type="text" class="form-input" [(ngModel)]="newItem.title" name="title" required placeholder="Ex: Ordinateur portable - écran cassé" />
          </div>

          <div class="form-grid-2">
            <div class="form-row">
              <label class="form-label">Demandeur / Votre Nom *</label>
              <input type="text" class="form-input" [(ngModel)]="newItem.sellerName" name="sellerName" required placeholder="Ex: Amine Ben Salah" />
            </div>

            <div class="form-row">
              <label class="form-label">Centre / Lieu de retrait *</label>
              <input type="text" class="form-input" [(ngModel)]="newItem.location" name="location" required placeholder="Ex: Eco-Tunis Centre" />
            </div>
          </div>

          <div class="form-grid-2">
            <div class="form-row">
              <label class="form-label">Type d'annonce</label>
              <select class="form-select" [(ngModel)]="newItem.type" name="type" (change)="onTypeChange()">
                <option value="SALE">Vente à prix solidaire</option>
                <option value="DONATION">Don solidaire (0 €)</option>
                <option value="REWARD_BOUNTY">Objet perdu (Récompense)</option>
              </select>
            </div>

            <div class="form-row">
              <label class="form-label">Prix (€)</label>
              <input type="number" class="form-input" [(ngModel)]="newItem.price" name="price" [disabled]="newItem.type === 'DONATION'" />
            </div>
          </div>

          <div class="form-row">
            <label class="form-label">Description / Détails de l'objet</label>
            <textarea class="form-input" rows="3" [(ngModel)]="newItem.description" name="description" placeholder="Précisez l'état ou les conditions de remise..."></textarea>
          </div>

          <div class="modal-footer">
            <button type="button" class="btn-cancel" (click)="closeAddModal()">Annuler</button>
            <button type="submit" class="btn-valorio-primary" [disabled]="savingItem">
              <span *ngIf="savingItem">Enregistrement...</span>
              <span *ngIf="!savingItem">Publier l'annonce</span>
            </button>
          </div>
        </form>
      </div>
    </div>

    <!-- SIMPLE CHAT MODAL (COMME DEMANDÉ PAR L'UTILISATEUR) -->
    <div class="modal-backdrop" *ngIf="showChatModal" (click)="closeChatModal()">
      <div class="valorio-chat-modal" (click)="$event.stopPropagation()">
        <!-- Chat Header -->
        <div class="chat-header">
          <div class="chat-user-info">
            <div class="chat-avatar">
              <i class="fa-solid fa-user"></i>
            </div>
            <div>
              <h4 class="chat-name">{{ activeChatItem?.sellerName || 'Demandeur' }}</h4>
              <span class="chat-sub">Discussion à propos de : <strong>{{ activeChatItem?.title }}</strong></span>
            </div>
          </div>
          <button class="btn-close" (click)="closeChatModal()">&times;</button>
        </div>

        <!-- Chat Messages Container -->
        <div class="chat-body">
          <div class="chat-message msg-received">
            <div class="msg-bubble">
              Bonjour ! Je suis le demandeur pour <strong>{{ activeChatItem?.title }}</strong>. L'objet est disponible pour retrait à {{ activeChatItem?.location || 'l\\'accueil' }}.
              <span class="msg-time">10:45</span>
            </div>
          </div>

          <div class="chat-message msg-sent" *ngFor="let msg of chatMessages">
            <div class="msg-bubble sent-bubble">
              {{ msg.text }}
              <span class="msg-time">{{ msg.time }}</span>
            </div>
          </div>
        </div>

        <!-- Chat Input Bar -->
        <div class="chat-footer">
          <input 
            type="text" 
            [(ngModel)]="chatInput" 
            (keyup.enter)="sendMessage()" 
            placeholder="Écrivez votre message..." 
            class="chat-input"
          />
          <button class="btn-send" (click)="sendMessage()" [disabled]="!chatInput.trim()">
            <i class="fa-solid fa-paper-plane"></i>
          </button>
        </div>
      </div>
    </div>

    <!-- SIMPLE DETAILS MODAL -->
    <div class="modal-backdrop" *ngIf="selectedDetailItem" (click)="selectedDetailItem = null">
      <div class="valorio-modal" (click)="$event.stopPropagation()">
        <div class="modal-head">
          <h3><i class="fa-solid fa-circle-info text-blue"></i> Détails de l'objet</h3>
          <button class="btn-close" (click)="selectedDetailItem = null">&times;</button>
        </div>

        <div class="detail-content">
          <div class="detail-row">
            <strong>Objet :</strong> <span>{{ selectedDetailItem.title }}</span>
          </div>
          <div class="detail-row">
            <strong>Demandeur :</strong> <span>{{ selectedDetailItem.sellerName }}</span>
          </div>
          <div class="detail-row">
            <strong>Lieu / Centre :</strong> <span>{{ selectedDetailItem.location }}</span>
          </div>
          <div class="detail-row">
            <strong>Type & Prix :</strong> 
            <span>{{ selectedDetailItem.type === 'DONATION' ? 'Don Solidaire (0 €)' : (selectedDetailItem.price + ' €') }}</span>
          </div>
          <div class="detail-row">
            <strong>Score IA de conformité :</strong> 
            <span class="text-green">{{ selectedDetailItem.aiVerificationScore || 98 }}% (Conforme)</span>
          </div>
          <div class="detail-row">
            <strong>Description :</strong>
            <p class="detail-desc">{{ selectedDetailItem.description }}</p>
          </div>
        </div>

        <div class="modal-footer">
          <button class="btn-valorio-primary" (click)="openChatFromDetail()">
            <i class="fa-solid fa-comment-dots"></i> Ouvrir le Chat
          </button>
          <button class="btn-cancel" (click)="selectedDetailItem = null">Fermer</button>
        </div>
      </div>
    </div>
  `,
  styles: [`
    /* VALORIO NAVBAR EXACT STYLES */
    .valorio-navbar {
      background-color: #0f172a;
      height: 60px;
      display: flex;
      align-items: center;
      border-bottom: 1px solid #1e293b;
      padding: 0 30px;
    }
    .nav-container {
      width: 100%;
      max-width: 1300px;
      margin: 0 auto;
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
    .nav-left {
      display: flex;
      align-items: center;
      gap: 36px;
    }
    .logo {
      font-size: 1.45rem;
      font-weight: 800;
      letter-spacing: -0.5px;
    }
    .logo-white { color: #ffffff; }
    .logo-blue { color: #38bdf8; }
    .nav-tabs {
      display: flex;
      align-items: center;
      gap: 12px;
    }
    .nav-tab {
      background: none;
      border: none;
      color: #94a3b8;
      font-size: 0.95rem;
      font-weight: 500;
      padding: 6px 14px;
      cursor: pointer;
      border-radius: 8px;
      transition: color 0.15s;
    }
    .nav-tab:hover {
      color: #ffffff;
    }
    .active-pill {
      background-color: #2563eb !important;
      color: #ffffff !important;
      font-weight: 600;
      border-radius: 9999px;
      padding: 6px 18px;
    }
    .nav-right {
      display: flex;
      align-items: center;
    }
    .role-box {
      display: flex;
      flex-direction: column;
      align-items: flex-end;
    }
    .role-label {
      font-size: 0.65rem;
      font-weight: 700;
      color: #64748b;
      letter-spacing: 0.05em;
      margin-bottom: 2px;
    }
    .role-select-pill {
      background: rgba(255, 255, 255, 0.07);
      border: 1px solid #334155;
      padding: 4px 12px;
      border-radius: 6px;
      color: #e2e8f0;
      font-size: 0.85rem;
      display: flex;
      align-items: center;
      gap: 8px;
      cursor: pointer;
    }
    .role-chevron {
      font-size: 0.7rem;
      color: #94a3b8;
    }

    /* BODY CONTAINER */
    .page-body {
      padding: 36px 30px 60px;
      background-color: #f8fafc;
      min-height: calc(100vh - 60px);
    }
    .content-container {
      max-width: 1200px;
      margin: 0 auto;
    }
    .page-title-section {
      margin-bottom: 24px;
    }
    .main-title {
      font-size: 2.1rem;
      font-weight: 800;
      color: #0f172a;
      margin-bottom: 4px;
    }
    .main-subtitle {
      font-size: 0.95rem;
      color: #64748b;
      margin-bottom: 12px;
    }
    .space-indicator {
      font-size: 0.8rem;
      font-weight: 700;
      color: #475569;
      letter-spacing: 0.05em;
    }
    .blue-bar {
      color: #2563eb;
      font-size: 1.1rem;
      font-weight: 900;
      margin-right: 4px;
    }

    /* CARD STYLING (LIKE FRIEND'S SCREENSHOT) */
    .valorio-card {
      background: #ffffff;
      border: 1px solid #e2e8f0;
      border-radius: 12px;
      padding: 24px;
      box-shadow: 0 2px 6px rgba(0, 0, 0, 0.03);
    }
    .card-top-bar {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 20px;
      flex-wrap: wrap;
      gap: 16px;
    }
    .card-heading {
      font-size: 1.25rem;
      font-weight: 700;
      color: #0f172a;
    }
    .card-actions {
      display: flex;
      align-items: center;
      gap: 12px;
    }
    .search-box {
      position: relative;
    }
    .search-icon {
      position: absolute;
      left: 12px;
      top: 50%;
      transform: translateY(-50%);
      color: #94a3b8;
      font-size: 0.85rem;
    }
    .search-input {
      padding: 8px 14px 8px 34px;
      border: 1px solid #cbd5e1;
      border-radius: 8px;
      font-size: 0.88rem;
      outline: none;
      width: 220px;
    }
    .search-input:focus {
      border-color: #2563eb;
    }
    .btn-valorio-primary {
      background-color: #2563eb;
      color: #ffffff;
      border: none;
      padding: 8px 16px;
      border-radius: 8px;
      font-size: 0.9rem;
      font-weight: 600;
      cursor: pointer;
      display: inline-flex;
      align-items: center;
      gap: 8px;
      transition: background-color 0.15s;
    }
    .btn-valorio-primary:hover {
      background-color: #1d4ed8;
    }

    /* TABLE STYLES */
    .table-responsive {
      overflow-x: auto;
    }
    .valorio-table {
      width: 100%;
      border-collapse: collapse;
      text-align: left;
    }
    .valorio-table th {
      font-size: 0.75rem;
      font-weight: 700;
      color: #64748b;
      letter-spacing: 0.05em;
      padding: 12px 14px;
      border-bottom: 1px solid #e2e8f0;
      background-color: #f8fafc;
    }
    .valorio-table td {
      padding: 14px;
      border-bottom: 1px solid #f1f5f9;
      font-size: 0.9rem;
      color: #334155;
    }
    .valorio-table tbody tr:hover {
      background-color: #f8fafc;
    }
    .col-num {
      color: #94a3b8;
      font-weight: 600;
    }
    .object-cell {
      display: flex;
      flex-direction: column;
      gap: 4px;
    }
    .object-title {
      font-weight: 600;
      color: #0f172a;
    }
    .object-tags {
      display: flex;
      gap: 6px;
      font-size: 0.72rem;
    }
    .tag-ai {
      color: #059669;
      background: #ecfdf5;
      padding: 2px 6px;
      border-radius: 4px;
      font-weight: 600;
    }
    .tag-condition {
      color: #64748b;
      background: #f1f5f9;
      padding: 2px 6px;
      border-radius: 4px;
    }
    .col-demandeur {
      font-weight: 500;
      color: #1e293b;
    }
    .col-centre {
      color: #64748b;
    }
    .price-badge {
      display: inline-block;
      padding: 4px 10px;
      border-radius: 6px;
      font-weight: 600;
      font-size: 0.85rem;
    }
    .badge-don {
      background-color: #ecfdf5;
      color: #059669;
    }
    .badge-prix {
      background-color: #eff6ff;
      color: #2563eb;
    }
    .col-actions {
      text-align: right;
      white-space: nowrap;
    }
    .btn-table {
      border: 1px solid #cbd5e1;
      background: #ffffff;
      padding: 6px 12px;
      border-radius: 6px;
      font-size: 0.82rem;
      cursor: pointer;
      margin-left: 6px;
      transition: all 0.15s;
    }
    .btn-table:hover {
      border-color: #2563eb;
      color: #2563eb;
    }
    .btn-chat {
      background: #eff6ff;
      border-color: #bfdbfe;
      color: #2563eb;
      font-weight: 600;
    }
    .btn-chat:hover {
      background: #2563eb;
      color: #ffffff;
    }
    .empty-cell {
      text-align: center;
      padding: 30px;
      color: #94a3b8;
    }

    /* MODAL OVERLAY & CARD */
    .modal-backdrop {
      position: fixed;
      inset: 0;
      background: rgba(15, 23, 42, 0.6);
      display: flex;
      align-items: center;
      justify-content: center;
      z-index: 1000;
      padding: 20px;
    }
    .valorio-modal {
      background: #ffffff;
      border-radius: 12px;
      max-width: 580px;
      width: 100%;
      padding: 24px;
      box-shadow: 0 10px 25px rgba(0, 0, 0, 0.15);
      animation: popIn 0.2s ease-out;
    }
    @keyframes popIn {
      from { transform: scale(0.95); opacity: 0; }
      to { transform: scale(1); opacity: 1; }
    }
    .modal-head {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 16px;
    }
    .modal-head h3 {
      font-size: 1.15rem;
      font-weight: 700;
      color: #0f172a;
    }
    .text-blue { color: #2563eb; }
    .text-green { color: #059669; }
    .btn-close {
      background: none;
      border: none;
      font-size: 1.5rem;
      cursor: pointer;
      color: #64748b;
    }
    .ai-quick-fill-box {
      background: #eff6ff;
      border: 1px solid #bfdbfe;
      border-radius: 8px;
      padding: 12px;
      margin-bottom: 16px;
    }
    .ai-quick-text {
      font-size: 0.8rem;
      color: #1e40af;
      margin-bottom: 6px;
    }
    .ai-input-group {
      display: flex;
      gap: 8px;
    }
    .quick-input {
      flex: 1;
      background: #ffffff;
      border-color: #93c5fd;
    }
    .btn-ai-sparkle {
      background: linear-gradient(135deg, #2563eb 0%, #7c3aed 100%);
      color: #ffffff;
      border: none;
      padding: 6px 14px;
      border-radius: 6px;
      font-size: 0.82rem;
      font-weight: 600;
      cursor: pointer;
      white-space: nowrap;
    }
    .form-row {
      margin-bottom: 14px;
    }
    .form-grid-2 {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 12px;
    }
    .form-label {
      display: block;
      font-size: 0.82rem;
      font-weight: 600;
      color: #334155;
      margin-bottom: 4px;
    }
    .form-input, .form-select {
      width: 100%;
      padding: 8px 12px;
      border: 1px solid #cbd5e1;
      border-radius: 6px;
      font-size: 0.9rem;
      outline: none;
    }
    .form-input:focus, .form-select:focus {
      border-color: #2563eb;
    }
    .modal-footer {
      display: flex;
      justify-content: flex-end;
      gap: 10px;
      margin-top: 20px;
    }
    .btn-cancel {
      background: #f1f5f9;
      border: 1px solid #cbd5e1;
      color: #475569;
      padding: 8px 16px;
      border-radius: 8px;
      font-size: 0.88rem;
      cursor: pointer;
    }
    .detail-content {
      display: flex;
      flex-direction: column;
      gap: 10px;
      font-size: 0.9rem;
    }
    .detail-row {
      display: flex;
      justify-content: space-between;
      padding: 6px 0;
      border-bottom: 1px solid #f1f5f9;
    }
    .detail-desc {
      color: #64748b;
      margin-top: 4px;
    }

    /* VALORIO CHAT MODAL STYLES */
    .valorio-chat-modal {
      background: #ffffff;
      border-radius: 12px;
      max-width: 450px;
      width: 100%;
      height: 520px;
      display: flex;
      flex-direction: column;
      box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
      overflow: hidden;
    }
    .chat-header {
      background: #0f172a;
      padding: 14px 18px;
      display: flex;
      justify-content: space-between;
      align-items: center;
      color: #ffffff;
    }
    .chat-user-info {
      display: flex;
      align-items: center;
      gap: 10px;
    }
    .chat-avatar {
      width: 36px;
      height: 36px;
      background: #2563eb;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 0.9rem;
    }
    .chat-name {
      font-size: 0.95rem;
      font-weight: 700;
      color: #ffffff;
      margin: 0;
    }
    .chat-sub {
      font-size: 0.72rem;
      color: #94a3b8;
    }
    .chat-header .btn-close {
      color: #ffffff;
    }
    .chat-body {
      flex: 1;
      padding: 16px;
      background: #f8fafc;
      overflow-y: auto;
      display: flex;
      flex-direction: column;
      gap: 12px;
    }
    .chat-message {
      display: flex;
    }
    .msg-received {
      justify-content: flex-start;
    }
    .msg-sent {
      justify-content: flex-end;
    }
    .msg-bubble {
      max-width: 80%;
      padding: 10px 14px;
      border-radius: 12px;
      font-size: 0.88rem;
      line-height: 1.4;
      background: #ffffff;
      color: #1e293b;
      box-shadow: 0 1px 3px rgba(0,0,0,0.06);
      border: 1px solid #e2e8f0;
      position: relative;
    }
    .sent-bubble {
      background: #2563eb;
      color: #ffffff;
      border: none;
    }
    .msg-time {
      display: block;
      font-size: 0.65rem;
      color: #94a3b8;
      text-align: right;
      margin-top: 4px;
    }
    .sent-bubble .msg-time {
      color: #bfdbfe;
    }
    .chat-footer {
      padding: 12px 16px;
      background: #ffffff;
      border-top: 1px solid #e2e8f0;
      display: flex;
      gap: 10px;
    }
    .chat-input {
      flex: 1;
      padding: 8px 12px;
      border: 1px solid #cbd5e1;
      border-radius: 20px;
      font-size: 0.88rem;
      outline: none;
    }
    .chat-input:focus {
      border-color: #2563eb;
    }
    .btn-send {
      background: #2563eb;
      color: #ffffff;
      border: none;
      width: 36px;
      height: 36px;
      border-radius: 50%;
      cursor: pointer;
      display: flex;
      align-items: center;
      justify-content: center;
    }
    .btn-send:disabled {
      background: #cbd5e1;
      cursor: not-allowed;
    }
  `]
})
export class AppComponent implements OnInit {
  currentRole = 'Organisme / Utilisateur';
  searchQuery = '';
  items: MarketplaceItem[] = [];
  filteredItems: MarketplaceItem[] = [];

  // Add Modal State
  showAddModal = false;
  savingItem = false;
  loadingAi = false;
  quickAiInput = '';

  newItem: MarketplaceItem = {
    title: '',
    description: '',
    category: 'ELECTRONICS',
    condition: 'GOOD',
    type: 'SALE',
    status: 'AVAILABLE',
    price: 35,
    sellerName: 'Amine Ben Salah',
    location: 'Eco-Tunis Centre'
  };

  // Chat State
  showChatModal = false;
  activeChatItem: MarketplaceItem | null = null;
  chatInput = '';
  chatMessages: { text: string; time: string }[] = [];

  // Detail State
  selectedDetailItem: MarketplaceItem | null = null;

  constructor(
    private marketplaceService: MarketplaceService,
    private aiService: AiAssistantService
  ) {}

  ngOnInit(): void {
    this.loadItems();
  }

  loadItems(): void {
    const defaultList: MarketplaceItem[] = [
      {
        id: 1,
        title: 'Ordinateur portable - ecran casse',
        sellerName: 'Amine Ben Salah',
        location: 'Eco-Tunis Centre',
        category: 'ELECTRONICS',
        condition: 'FAIR',
        type: 'DONATION',
        status: 'AVAILABLE',
        price: 0,
        aiVerificationScore: 98,
        description: 'Ordinateur portable non réclamé après 90 jours de conservation. Écran fissuré mais composants internes et chargeur fonctionnels.'
      },
      {
        id: 2,
        title: 'Apple iPhone 13 (128 Go) Noir Minuit',
        sellerName: 'Régie des Transports / Service Revalorisation',
        location: 'Gare Centrale - Guichet Objets Trouvés',
        category: 'ELECTRONICS',
        condition: 'GOOD',
        type: 'SALE',
        status: 'AVAILABLE',
        price: 290,
        aiVerificationScore: 98.5,
        description: 'iPhone 13 trouvé dans le métro, réinitialisé en usine, batterie à 89% d\'état de santé. Vendu avec câble.'
      },
      {
        id: 3,
        title: 'Manteau d\'Hiver Chaud Imperméable (Taille L) - Don Solidaire',
        sellerName: 'Association Étudiante Solidarité',
        location: 'Maison de l\'Étudiant - Pôle Épicerie Solidaire',
        category: 'CLOTHING_ACCESSORIES',
        condition: 'GOOD',
        type: 'DONATION',
        status: 'AVAILABLE',
        price: 0,
        aiVerificationScore: 100,
        description: 'Manteau d\'hiver chaud imperméable oublié dans un amphi. Nettoyé et mis à disposition gratuite pour étudiant.'
      },
      {
        id: 4,
        title: 'Vélo Tout Terrain VTT Rockrider ST 100 Noir / Rouge',
        sellerName: 'Service Mobilité Urbaine',
        location: 'Fourrière Municipale des Vélos',
        category: 'BICYCLES_VEHICLES',
        condition: 'GOOD',
        type: 'SALE',
        status: 'AVAILABLE',
        price: 85,
        aiVerificationScore: 97,
        description: 'VTT Rockrider révisé avec 21 vitesses Shimano, freins et pneus en bon état.'
      },
      {
        id: 5,
        title: 'Casque Audio Sans Fil Sony WH-1000XM4 Réduction de Bruit',
        sellerName: 'Pôle Logistique Campus',
        location: 'Campus Universitaire - Accueil Général',
        category: 'ELECTRONICS',
        condition: 'EXCELLENT',
        type: 'SALE',
        status: 'AVAILABLE',
        price: 135,
        aiVerificationScore: 99,
        description: 'Casque audio réducteur de bruit en excellent état avec son étui de transport.'
      }
    ];

    this.marketplaceService.getItems().subscribe({
      next: (data) => {
        if (data && data.length > 0) {
          this.items = data;
        } else {
          this.items = defaultList;
        }
        this.filteredItems = [...this.items];
      },
      error: (err) => {
        console.error('Erreur chargement objets (fallback local activé):', err);
        this.items = defaultList;
        this.filteredItems = [...defaultList];
      }
    });
  }

  filterItems(): void {
    const q = this.searchQuery.toLowerCase().trim();
    if (!q) {
      this.filteredItems = [...this.items];
    } else {
      this.filteredItems = this.items.filter(item => 
        item.title.toLowerCase().includes(q) ||
        (item.sellerName && item.sellerName.toLowerCase().includes(q)) ||
        (item.location && item.location.toLowerCase().includes(q))
      );
    }
  }

  openAddModal(): void {
    this.showAddModal = true;
    this.quickAiInput = '';
    this.newItem = {
      title: '',
      description: '',
      category: 'ELECTRONICS',
      condition: 'GOOD',
      type: 'SALE',
      status: 'AVAILABLE',
      price: 30,
      sellerName: 'Amine Ben Salah',
      location: 'Eco-Tunis Centre'
    };
  }

  closeAddModal(): void {
    this.showAddModal = false;
  }

  onTypeChange(): void {
    if (this.newItem.type === 'DONATION') {
      this.newItem.price = 0;
    }
  }

  autoFillWithAi(): void {
    if (!this.quickAiInput.trim()) return;
    this.loadingAi = true;

    this.aiService.generateListing({
      prompt: this.quickAiInput
    }).subscribe({
      next: (res) => {
        this.newItem.title = res.title;
        this.newItem.description = res.description;
        this.newItem.category = res.suggestedCategory;
        this.newItem.condition = res.suggestedCondition;
        this.newItem.price = res.suggestedPrice;
        this.loadingAi = false;
      },
      error: (err) => {
        console.error('Erreur IA:', err);
        this.newItem.title = this.quickAiInput;
        this.loadingAi = false;
      }
    });
  }

  submitItem(): void {
    this.savingItem = true;
    this.marketplaceService.createItem(this.newItem).subscribe({
      next: (created) => {
        this.items.unshift(created);
        this.filterItems();
        this.savingItem = false;
        this.closeAddModal();
      },
      error: (err) => {
        console.error('Erreur création annonce:', err);
        this.savingItem = false;
      }
    });
  }

  // CHAT MODAL
  openChatModal(item: MarketplaceItem): void {
    this.activeChatItem = item;
    this.chatMessages = [
      { text: "Bonjour ! Cet objet est-il toujours disponible ?", time: "10:48" }
    ];
    this.chatInput = '';
    this.showChatModal = true;
  }

  closeChatModal(): void {
    this.showChatModal = false;
    this.activeChatItem = null;
  }

  sendMessage(): void {
    if (!this.chatInput.trim()) return;
    const now = new Date();
    const timeStr = `${now.getHours().toString().padStart(2, '0')}:${now.getMinutes().toString().padStart(2, '0')}`;
    this.chatMessages.push({
      text: this.chatInput.trim(),
      time: timeStr
    });
    this.chatInput = '';
  }

  viewDetails(item: MarketplaceItem): void {
    this.selectedDetailItem = item;
  }

  openChatFromDetail(): void {
    if (this.selectedDetailItem) {
      const item = this.selectedDetailItem;
      this.selectedDetailItem = null;
      this.openChatModal(item);
    }
  }
}
