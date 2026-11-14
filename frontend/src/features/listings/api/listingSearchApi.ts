import { httpClient } from "@/shared/api/httpClient";
import type { PageResponse } from "@/shared/types/api";
import type {
  ListingDetails,
  ListingMapMarker,
  ListingSearchParams,
  ListingSummary,
} from "../types";

/** Căutarea publică de anunțuri (fără autentificare). */
export async function searchListings(
  params: ListingSearchParams,
): Promise<PageResponse<ListingSummary>> {
  const { data } = await httpClient.get<PageResponse<ListingSummary>>("/listings", { params });
  return data;
}

/** Pagina publică a unui anunț activ; telefonul proprietarului vine doar dacă există token. */
export async function getListingDetails(id: number): Promise<ListingDetails> {
  const { data } = await httpClient.get<ListingDetails>(`/listings/${id}`);
  return data;
}

/** Anunțurile din zona vizibilă a hărții, cu aceleași filtre ca lista. `bbox` = `south,west,north,east`. */
export async function searchListingsForMap(
  bbox: string,
  params: ListingSearchParams,
): Promise<ListingMapMarker[]> {
  const { data } = await httpClient.get<ListingMapMarker[]>("/listings/map", {
    params: { ...params, bbox },
  });
  return data;
}
