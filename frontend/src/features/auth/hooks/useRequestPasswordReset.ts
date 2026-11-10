import { useMutation } from "@tanstack/react-query";
import { requestPasswordReset } from "../api/passwordResetApi";

// Gestioneaza trimiterea cererii de resetare a parolei.
export function useRequestPasswordReset() {
  return useMutation({
    mutationFn: requestPasswordReset,
  });
}
