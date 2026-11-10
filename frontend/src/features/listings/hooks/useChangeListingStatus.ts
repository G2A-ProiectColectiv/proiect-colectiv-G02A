"use client";

import { useMutation, useQueryClient } from "@tanstack/react-query";
import { changeStatus } from "../api/listingsApi";
import type { ListingStatusChoice } from "../types";
import { listingKeys } from "./listingKeys";

export function useChangeListingStatus() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ id, status }: { id: number; status: ListingStatusChoice }) =>
      changeStatus(id, status),
    // Anunțul se mută în alt tab, deci invalidăm și listele, și numerele din tab-uri.
    onSuccess: () => queryClient.invalidateQueries({ queryKey: listingKeys.all }),
  });
}
