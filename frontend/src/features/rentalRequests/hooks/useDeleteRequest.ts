"use client";

import { useMutation, useQueryClient } from "@tanstack/react-query";
import { remove } from "../api/rentalRequestsApi";
import { requestKeys } from "./requestKeys";

export function useDeleteRequest() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (id: number) => remove(id),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: requestKeys.all }),
  });
}
