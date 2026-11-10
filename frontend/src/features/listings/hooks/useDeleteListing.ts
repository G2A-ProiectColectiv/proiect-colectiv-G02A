"use client";

import { useMutation, useQueryClient } from "@tanstack/react-query";
import { remove } from "../api/listingsApi";
import { listingKeys } from "./listingKeys";

export function useDeleteListing() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (id: number) => remove(id),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: listingKeys.all }),
  });
}
