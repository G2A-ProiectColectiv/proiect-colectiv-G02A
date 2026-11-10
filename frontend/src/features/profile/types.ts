import type { User } from "@/features/auth/types";

export type Profile = User;

export interface UpdateProfileRequest {
  firstName: string;
  lastName: string;
  phone: string | null;
  bio: string | null;
}

export interface DeleteAccountRequest {
  password: string;
}
