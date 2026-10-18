export interface ValorioApiConfig {
  readonly reparation: string;
  readonly don: string;
  readonly recyclage: string;
  readonly objets: string;
  readonly marketplace: string;
  readonly users: string;
}

export interface ValorioEnvironment {
  readonly production: boolean;
  readonly api: ValorioApiConfig;
}

export const VALORIO_ENV: ValorioEnvironment = {
  production: false,
  api: {
    reparation: '/rep',
    don: '/don',
    recyclage: '/rec',
    objets: '/objets',
    marketplace: '/mkt',
    users: '/api/users',
  },
};
