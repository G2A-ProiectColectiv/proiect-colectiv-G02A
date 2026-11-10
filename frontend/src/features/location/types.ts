/** `CoordinatesDto` din backend (răspunsul lui `POST /api/location/parse`). */
export interface Coordinates {
  latitude: number;
  longitude: number;
}

export interface ParseLocationRequest {
  /** Link Google Maps (lung sau scurt) sau coordonate simple, maximum 2000 de caractere. */
  url: string;
}
