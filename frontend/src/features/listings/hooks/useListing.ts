"use client";

import { useQuery } from "@tanstack/react-query";
import { useAuth } from "@/features/auth/context/AuthContext";
import { getListingDetails } from "../api/listingSearchApi";
import { listingKeys } from "./listingKeys";

/** Pagina publică a unui anunț (DET-01…04). */
export function useListing(id: number) {
  const { user } = useAuth();

  return useQuery({
    queryKey: listingKeys.detail(id, user?.id ?? null),
    queryFn: () => getListingDetails(id),
    enabled: Number.isInteger(id) && id > 0,
    // Un 404 (anunț șters, închiriat sau al unui proprietar dezactivat) nu se rezolvă singur.
    retry: false,
  });
}
