import { httpClient } from "@/shared/api/httpClient";

// Datele necesare pentru solicitarea resetarii parolei.
export interface PasswordResetRequest {
  email: string;
}

// Trimite catre backend cererea pentru resetarea parolei.
export async function requestPasswordReset(
  request: PasswordResetRequest
): Promise<void> {
  await httpClient.post("/auth/password-reset/request", request);
}

// Datele necesare pentru confirmarea resetarii parolei.
export interface PasswordResetConfirmRequest {
  token: string;
  newPassword: string;
}

// Trimite tokenul si parola noua catre backend.
export async function confirmPasswordReset(
  request: PasswordResetConfirmRequest
): Promise<void> {
  await httpClient.post("/auth/password-reset/confirm", request);
}
