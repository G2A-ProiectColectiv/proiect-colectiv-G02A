import { useMutation } from "@tanstack/react-query";
import { confirmPasswordReset } from "../api/passwordResetApi";

// Gestioneaza cererea de confirmare a resetarii parolei.
export function useConfirmPasswordReset() {
  return useMutation({
    mutationFn: confirmPasswordReset,
  });
}
