export interface ValorioApiConfig {
  readonly reparation: string;
  readonly don: string;
  readonly recyclage: string;
}

export interface ValorioEnvironment {
  readonly production: boolean;
  readonly useStaticApis: boolean;
  readonly api: ValorioApiConfig;
}

export const VALORIO_ENV: ValorioEnvironment = {
  production: false,
  useStaticApis: true,
  api: {
    reparation: '/rep',
    don: '/don',
    recyclage: '/rec',
  },
};
