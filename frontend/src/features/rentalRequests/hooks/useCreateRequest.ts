"use client";

import { useMutation, useQueryClient } from "@tanstack/react-query";
import { createRequest } from "../api/rentalRequestsApi";
import { requestKeys } from "./requestKeys";

export function useCreateRequest() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: createRequest,
    onSuccess: async (request) => {
      queryClient.setQueryData(requestKeys.mineDetail(request.id), request);
      await queryClient.invalidateQueries({ queryKey: requestKeys.mine });
    },
  });
}
