
import { z } from "zod";

export const profileSchema = z.object({
  firstName: z
    .string()
    .trim()
    .min(1, "Introdu prenumele."),

  lastName: z
    .string()
    .trim()
    .min(1, "Introdu numele."),

  phone: z
    .string()
    .trim()
    .nullable()
    .refine(
      (value) =>
        !value || /^(07\d{8}|\+407\d{8})$/.test(value),
      {
        message:
          "Introdu un număr valid (07XXXXXXXX sau +407XXXXXXXX).",
      }
    ),


  bio: z
    .string()
    .trim()
    .max(1000, "Descrierea poate avea maximum 1000 de caractere.")
    .nullable(),
});

export type ProfileFormValues = z.infer<typeof profileSchema>;
