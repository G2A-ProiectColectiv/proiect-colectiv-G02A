"use client";

import { useQueries } from "@tanstack/react-query";
import { getMine } from "../api/rentalRequestsApi";
import { REQUEST_TABS } from "../constants/requestStatuses";
import type { RequestStatusChoice } from "../types";
import { requestKeys } from "./requestKeys";

export type RequestCounts = Partial<Record<RequestStatusChoice, number>>;

/** Câte cereri are chiriașul în fiecare status (pentru numerele din tab-uri). */
export function useMyRequestCounts(): RequestCounts {
  return useQueries({
    queries: REQUEST_TABS.map(({ status }) => {
      const params = { status, page: 0, size: 1 };
      return { queryKey: requestKeys.mineList(params), queryFn: () => getMine(params) };
    }),
    combine: (results) => {
      const counts: RequestCounts = {};
      REQUEST_TABS.forEach(({ status }, index) => {
        const total = results[index].data?.totalElements;
        if (total !== undefined) counts[status] = total;
      });
      return counts;
    },
  });
}
