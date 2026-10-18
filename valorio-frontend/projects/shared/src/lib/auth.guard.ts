import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from './auth.service';
import { UserRole } from './auth-models';
import { RoleActeur, routeAccueil } from './session';

export const authGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (authService.isAuthenticated()) {
    return true;
  }

  // Mémorise la destination pour redirection post-connexion
  return router.createUrlTree(['/login'], {
    queryParams: { returnUrl: state.url },
  });
};

/** Redirige vers l'espace de l'acteur connecté (page d'accueil dynamique). */
export const accueilGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router = inject(Router);
  return router.parseUrl(routeAccueil(authService.acteur()));
};

/** Restreint l'accès à une route aux acteurs listés (un ADMIN passe toujours). */
export function acteurGuard(acteurs: RoleActeur[]): CanActivateFn {
  return (route, state) => {
    const authService = inject(AuthService);
    const router = inject(Router);

    if (!authService.isAuthenticated()) {
      return router.createUrlTree(['/login'], {
        queryParams: { returnUrl: state.url },
      });
    }

    const acteur = authService.acteur();
    if (acteur && (acteurs.includes(acteur) || acteur === 'ADMIN')) {
      return true;
    }

    return router.parseUrl(routeAccueil(acteur));
  };
}

export function roleGuard(requiredRole: UserRole): CanActivateFn {
  return (route, state) => {
    const authService = inject(AuthService);
    const router = inject(Router);

    if (!authService.isAuthenticated()) {
      return router.createUrlTree(['/login'], {
        queryParams: { returnUrl: state.url },
      });
    }

    if (authService.currentUser()?.role === requiredRole || authService.isAdmin()) {
      return true;
    }

    return router.createUrlTree(['/']);
  };
}
