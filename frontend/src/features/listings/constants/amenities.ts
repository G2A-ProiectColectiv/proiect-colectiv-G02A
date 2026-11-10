import type { Amenity } from "../types";

/** Lista fixă de dotări (ANUN-03), în ordinea afișării. */
export const AMENITY_LABELS: Record<Amenity, string> = {
  FURNISHED: "Mobilat",
  PARKING: "Parcare",
  BALCONY: "Balcon",
  AIR_CONDITIONING: "Aer condiționat",
  WASHING_MACHINE: "Mașină de spălat",
  PETS_ALLOWED: "Animale permise",
  INTERNET: "Internet",
};

export const AMENITIES = Object.keys(AMENITY_LABELS) as Amenity[];
