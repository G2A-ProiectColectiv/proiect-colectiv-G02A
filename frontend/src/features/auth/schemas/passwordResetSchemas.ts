import { z } from "zod";

// Valideaza emailul introdus pentru recuperarea parolei.
export const forgotPasswordSchema = z.object({
  email: z.string().trim().email("Introdu o adresă de email validă."),
});

// Tipul datelor din formular, obtinut din schema Zod.
export type ForgotPasswordFormValues = z.infer<
  typeof forgotPasswordSchema
>;

import { PASSWORD_MIN_LENGTH } from "./authSchemas";

// Valideaza parola noua si confirmarea acesteia.
export const resetPasswordSchema = z
  .object({
    newPassword: z
      .string()
      .min(
        PASSWORD_MIN_LENGTH,
        `Parola trebuie să aibă cel puțin ${PASSWORD_MIN_LENGTH} caractere.`
      ),
    confirmPassword: z.string().min(1, "Confirmă parola nouă."),
  })
  .refine((values) => values.newPassword === values.confirmPassword, {
    message: "Parolele nu coincid.",
    path: ["confirmPassword"],
  });

// Tipul datelor introduse in formularul de resetare.
export type ResetPasswordFormValues = z.infer<typeof resetPasswordSchema>;

