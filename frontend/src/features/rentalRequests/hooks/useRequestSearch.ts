"use client";

import { keepPreviousData, useQuery } from "@tanstack/react-query";
import type { PropertyType } from "@/features/listings/types";
import { searchRequests } from "../api/requestSearchApi";
import type { RequestSearchFilters } from "../schemas/requestSearchSchema";
import type { RentalRequestSearchParams } from "../types";
import { requestKeys } from "./requestKeys";

export const REQUEST_SEARCH_PAGE_SIZE = 12;

/** Filtrele din URL → parametrii API-ului. `page` e numărat de la 1 în URL și de la 0 în API. */
export function toRequestSearchParams(
  filters: RequestSearchFilters,
  page: number,
): RentalRequestSearchParams {
  const { type, ...rest } = filters;
  return {
    ...rest,
    type: type as PropertyType | undefined,
    page: page - 1,
    size: REQUEST_SEARCH_PAGE_SIZE,
  };
}

/** O pagină de cereri; la schimbarea filtrelor o păstrează pe cea veche până vine următoarea. */
export function useRequestSearch(filters: RequestSearchFilters, page: number) {
  const params = toRequestSearchParams(filters, page);
  return useQuery({
    queryKey: requestKeys.search(params),
    queryFn: () => searchRequests(params),
    placeholderData: keepPreviousData,
  });
}
