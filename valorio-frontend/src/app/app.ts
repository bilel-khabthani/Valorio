import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { ReparationApiService } from '@valorio/api-reparation';
import { RecyclageApiService } from '@valorio/api-recyclage';
import {
  CentreRecyclage,
  IDENTITE_ADMIN,
  LIBELLES_ROLE,
  Reparateur,
  ROLES_ACTEUR,
  RoleActeur,
  SessionService,
  Utilisateur,
  identiteCentre,
  identiteReparateur,
  identiteUtilisateur,
  labelReparateur,
  labelUtilisateur,
} from '@valorio/shared';

interface OptionIdentite {
  valeur: number;
  label: string;
}

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, RouterLink, RouterLinkActive, FormsModule],
  templateUrl: './app.html',
  styleUrl: './app.css',
})
export class App implements OnInit {
  protected readonly title = signal('Valorio');

  protected readonly session = inject(SessionService);
  private readonly apiReparation = inject(ReparationApiService);
  private readonly apiRecyclage = inject(RecyclageApiService);

  protected readonly roles = ROLES_ACTEUR;
  protected readonly libelleRole = LIBELLES_ROLE;

  private readonly utilisateurs = signal<Utilisateur[]>([]);
  private readonly reparateurs = signal<Reparateur[]>([]);
  private readonly centres = signal<CentreRecyclage[]>([]);

  protected roleChoisi: RoleActeur = this.session.role();
  protected identiteChoisie: number | null = null;

  ngOnInit(): void {
    this.apiReparation.listerUtilisateurs().subscribe({
      next: (utilisateurs) => {
        this.utilisateurs.set(utilisateurs);
        this.assurerIdentiteParDefaut();
      },
    });
    this.apiReparation.rechercherReparateurs().subscribe({
      next: (reparateurs) => {
        this.reparateurs.set(reparateurs);
        this.assurerIdentiteParDefaut();
      },
    });
    this.apiRecyclage.rechercherCentres({}).subscribe({
      next: (centres) => {
        this.centres.set(centres);
        this.assurerIdentiteParDefaut();
      },
    });

    this.roleChoisi = this.session.role();
    this.identiteChoisie =
      this.session.identite().utilisateurId ??
      this.session.identite().reparateurId ??
      this.session.identite().centreId ??
      null;
  }

  optionsIdentite(): OptionIdentite[] {
    switch (this.roleChoisi) {
      case 'REPARATEUR':
        return this.reparateurs().map((r) => ({ valeur: r.id, label: labelReparateur(r) }));
      case 'ORGANISME':
        return this.centres().map((c) => ({ valeur: c.id, label: c.nom }));
      case 'ADMIN':
        return [];
      default:
        return this.utilisateurs().map((u) => ({ valeur: u.id, label: labelUtilisateur(u) }));
    }
  }

  onRoleChange(): void {
    const options = this.optionsIdentite();
    this.identiteChoisie = options.length > 0 ? options[0].valeur : null;
    this.connecter();
  }

  onIdentiteChange(): void {
    this.connecter();
  }

  private assurerIdentiteParDefaut(): void {
    const identite = this.session.identite();
    const dejaDefinie =
      identite.utilisateurId != null || identite.reparateurId != null || identite.centreId != null;
    if (dejaDefinie || identite.role === 'ADMIN') {
      return;
    }
    const premier = this.utilisateurs()[0];
    if (premier) {
      this.roleChoisi = 'UTILISATEUR';
      this.identiteChoisie = premier.id;
      this.connecter();
    }
  }

  private connecter(): void {
    const role = this.roleChoisi;
    if (role === 'ADMIN') {
      this.session.connecter(IDENTITE_ADMIN);
      return;
    }
    if (role === 'REPARATEUR') {
      const reparateur = this.reparateurs().find((r) => r.id === this.identiteChoisie);
      if (reparateur) {
        this.session.connecter(identiteReparateur(reparateur));
      }
      return;
    }
    if (role === 'ORGANISME') {
      const centre = this.centres().find((c) => c.id === this.identiteChoisie);
      if (centre) {
        this.session.connecter(identiteCentre(centre));
      }
      return;
    }
    const utilisateur = this.utilisateurs().find((u) => u.id === this.identiteChoisie);
    if (utilisateur) {
      this.session.connecter(identiteUtilisateur(utilisateur, role));
    }
  }
}
