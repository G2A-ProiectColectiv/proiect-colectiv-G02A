"use client";

import { useQueries } from "@tanstack/react-query";
import { getMine } from "../api/listingsApi";
import { LISTING_TABS } from "../constants/listingStatuses";
import type { ListingStatusChoice } from "../types";
import { listingKeys } from "./listingKeys";

export type ListingCounts = Partial<Record<ListingStatusChoice, number>>;

/** Câte anunțuri are proprietarul în fiecare status (pentru numerele din tab-uri). */
export function useMyListingCounts(): ListingCounts {
  return useQueries({
    queries: LISTING_TABS.map(({ status }) => {
      const params = { status, page: 0, size: 1 };
      return { queryKey: listingKeys.mineList(params), queryFn: () => getMine(params) };
    }),
    combine: (results) => {
      const counts: ListingCounts = {};
      LISTING_TABS.forEach(({ status }, index) => {
        const total = results[index].data?.totalElements;
        if (total !== undefined) counts[status] = total;
      });
      return counts;
    },
  });
}
