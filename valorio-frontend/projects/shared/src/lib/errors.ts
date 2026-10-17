import { HttpErrorResponse } from '@angular/common/http';

export function messageErreur(erreur: unknown): string {
  if (erreur instanceof HttpErrorResponse) {
    const body = erreur.error as { detail?: string; title?: string } | string | null;
    if (body && typeof body === 'object') {
      return body.detail ?? body.title ?? `Erreur HTTP ${erreur.status}`;
    }
    if (typeof body === 'string' && body.trim().length > 0) {
      return body;
    }
    return `Erreur HTTP ${erreur.status}`;
  }
  if (erreur instanceof Error) {
    return erreur.message;
  }
  return 'Erreur inattendue';
}
