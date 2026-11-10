import { httpClient } from "@/shared/api/httpClient";

import type {
  Profile,
  UpdateProfileRequest,
  DeleteAccountRequest,
} from "../types";

export async function getMe(): Promise<Profile> {
  const response = await httpClient.get<Profile>("/users/me");
  return response.data;
}

export async function updateProfile(request: UpdateProfileRequest): Promise<Profile> {
  const response = await httpClient.put<Profile>(
    "/users/me",
    request
  );

  return response.data;
}

export async function uploadAvatar(file: File): Promise<Profile> {
  const formData = new FormData();
  formData.append("file", file);

  const response = await httpClient.post<Profile>(
    "/users/me/avatar",
    formData
  );

  return response.data;
}

export async function removeAvatar(): Promise<void> {
  await httpClient.delete("/users/me/avatar");
}

export async function deleteAccount(request: DeleteAccountRequest): Promise<void> {
  await httpClient.delete("/users/me", {
    data: request,
  });
}
