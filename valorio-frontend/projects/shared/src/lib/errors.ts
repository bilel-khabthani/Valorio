import { HttpErrorResponse } from '@angular/common/http';

export function messageErreur(erreur: unknown, repli = 'Erreur inattendue'): string {
  if (erreur instanceof HttpErrorResponse) {
    const body = erreur.error as { detail?: string; title?: string } | string | null;
    if (body && typeof body === 'object') {
      return body.detail ?? body.title ?? (erreur.status ? `Erreur HTTP ${erreur.status}` : repli);
    }
    if (typeof body === 'string' && body.trim().length > 0) {
      return body;
    }
    return erreur.status ? `Erreur HTTP ${erreur.status}` : repli;
  }
  if (erreur instanceof Error) {
    return erreur.message || repli;
  }
  return repli;
}
