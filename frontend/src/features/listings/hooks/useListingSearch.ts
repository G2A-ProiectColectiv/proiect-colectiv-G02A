"use client";

import { keepPreviousData, useQuery } from "@tanstack/react-query";
import { searchListings } from "../api/listingSearchApi";
import type { ListingSearchFilters } from "../schemas/listingSearchSchema";
import type { ListingSearchParams, PropertyType } from "../types";
import { listingKeys } from "./listingKeys";

export const SEARCH_PAGE_SIZE = 12;

/** Filtrele din URL → parametrii API-ului. `page` e numărat de la 1 în URL și de la 0 în API. */
export function toSearchParams(filters: ListingSearchFilters, page: number): ListingSearchParams {
  // `view` e doar starea paginii, nu un filtru.
  const { amenities, type, view, ...rest } = filters;
  void view;
  return {
    ...rest,
    type: type as PropertyType | undefined,
    amenities: amenities && amenities.length > 0 ? amenities.join(",") : undefined,
    page: page - 1,
    size: SEARCH_PAGE_SIZE,
  };
}

/** O pagină de rezultate; la schimbarea filtrelor o păstrează pe cea veche până vine următoarea. */
export function useListingSearch(filters: ListingSearchFilters, page: number, enabled = true) {
  const params = toSearchParams(filters, page);
  return useQuery({
    queryKey: listingKeys.search(params),
    queryFn: () => searchListings(params),
    enabled,
    placeholderData: keepPreviousData,
  });
}
