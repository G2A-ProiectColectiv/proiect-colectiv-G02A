import { z } from "zod";

/** Aceleași reguli ca în backend: `@Email`, `@NotBlank`, parola de minimum 8 caractere. */
export const PASSWORD_MIN_LENGTH = 8;

const emailField = z
  .string()
  .trim()
  .min(1, "Introdu adresa de email.")
  .pipe(z.email("Adresa de email nu este validă."));

export const loginSchema = z.object({
  email: emailField,
  password: z.string().min(1, "Introdu parola."),
});

export const registerSchema = z.object({
  firstName: z.string().trim().min(1, "Introdu prenumele."),
  lastName: z.string().trim().min(1, "Introdu numele."),
  email: emailField,
  password: z
    .string()
    .min(PASSWORD_MIN_LENGTH, `Parola trebuie să aibă cel puțin ${PASSWORD_MIN_LENGTH} caractere.`),
  role: z.enum(["TENANT", "LANDLORD"], { error: "Alege dacă ești chiriaș sau proprietar." }),
});

export type LoginFormValues = z.infer<typeof loginSchema>;
export type RegisterFormValues = z.infer<typeof registerSchema>;
