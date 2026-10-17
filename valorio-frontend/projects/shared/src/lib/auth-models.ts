export type UserRole = 'USER' | 'ADMIN';

export interface User {
  id: number;
  email: string;
  nom: string;
  prenom: string;
  nomComplet: string;
  role: UserRole;
  emailVerifie: boolean;
  actif: boolean;
}

export interface RegisterRequest {
  nom: string;
  prenom: string;
  email: string;
  password: string;
  confirmPassword: string;
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
}
