import { TestBed } from '@angular/core/testing';
import { SessionService } from './session';

describe('SessionService', () => {
  let session: SessionService;

  beforeEach(() => {
    localStorage.removeItem('valorio.session');
    TestBed.configureTestingModule({});
    session = TestBed.inject(SessionService);
  });

  afterEach(() => localStorage.removeItem('valorio.session'));

  it('démarre sur la session utilisateur par défaut', () => {
    expect(session.role()).toBe('UTILISATEUR');
    expect(session.estAdmin()).toBeFalse();
  });

  it('connecte un utilisateur et applique son rôle', () => {
    session.connecter({ role: 'UTILISATEUR', label: 'Amine', utilisateurId: 7 });

    expect(session.role()).toBe('UTILISATEUR');
    expect(session.utilisateurId()).toBe(7);
    expect(session.est('UTILISATEUR')).toBeTrue();
    expect(session.est('ACHETEUR')).toBeFalse();
  });

  it('distingue les acteurs Acheteur et Réparateur', () => {
    session.connecter({ role: 'ACHETEUR', label: 'Acheteur', utilisateurId: 9 });
    expect(session.est('ACHETEUR')).toBeTrue();
    expect(session.est('REPARATEUR', 'ORGANISME')).toBeFalse();
  });

  it('active les privilèges administrateur', () => {
    session.connecter({ role: 'ADMIN', label: 'Administrateur' });
    expect(session.estAdmin()).toBeTrue();
    expect(session.utilisateurId()).toBeNull();
  });
});
