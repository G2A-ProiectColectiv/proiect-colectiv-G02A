"use client";

import {
  createContext,
  useCallback,
  useContext,
  useMemo,
  useSyncExternalStore,
  type ReactNode,
} from "react";
import { useRouter } from "next/navigation";
import { useQueryClient } from "@tanstack/react-query";
import { decodeToken, isTokenExpired } from "@/shared/auth/decodeToken";
import { clearToken, getToken, setToken } from "@/shared/auth/tokenCookie";
import type { AuthResponse, User } from "../types";

const USER_STORAGE_KEY = "auth_user";
const ROLES: readonly string[] = ["TENANT", "LANDLORD", "ADMIN"];

/*
 * Sesiunea trăiește în două locuri: token-ul în cookie (îl citește și `proxy.ts`)
 * și utilizatorul (numele, pentru Navbar) în localStorage. Backendul nu are încă
 * `GET /api/users/me`, deci după un refresh numele vine din ce am salvat la login.
 * Este un mic store extern, citit cu `useSyncExternalStore`.
 */

const listeners = new Set<() => void>();

function notify() {
  listeners.forEach((listener) => listener());
}

function subscribe(listener: () => void) {
  listeners.add(listener);
  // Modificările făcute din alt tab.
  window.addEventListener("storage", listener);
  return () => {
    listeners.delete(listener);
    window.removeEventListener("storage", listener);
  };
}

function readStoredUser(): string | null {
  try {
    return localStorage.getItem(USER_STORAGE_KEY);
  } catch {
    return null;
  }
}

function parseStoredUser(raw: string | null): User | null {
  if (!raw) return null;
  try {
    const value: unknown = JSON.parse(raw);
    if (typeof value !== "object" || value === null) return null;
    const candidate = value as Record<string, unknown>;
    const isValid =
      typeof candidate.id === "number" &&
      typeof candidate.email === "string" &&
      typeof candidate.firstName === "string" &&
      typeof candidate.lastName === "string" &&
      typeof candidate.role === "string" &&
      ROLES.includes(candidate.role);
    return isValid ? (value as User) : null;
  } catch {
    return null;
  }
}

function resolveUser(token: string | null, rawUser: string | null): User | null {
  if (!token) return null;

  const decoded = decodeToken(token);
  if (!decoded || isTokenExpired(decoded)) return null;

  const stored = parseStoredUser(rawUser);
  if (stored && stored.id === decoded.userId) return stored;

  // Token valid, dar fără utilizator salvat (de ex. localStorage golit): folosim ce știm din token.
  return {
    id: decoded.userId,
    email: decoded.email,
    firstName: decoded.email.split("@")[0],
    lastName: "",
    phone: null,
    avatarUrl: null,
    bio: null,
    role: decoded.role,
  };
}

let snapshotCache: { key: string; user: User | null } | null = null;

/** Întoarce același obiect cât timp cookie-ul și utilizatorul salvat nu se schimbă. */
function getSnapshot(): User | null {
  const token = getToken();
  const rawUser = readStoredUser();
  const key = `${token ?? ""}\n${rawUser ?? ""}`;

  if (snapshotCache?.key !== key) {
    snapshotCache = { key, user: resolveUser(token, rawUser) };
  }
  return snapshotCache.user;
}

function getServerSnapshot(): User | null {
  return null;
}

const subscribeNever = () => () => {};

export interface AuthContextValue {
  /** Utilizatorul autentificat, sau `null` dacă nu e nimeni conectat. */
  user: User | null;
  /** `false` până când aplicația a citit sesiunea în browser (evită un „flash" de utilizator deconectat). */
  isReady: boolean;
  /** Salvează sesiunea primită de la `POST /api/auth/login`. */
  signIn: (auth: AuthResponse) => void;
  /** Sterge sesiunea si cache-ul, apoi redirectioneaza utilizatorul catre pagina specificata sau /login. */
  logout: (redirectTo?: string) => void;
  /** Actualizează datele utilizatorului autentificat în aplicație. */
  updateUser: (updatedUser: User) => void;
}

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const router = useRouter();
  const queryClient = useQueryClient();

  const user = useSyncExternalStore(subscribe, getSnapshot, getServerSnapshot);
  const isReady = useSyncExternalStore(
    subscribeNever,
    () => true,
    () => false,
  );

  const signIn = useCallback((auth: AuthResponse) => {
    setToken(auth.token);
    try {
      localStorage.setItem(USER_STORAGE_KEY, JSON.stringify(auth.user));
    } catch {
      // Fără localStorage, numele nu se păstrează după refresh, dar sesiunea funcționează.
    }
    notify();
  }, []);

  // Actualizeaza datele utilizatorului si notifica componentele.
  const updateUser = useCallback((updatedUser: User) => {
    try {
      localStorage.setItem(
        USER_STORAGE_KEY,
        JSON.stringify(updatedUser)
      );
    } catch {
      // Datele nu au putut fi salvate local.
    }

    notify();
  }, []);

  const logout = useCallback((redirectTo = "/login") => {
    clearToken();
    try {
      localStorage.removeItem(USER_STORAGE_KEY);
    } catch {
      // Nimic de șters.
    }
    notify();
    queryClient.clear();
    router.replace(redirectTo);
  }, [queryClient, router]);

  const value = useMemo(() => ({ user, isReady, signIn, logout, updateUser }), [user, isReady, signIn, logout, updateUser ]);

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth(): AuthContextValue {
  const context = useContext(AuthContext);
  if (!context) throw new Error("useAuth trebuie folosit în interiorul AuthProvider.");
  return context;
}
