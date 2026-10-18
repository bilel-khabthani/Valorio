import { VALORIO_ENV } from './environment';

describe('VALORIO_ENV', () => {
  it('pointe vers les préfixes proxy des modules métier', () => {
    expect(VALORIO_ENV.api.reparation).toBe('/rep');
    expect(VALORIO_ENV.api.don).toBe('/don');
    expect(VALORIO_ENV.api.recyclage).toBe('/rec');
    expect(VALORIO_ENV.api.objets).toBe('/objets');
    expect(VALORIO_ENV.api.users).toBe('/api/users');
  });
});
