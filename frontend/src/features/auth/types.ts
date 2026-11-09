import type { Role } from "@/shared/auth/decodeToken";

export type { Role };

/** Rolurile care pot fi alese la înregistrare (ADMIN este refuzat de backend). */
export type SelfRegisterRole = Exclude<Role, "ADMIN">;

/** `UserDto` din backend. Parola nu apare în niciun răspuns. */
export interface User {
  id: number;
  email: string;
  firstName: string;
  lastName: string;
  phone: string | null;
  avatarUrl: string | null;
  bio: string | null;
  role: Role;
}

export interface RegisterRequest {
  email: string;
  password: string;
  firstName: string;
  lastName: string;
  role: SelfRegisterRole;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface AuthResponse {
  token: string;
  user: User;
}
