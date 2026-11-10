import type { Coordinates } from "../types";

/** Dreptunghiul care înconjoară România (aproximativ). Folosit doar pentru avertismentul de la LOC-13. */
const ROMANIA_BOUNDS = { south: 43.6, north: 48.3, west: 20.2, east: 29.8 };

export function isInRomania({ latitude, longitude }: Coordinates): boolean {
  return (
    latitude >= ROMANIA_BOUNDS.south &&
    latitude <= ROMANIA_BOUNDS.north &&
    longitude >= ROMANIA_BOUNDS.west &&
    longitude <= ROMANIA_BOUNDS.east
  );
}
