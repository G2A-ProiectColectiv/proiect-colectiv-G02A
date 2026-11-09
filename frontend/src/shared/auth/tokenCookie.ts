import { decodeToken } from "./decodeToken";

/** Numele cookie-ului; îl citește și `proxy.ts`, care rulează pe server. */
export const TOKEN_COOKIE_NAME = "access_token";
const COOKIE_NAME = TOKEN_COOKIE_NAME;

/** Citește token-ul din cookie. Întoarce `null` pe server sau dacă nu există. */
export function getToken(): string | null {
  if (typeof document === "undefined") return null;

  const entry = document.cookie.split("; ").find((cookie) => cookie.startsWith(`${COOKIE_NAME}=`));
  if (!entry) return null;

  try {
    return decodeURIComponent(entry.slice(COOKIE_NAME.length + 1));
  } catch {
    return null;
  }
}

/**
 * Salvează token-ul în cookie, cu `SameSite=Lax`, `Secure` în producție și
 * aceeași expirare ca token-ul. Cookie-ul nu este `HttpOnly`, pentru că îl
 * setează frontendul și îl citește și `proxy.ts`.
 */
export function setToken(token: string): void {
  if (typeof document === "undefined") return;

  const attributes = [`${COOKIE_NAME}=${encodeURIComponent(token)}`, "Path=/", "SameSite=Lax"];

  const decoded = decodeToken(token);
  if (decoded) attributes.push(`Expires=${new Date(decoded.expiresAt).toUTCString()}`);
  if (process.env.NODE_ENV === "production") attributes.push("Secure");

  document.cookie = attributes.join("; ");
}

export function clearToken(): void {
  if (typeof document === "undefined") return;
  document.cookie = `${COOKIE_NAME}=; Path=/; Max-Age=0; SameSite=Lax`;
}
