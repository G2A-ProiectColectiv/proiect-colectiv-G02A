import { z } from "zod";
import { prefillMapsLink } from "@/features/location/utils/prefillMapsLink";
import { PROPERTY_TYPES } from "@/features/listings/constants/propertyTypes";
import type { PropertyType } from "@/features/listings/types";
import { RADIUS_KM, type RentalRequest, type RentalRequestPayload } from "../types";

/** Limitele coloanelor din baza de date (`rental_requests`). */
export const REQUEST_LIMITS = {
  title: 150,
  city: 100,
  area: 100,
  googleMapsUrl: 2000,
} as const;

/** Acceptă și virgula ca separator zecimal („450,50"). */
function parseNumber(value: string): number {
  return Number(value.trim().replace(",", "."));
}

/** Data de azi, ca `YYYY-MM-DD` în ora locală (pentru comparația cu `<input type="date">`). */
export function todayIso(): string {
  const now = new Date();
  const month = String(now.getMonth() + 1).padStart(2, "0");
  const day = String(now.getDate()).padStart(2, "0");
  return `${now.getFullYear()}-${month}-${day}`;
}

/*
 * Aceleași reguli ca CER-02…CER-04 și ca validările din backend. Câmpurile numerice sunt text
 * în formular (așa vin din `<input>`) și se convertesc în `toRequestPayload`.
 */
export const requestFormSchema = z
  .object({
    title: z
      .string()
      .trim()
      .min(1, "Introdu titlul.")
      .max(
        REQUEST_LIMITS.title,
        `Titlul poate avea cel mult ${REQUEST_LIMITS.title} de caractere.`,
      ),
    description: z.string().trim().min(1, "Scrie ce cauți."),
    desiredType: z.enum(PROPERTY_TYPES as [PropertyType, ...PropertyType[]], {
      error: "Alege tipul de locuință.",
    }),
    rooms: z
      .string()
      .trim()
      .refine((value) => {
        if (!value) return true;
        const number = parseNumber(value);
        return Number.isInteger(number) && number >= 1;
      }, "Numărul de camere trebuie să fie un număr întreg, cel puțin 1."),
    budgetMin: z
      .string()
      .trim()
      .min(1, "Introdu bugetul minim.")
      .refine((value) => {
        const number = parseNumber(value);
        return Number.isFinite(number) && number >= 0;
      }, "Bugetul minim trebuie să fie 0 sau mai mare."),
    budgetMax: z
      .string()
      .trim()
      .min(1, "Introdu bugetul maxim.")
      .refine((value) => {
        const number = parseNumber(value);
        return Number.isFinite(number) && number > 0;
      }, "Bugetul maxim trebuie să fie mai mare decât 0."),
    currency: z.enum(["RON", "EUR"], { error: "Alege moneda." }),
    desiredFrom: z
      .string()
      .min(1, "Alege data de la care cauți.")
      .regex(/^\d{4}-\d{2}-\d{2}$/, "Data nu este validă.")
      // Datele ISO se compară corect ca text.
      .refine((value) => value >= todayIso(), "Data nu poate fi în trecut."),
    city: z
      .string()
      .trim()
      .min(1, "Introdu orașul.")
      .max(REQUEST_LIMITS.city, `Orașul poate avea cel mult ${REQUEST_LIMITS.city} de caractere.`),
    area: z
      .string()
      .trim()
      .max(REQUEST_LIMITS.area, `Zona poate avea cel mult ${REQUEST_LIMITS.area} de caractere.`),
    googleMapsUrl: z
      .string()
      .trim()
      .max(
        REQUEST_LIMITS.googleMapsUrl,
        `Linkul poate avea cel mult ${REQUEST_LIMITS.googleMapsUrl} de caractere.`,
      ),
    radiusKm: z
      .number({ error: "Alege raza de căutare." })
      .int("Raza trebuie să fie un număr întreg de km.")
      .min(RADIUS_KM.min, `Raza trebuie să fie între ${RADIUS_KM.min} și ${RADIUS_KM.max} km.`)
      .max(RADIUS_KM.max, `Raza trebuie să fie între ${RADIUS_KM.min} și ${RADIUS_KM.max} km.`),
  })
  .superRefine((values, context) => {
    // CER-04: eroarea se afișează la bugetul maxim, ca în backend.
    const min = parseNumber(values.budgetMin);
    const max = parseNumber(values.budgetMax);
    if (Number.isFinite(min) && Number.isFinite(max) && values.budgetMax && min > max) {
      context.addIssue({
        code: "custom",
        path: ["budgetMax"],
        message: "Bugetul maxim trebuie să fie cel puțin egal cu cel minim.",
      });
    }
  });

export type RequestFormValues = z.infer<typeof requestFormSchema>;

export const EMPTY_REQUEST_FORM: RequestFormValues = {
  title: "",
  description: "",
  desiredType: "APARTMENT",
  rooms: "",
  budgetMin: "",
  budgetMax: "",
  currency: "EUR",
  desiredFrom: "",
  city: "",
  area: "",
  googleMapsUrl: "",
  radiusKm: RADIUS_KM.default,
};

/** Valorile formularului → corpul cererii către backend. Raza pleacă doar împreună cu linkul. */
export function toRequestPayload(values: RequestFormValues): RentalRequestPayload {
  const googleMapsUrl = values.googleMapsUrl.trim();
  const area = values.area.trim();
  const rooms = values.rooms.trim();

  return {
    title: values.title.trim(),
    description: values.description.trim(),
    desiredType: values.desiredType,
    budgetMin: parseNumber(values.budgetMin),
    budgetMax: parseNumber(values.budgetMax),
    currency: values.currency,
    city: values.city.trim(),
    area: area || null,
    rooms: rooms ? parseNumber(rooms) : null,
    desiredFrom: values.desiredFrom,
    googleMapsUrl: googleMapsUrl || null,
    radiusKm: googleMapsUrl ? values.radiusKm : null,
  };
}

/** Cererea salvată → valorile inițiale ale formularului de editare. */
export function toFormValues(request: RentalRequest): RequestFormValues {
  return {
    title: request.title,
    description: request.description,
    desiredType: request.desiredType,
    rooms: request.rooms !== null ? String(request.rooms) : "",
    budgetMin: String(request.budgetMin),
    budgetMax: String(request.budgetMax),
    currency: request.currency,
    desiredFrom: request.desiredFrom,
    city: request.city,
    area: request.area ?? "",
    googleMapsUrl: prefillMapsLink(
      request.googleMapsUrl,
      request.centerLatitude,
      request.centerLongitude,
    ),
    radiusKm: request.radiusKm ?? RADIUS_KM.default,
  };
}
