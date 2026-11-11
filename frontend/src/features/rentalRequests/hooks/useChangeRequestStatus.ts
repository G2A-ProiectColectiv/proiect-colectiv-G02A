"use client";

import { useMutation, useQueryClient } from "@tanstack/react-query";
import { changeStatus } from "../api/rentalRequestsApi";
import type { RequestStatusChoice } from "../types";
import { requestKeys } from "./requestKeys";

export function useChangeRequestStatus() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ id, status }: { id: number; status: RequestStatusChoice }) =>
      changeStatus(id, status),
    // Cererea se mută în alt tab, deci invalidăm și listele, și numerele din tab-uri.
    onSuccess: () => queryClient.invalidateQueries({ queryKey: requestKeys.all }),
  });
}
