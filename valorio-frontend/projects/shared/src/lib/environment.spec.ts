import { VALORIO_ENV } from './environment';

describe('VALORIO_ENV', () => {
  it('pointe vers les préfixes proxy des trois modules', () => {
    expect(VALORIO_ENV.api.reparation).toBe('/rep');
    expect(VALORIO_ENV.api.don).toBe('/don');
    expect(VALORIO_ENV.api.recyclage).toBe('/rec');
  });

  it('démarre avec les APIs statiques (bascule en intégration)', () => {
    expect(VALORIO_ENV.useStaticApis).toBeTrue();
  });
});
