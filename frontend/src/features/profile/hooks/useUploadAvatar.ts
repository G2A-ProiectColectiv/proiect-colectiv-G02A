
"use client";

import { useMutation, useQueryClient } from "@tanstack/react-query";
import { useAuth } from "@/features/auth/context/AuthContext";
import {
  getMe,
  uploadAvatar,
  removeAvatar,
} from "../api/userApi";

// Gestioneaza avatarul si sincronizeaza datele utilizatorului.
export function useUploadAvatar() {
  const queryClient = useQueryClient();
  const { updateUser } = useAuth();

  const upload = useMutation({
    mutationFn: uploadAvatar,

    onSuccess: (updatedProfile) => {
      queryClient.setQueryData(["me"], updatedProfile);
      updateUser(updatedProfile);
    },
  });

  const remove = useMutation({
    mutationFn: removeAvatar,

    onSuccess: async () => {
      // Obtine profilul actualizat dupa eliminarea avatarului.
      const updatedProfile = await getMe();

      queryClient.setQueryData(["me"], updatedProfile);
      updateUser(updatedProfile);
    },
  });

  return {
    upload,
    remove,
  };
}
