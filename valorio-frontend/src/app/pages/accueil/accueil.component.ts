import { Component } from '@angular/core';

/**
 * Point d'entrée racine : le guard `accueilGuard` redirige vers la page
 * d'accueil de l'acteur connecté avant tout rendu.
 */
@Component({
  selector: 'app-accueil',
  standalone: true,
  template: `<p class="chargement">Chargement…</p>`,
  styles: [`.chargement { text-align: center; margin: 3rem; color: #5b6472; }`],
})
export class AccueilComponent {}
