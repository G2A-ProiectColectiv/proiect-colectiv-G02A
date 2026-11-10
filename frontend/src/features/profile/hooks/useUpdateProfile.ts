"use client";

import {
  useMutation,
  useQueryClient,
} from "@tanstack/react-query";

import { useAuth } from "@/features/auth/context/AuthContext";
import { updateProfile } from "../api/userApi";

// Actualizeaza profilul, cache-ul si datele din AuthContext.
export function useUpdateProfile() {
  const queryClient = useQueryClient();
  const { updateUser } = useAuth();

  return useMutation({
    mutationFn: updateProfile,

    onSuccess: (updatedProfile) => {
      queryClient.setQueryData(["me"], updatedProfile);
      updateUser(updatedProfile);
    },
  });
}
