"use client";

import { useMutation } from "@tanstack/react-query";
import { useAuth } from "@/features/auth/context/AuthContext";
import { deleteAccount } from "../api/userApi";

// Sterge contul si deconecteaza utilizatorul dupa confirmarea backendului.
export function useDeleteAccount() {
  const { logout } = useAuth();

  return useMutation({
    mutationFn: deleteAccount,

    onSuccess: () => {
      logout("/?accountDeleted=1");
    },
  });
}
