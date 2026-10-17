import {
  libelleStatut,
  prochainStatutCollecte,
  prochainStatutDemande,
  prochainStatutDemandeDon,
} from './models';

describe('helpers de statut', () => {
  it('met en forme un libellé de statut', () => {
    expect(libelleStatut('EN_REPARATION')).toBe('En Reparation');
    expect(libelleStatut('DEMANDE')).toBe('Demande');
  });

  it('donne le prochain statut du workflow de réparation', () => {
    expect(prochainStatutDemande('DEMANDE')).toBe('ACCEPTEE');
    expect(prochainStatutDemande('LIVREE')).toBeNull();
  });

  it('donne le prochain statut du workflow de don', () => {
    expect(prochainStatutDemandeDon('DEMANDE')).toBe('ACCEPTEE');
    expect(prochainStatutDemandeDon('CONFIRMEE')).toBeNull();
  });

  it('donne le prochain statut du workflow de collecte', () => {
    expect(prochainStatutCollecte('DEMANDE')).toBe('PLANIFIEE');
    expect(prochainStatutCollecte('RECUE')).toBeNull();
  });
});
