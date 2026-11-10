"use client";

import { useMutation, useQueryClient } from "@tanstack/react-query";
import { createListing } from "../api/listingsApi";
import { listingKeys } from "./listingKeys";

export function useCreateListing() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: createListing,
    onSuccess: async (listing) => {
      // Pasul de poze are nevoie de anunț imediat, fără o nouă cerere.
      queryClient.setQueryData(listingKeys.mineDetail(listing.id), listing);
      await queryClient.invalidateQueries({ queryKey: listingKeys.mine });
    },
  });
}
