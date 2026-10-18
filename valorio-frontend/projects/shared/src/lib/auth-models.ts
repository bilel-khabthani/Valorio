export type UserRole = 'USER' | 'ADMIN';

/** Les 5 acteurs métier du CDC Valorio. */
export type Acteur = 'UTILISATEUR' | 'REPARATEUR' | 'ACHETEUR' | 'ORGANISME' | 'ADMIN';

export const ACTEURS: readonly Acteur[] = [
  'UTILISATEUR',
  'REPARATEUR',
  'ACHETEUR',
  'ORGANISME',
  'ADMIN',
];

export const LIBELLES_ACTEUR: Record<Acteur, string> = {
  UTILISATEUR: 'Utilisateur',
  REPARATEUR: 'Réparateur',
  ACHETEUR: 'Acheteur',
  ORGANISME: 'Organisme de recyclage',
  ADMIN: 'Administrateur',
};

export function acteurDepuisRole(role: UserRole | undefined | null): Acteur {
  return role === 'ADMIN' ? 'ADMIN' : 'UTILISATEUR';
}

export interface User {
  id: number;
  email: string;
  nom: string;
  prenom: string;
  nomComplet: string;
  role: UserRole;
  acteur?: Acteur;
  emailVerifie: boolean;
  actif: boolean;
}

export interface RegisterRequest {
  nom: string;
  prenom: string;
  email: string;
  password: string;
  confirmPassword: string;
  acteur?: Acteur;
  termsAccepted: boolean;
}

export interface LoginRequest {
  email: string;
  password: string;
  rememberMe?: boolean;
}

export interface AuthResponse {
  accessToken: string;
  tokenType: string;
  expiresIn: number;
  user: User;
}

export interface ForgotPasswordRequest {
  email: string;
}

export interface ResetPasswordRequest {
  token: string;
  newPassword: string;
  confirmPassword: string;
}

export interface VerifyEmailRequest {
  token: string;
}

export interface ResendVerificationRequest {
  email: string;
}

export interface MessageResponse {
  message: string;
  success: boolean;
  devLink?: string;
}
