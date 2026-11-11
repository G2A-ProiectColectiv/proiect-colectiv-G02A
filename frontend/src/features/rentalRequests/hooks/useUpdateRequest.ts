"use client";

import { useMutation, useQueryClient } from "@tanstack/react-query";
import { updateRequest } from "../api/rentalRequestsApi";
import type { RentalRequestPayload } from "../types";
import { requestKeys } from "./requestKeys";

export function useUpdateRequest(id: number) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (payload: RentalRequestPayload) => updateRequest(id, payload),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: requestKeys.all }),
  });
}
