"use client";

import { useQuery } from "@tanstack/react-query";
import { searchListings } from "@/features/listings/api/listingSearchApi";
import { listingKeys } from "@/features/listings/hooks/listingKeys";
import type { ListingSearchParams } from "@/features/listings/types";

const HOME_LISTINGS_COUNT = 4;

const PARAMS: ListingSearchParams = { page: 0, size: HOME_LISTINGS_COUNT };

/**
 * Cele mai noi anunțuri active, din baza de date. Lista și harta de pe pagina principală
 * folosesc aceeași cheie, deci o singură cerere.
 */
export function useHomeListings() {
  return useQuery({
    queryKey: listingKeys.search(PARAMS),
    queryFn: () => searchListings(PARAMS),
  });
}
