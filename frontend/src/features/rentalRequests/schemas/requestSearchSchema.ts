import { z } from "zod";
import { PROPERTY_TYPES } from "@/features/listings/constants/propertyTypes";

/** Opțiunile de sortare; prima e cea implicită (și nu apare în URL). */
export const REQUEST_SORT_OPTIONS = [
  { value: "createdAt,desc", label: "Cele mai noi" },
  { value: "budgetMax,desc", label: "Buget maxim descrescător" },
  { value: "budgetMin,asc", label: "Buget minim crescător" },
  { value: "desiredFrom,asc", label: "Data dorită, cea mai apropiată" },
] as const;

const nonNegativeNumber = z.coerce.number().min(0);

/** Filtrele căutării de cereri, așa cum stau în URL. O valoare invalidă se ignoră. */
export const requestSearchSchema = z.object({
  type: z.enum(PROPERTY_TYPES as [string, ...string[]]).optional(),
  minBudget: nonNegativeNumber.optional(),
  maxBudget: nonNegativeNumber.optional(),
  city: z.string().trim().max(100).optional(),
  area: z.string().trim().max(100).optional(),
  minRooms: z.coerce.number().int().min(0).optional(),
  desiredFrom: z.string().regex(/^\d{4}-\d{2}-\d{2}$/).optional(),
  sort: z.enum(REQUEST_SORT_OPTIONS.map((option) => option.value) as [string, ...string[]]).optional(),
});

export type RequestSearchFilters = Partial<z.infer<typeof requestSearchSchema>>;
