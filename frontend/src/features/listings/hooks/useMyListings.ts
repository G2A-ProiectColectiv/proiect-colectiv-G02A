"use client";

import { keepPreviousData, useQuery } from "@tanstack/react-query";
import { getMine } from "../api/listingsApi";
import type { MyListingsParams } from "../types";
import { listingKeys } from "./listingKeys";

/** O pagină din „Anunțurile mele"; la schimbarea paginii o păstrează pe cea veche până vine următoarea. */
export function useMyListings(params: MyListingsParams) {
  return useQuery({
    queryKey: listingKeys.mineList(params),
    queryFn: () => getMine(params),
    placeholderData: keepPreviousData,
  });
}
