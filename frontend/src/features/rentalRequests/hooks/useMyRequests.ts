"use client";

import { keepPreviousData, useQuery } from "@tanstack/react-query";
import { getMine } from "../api/rentalRequestsApi";
import type { MyRequestsParams } from "../types";
import { requestKeys } from "./requestKeys";

/** O pagină din „Cererile mele"; la schimbarea paginii o păstrează pe cea veche până vine următoarea. */
export function useMyRequests(params: MyRequestsParams) {
  return useQuery({
    queryKey: requestKeys.mineList(params),
    queryFn: () => getMine(params),
    placeholderData: keepPreviousData,
  });
}
