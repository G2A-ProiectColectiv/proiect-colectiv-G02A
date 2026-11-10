import { httpClient } from "@/shared/api/httpClient";
import type { Coordinates, ParseLocationRequest } from "../types";

/** Doar pentru previzualizare: la salvare, serverul extrage din nou coordonatele din link (LOC-04). */
export async function parseLocation(url: string): Promise<Coordinates> {
  const request: ParseLocationRequest = { url };
  const { data } = await httpClient.post<Coordinates>("/location/parse", request);
  return data;
}
