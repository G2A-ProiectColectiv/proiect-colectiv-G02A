"use client";

import { useMutation, useQueryClient } from "@tanstack/react-query";
import { updateListing } from "../api/listingsApi";
import type { ListingRequest } from "../types";
import { listingKeys } from "./listingKeys";

export function useUpdateListing(id: number) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (request: ListingRequest) => updateListing(id, request),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: listingKeys.all }),
  });
}
