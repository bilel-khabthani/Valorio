import { HttpErrorResponse, HttpHandlerFn, HttpInterceptorFn, HttpRequest } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, switchMap, throwError } from 'rxjs';
import { AuthService } from './auth.service';

const URLS_PUBLIQUES = [
  '/api/auth/login',
  '/api/auth/register',
  '/api/auth/refresh',
  '/api/auth/forgot-password',
  '/api/auth/reset-password',
  '/api/auth/verify-email',
  '/api/auth/resend-verification',
];

export const authInterceptor: HttpInterceptorFn = (req: HttpRequest<unknown>, next: HttpHandlerFn) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  let clone = req.clone({
    setHeaders: {
      'X-Requested-With': 'XMLHttpRequest',
    },
    withCredentials: true,
  });

  const token = authService.accessToken();
  const estPublique = URLS_PUBLIQUES.some((url) => req.url.includes(url));

  if (token && !estPublique) {
    clone = clone.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`,
      },
    });
  }

  return next(clone).pipe(
    catchError((error: unknown) => {
      if (error instanceof HttpErrorResponse && error.status === 401 && !estPublique) {
        // Tentative automatique de rafraîchissement silencieux
        return authService.refresh().pipe(
          switchMap((authRes) => {
            const retryReq = req.clone({
              setHeaders: {
                Authorization: `Bearer ${authRes.accessToken}`,
                'X-Requested-With': 'XMLHttpRequest',
              },
              withCredentials: true,
            });
            return next(retryReq);
          }),
          catchError((refreshErr) => {
            authService.logout();
            router.navigate(['/login']);
            return throwError(() => refreshErr);
          })
        );
      }
      return throwError(() => error);
    })
  );
};
