import { z } from "zod";
import { AMENITIES } from "../constants/amenities";
import { PROPERTY_TYPES } from "../constants/propertyTypes";
import type { Amenity } from "../types";

/** Opțiunile de sortare; prima e cea implicită (și nu apare în URL). */
export const LISTING_SORT_OPTIONS = [
  { value: "createdAt,desc", label: "Cele mai noi" },
  { value: "price,asc", label: "Preț crescător" },
  { value: "price,desc", label: "Preț descrescător" },
] as const;

const nonNegativeNumber = z.coerce.number().min(0);
const nonNegativeInt = z.coerce.number().int().min(0);

/**
 * Filtrele căutării, așa cum stau în URL. O valoare invalidă se ignoră (vezi `parseFilters`).
 * Dotările se scriu separate prin virgulă, iar cele necunoscute se elimină.
 */
export const listingSearchSchema = z.object({
  minPrice: nonNegativeNumber.optional(),
  maxPrice: nonNegativeNumber.optional(),
  type: z.enum(PROPERTY_TYPES as [string, ...string[]]).optional(),
  minRooms: nonNegativeInt.optional(),
  minBathrooms: nonNegativeInt.optional(),
  minArea: nonNegativeNumber.optional(),
  maxArea: nonNegativeNumber.optional(),
  city: z.string().trim().max(100).optional(),
  amenities: z
    .string()
    .transform((value) =>
      value.split(",").filter((item): item is Amenity => AMENITIES.includes(item as Amenity)),
    )
    .optional(),
  availableBy: z.string().regex(/^\d{4}-\d{2}-\d{2}$/).optional(),
  view: z.enum(["list", "map"]).optional(),
  sort: z.enum(LISTING_SORT_OPTIONS.map((option) => option.value) as [string, ...string[]]).optional(),
});

export type ListingSearchFilters = Partial<z.infer<typeof listingSearchSchema>>;
