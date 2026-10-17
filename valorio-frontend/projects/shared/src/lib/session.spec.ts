import { TestBed } from '@angular/core/testing';
import { SessionService, identiteUtilisateur } from './session';
import { Utilisateur } from './models';

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

  it('connecte un utilisateur et applique son libellé', () => {
    const utilisateur: Utilisateur = { id: 7, nom: 'Amine', ville: 'Tunis' };
    session.connecter(identiteUtilisateur(utilisateur, 'DONNEUR'));

    expect(session.role()).toBe('DONNEUR');
    expect(session.utilisateurId()).toBe(7);
    expect(session.est('DONNEUR')).toBeTrue();
    expect(session.est('BENEFICIAIRE')).toBeFalse();
  });

  it('active les privilèges administrateur', () => {
    session.connecter({ role: 'ADMIN', label: 'Administrateur' });
    expect(session.estAdmin()).toBeTrue();
    expect(session.utilisateurId()).toBeNull();
  });
});
