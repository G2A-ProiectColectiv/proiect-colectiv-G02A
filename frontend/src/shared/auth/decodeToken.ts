export type Role = "TENANT" | "LANDLORD" | "ADMIN";

const ROLES: readonly Role[] = ["TENANT", "LANDLORD", "ADMIN"];

export interface DecodedToken {
  userId: number;
  email: string;
  role: Role;
  /** Momentul expirării, în milisecunde de la epoch. */
  expiresAt: number;
}

function base64UrlDecode(value: string): string {
  const base64 = value.replace(/-/g, "+").replace(/_/g, "/");
  const padded = base64.padEnd(Math.ceil(base64.length / 4) * 4, "=");
  const binary = atob(padded);
  const bytes = Uint8Array.from(binary, (char) => char.charCodeAt(0));
  return new TextDecoder().decode(bytes);
}

/**
 * Citește rolul și expirarea din payload-ul unui JWT, FĂRĂ să verifice semnătura.
 * Rezultatul servește doar la interfață (redirecționări, meniuri); securitatea
 * reală este în backend. Întoarce `null` dacă token-ul nu are forma așteptată.
 */
export function decodeToken(token: string): DecodedToken | null {
  const parts = token.split(".");
  if (parts.length !== 3) return null;

  try {
    const payload: unknown = JSON.parse(base64UrlDecode(parts[1]));
    if (typeof payload !== "object" || payload === null) return null;

    const { sub, email, role, exp } = payload as Record<string, unknown>;
    const userId = Number(sub);

    if (!Number.isFinite(userId)) return null;
    if (typeof email !== "string") return null;
    if (typeof role !== "string" || !ROLES.includes(role as Role)) return null;
    if (typeof exp !== "number") return null;

    return { userId, email, role: role as Role, expiresAt: exp * 1000 };
  } catch {
    return null;
  }
}

export function isTokenExpired(token: DecodedToken, now: number = Date.now()): boolean {
  return token.expiresAt <= now;
}
