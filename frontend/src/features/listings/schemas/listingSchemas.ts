import { z } from "zod";
import { prefillMapsLink } from "@/features/location/utils/prefillMapsLink";
import { AMENITIES } from "../constants/amenities";
import { PROPERTY_TYPES } from "../constants/propertyTypes";
import type { Amenity, Listing, ListingRequest, PropertyType } from "../types";

/** Limitele coloanelor din baza de date (`listings`). */
export const LISTING_LIMITS = {
  title: 150,
  address: 255,
  city: 100,
  googleMapsUrl: 2000,
} as const;

/** Acceptă și virgula ca separator zecimal („450,50"). */
function parseNumber(value: string): number {
  return Number(value.trim().replace(",", "."));
}

/** Număr zecimal strict pozitiv (preț, suprafață). */
function positiveNumber(requiredMessage: string, invalidMessage: string) {
  return z
    .string()
    .trim()
    .min(1, requiredMessage)
    .refine((value) => {
      const number = parseNumber(value);
      return Number.isFinite(number) && number > 0;
    }, invalidMessage);
}

/** Număr întreg mai mare sau egal cu 0 (camere, băi). */
function nonNegativeInteger(requiredMessage: string, invalidMessage: string) {
  return z
    .string()
    .trim()
    .min(1, requiredMessage)
    .refine((value) => {
      const number = parseNumber(value);
      return Number.isInteger(number) && number >= 0;
    }, invalidMessage);
}

/*
 * Aceleași reguli ca ANUN-04 și ca validările din backend. Câmpurile numerice sunt text
 * în formular (așa vin din `<input>`) și se convertesc în `toListingRequest`.
 */
export const listingFormSchema = z.object({
  title: z
    .string()
    .trim()
    .min(1, "Introdu titlul.")
    .max(LISTING_LIMITS.title, `Titlul poate avea cel mult ${LISTING_LIMITS.title} de caractere.`),
  description: z.string().trim().min(1, "Scrie o descriere."),
  type: z.enum(PROPERTY_TYPES as [PropertyType, ...PropertyType[]], {
    error: "Alege tipul locuinței.",
  }),
  price: positiveNumber("Introdu prețul.", "Prețul trebuie să fie mai mare decât 0."),
  currency: z.enum(["RON", "EUR"], { error: "Alege moneda." }),
  rooms: nonNegativeInteger(
    "Introdu numărul de camere.",
    "Numărul de camere trebuie să fie un număr întreg, 0 sau mai mare.",
  ),
  bathrooms: nonNegativeInteger(
    "Introdu numărul de băi.",
    "Numărul de băi trebuie să fie un număr întreg, 0 sau mai mare.",
  ),
  areaSqm: positiveNumber("Introdu suprafața.", "Suprafața trebuie să fie mai mare decât 0."),
  availableFrom: z
    .string()
    .min(1, "Alege data de la care locuința e disponibilă.")
    .regex(/^\d{4}-\d{2}-\d{2}$/, "Data nu este validă."),
  address: z
    .string()
    .trim()
    .min(1, "Introdu adresa.")
    .max(
      LISTING_LIMITS.address,
      `Adresa poate avea cel mult ${LISTING_LIMITS.address} de caractere.`,
    ),
  city: z
    .string()
    .trim()
    .min(1, "Introdu orașul.")
    .max(LISTING_LIMITS.city, `Orașul poate avea cel mult ${LISTING_LIMITS.city} de caractere.`),
  googleMapsUrl: z
    .string()
    .trim()
    .min(1, "Lipește linkul din Google Maps.")
    .max(
      LISTING_LIMITS.googleMapsUrl,
      `Linkul poate avea cel mult ${LISTING_LIMITS.googleMapsUrl} de caractere.`,
    ),
  amenities: z.array(z.enum(AMENITIES as [Amenity, ...Amenity[]])),
});

export type ListingFormValues = z.infer<typeof listingFormSchema>;

export const EMPTY_LISTING_FORM: ListingFormValues = {
  title: "",
  description: "",
  type: "APARTMENT",
  price: "",
  currency: "RON",
  rooms: "",
  bathrooms: "",
  areaSqm: "",
  availableFrom: "",
  address: "",
  city: "",
  googleMapsUrl: "",
  amenities: [],
};

/** Valorile formularului → corpul cererii către backend. */
export function toListingRequest(values: ListingFormValues): ListingRequest {
  return {
    title: values.title.trim(),
    description: values.description.trim(),
    type: values.type,
    price: parseNumber(values.price),
    currency: values.currency,
    rooms: parseNumber(values.rooms),
    bathrooms: parseNumber(values.bathrooms),
    areaSqm: parseNumber(values.areaSqm),
    address: values.address.trim(),
    city: values.city.trim(),
    amenities: values.amenities,
    availableFrom: values.availableFrom,
    googleMapsUrl: values.googleMapsUrl.trim(),
  };
}

/** Anunțul salvat → valorile inițiale ale formularului de editare. */
export function toFormValues(listing: Listing): ListingFormValues {
  return {
    title: listing.title,
    description: listing.description,
    type: listing.type,
    price: String(listing.price),
    currency: listing.currency,
    rooms: String(listing.rooms),
    bathrooms: String(listing.bathrooms),
    areaSqm: String(listing.areaSqm),
    availableFrom: listing.availableFrom,
    address: listing.address,
    city: listing.city,
    googleMapsUrl: prefillMapsLink(listing.googleMapsUrl, listing.latitude, listing.longitude),
    amenities: listing.amenities,
  };
}
