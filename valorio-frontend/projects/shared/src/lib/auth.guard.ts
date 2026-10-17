import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from './auth.service';
import { UserRole } from './auth-models';

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
