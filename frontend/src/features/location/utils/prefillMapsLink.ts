import { searchUrl } from "@/shared/map/googleMapsUrls";

/**
 * Valoarea cu care se precompletează câmpul „Link Google Maps” la editare: linkul salvat, dacă
 * e un link; altfel (câmp gol sau doar coordonate scrise ca text) un link Google Maps construit
 * din coordonatele salvate. Fără niciuna, un câmp gol.
 */
export function prefillMapsLink(
  savedLink: string | null | undefined,
  latitude: number | null | undefined,
  longitude: number | null | undefined,
): string {
  const link = savedLink?.trim() ?? "";
  if (/^(https?:\/\/|maps\.app\.goo\.gl\/|goo\.gl\/)/i.test(link)) return link;

  if (typeof latitude === "number" && typeof longitude === "number") {
    return searchUrl(latitude, longitude);
  }
  return link;
}
