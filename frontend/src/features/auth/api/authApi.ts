import { httpClient } from "@/shared/api/httpClient";
import type { AuthResponse, LoginRequest, RegisterRequest, User } from "../types";

export async function register(request: RegisterRequest): Promise<User> {
  const { data } = await httpClient.post<User>("/auth/register", request);
  return data;
}

export async function login(request: LoginRequest): Promise<AuthResponse> {
  const { data } = await httpClient.post<AuthResponse>("/auth/login", request);
  return data;
}
