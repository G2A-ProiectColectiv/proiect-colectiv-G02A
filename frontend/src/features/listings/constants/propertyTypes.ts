import type { PropertyType } from "../types";

export const PROPERTY_TYPE_LABELS: Record<PropertyType, string> = {
  APARTMENT: "Apartament",
  STUDIO: "Garsonieră",
  ROOM: "Cameră",
  HOUSE: "Casă",
  OTHER: "Altele",
};

export const PROPERTY_TYPES = Object.keys(PROPERTY_TYPE_LABELS) as PropertyType[];
