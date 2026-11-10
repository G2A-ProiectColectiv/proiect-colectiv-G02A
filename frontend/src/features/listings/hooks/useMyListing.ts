"use client";

import { useQuery } from "@tanstack/react-query";
import { getMineById } from "../api/listingsApi";
import { listingKeys } from "./listingKeys";

/** Un anunț al proprietarului, cu pozele și coordonatele (pentru editare). */
export function useMyListing(id: number) {
  return useQuery({
    queryKey: listingKeys.mineDetail(id),
    queryFn: () => getMineById(id),
    enabled: Number.isInteger(id) && id > 0,
    // Un 403 sau 404 nu se rezolvă singur; nu are rost să mai încercăm.
    retry: false,
  });
}
