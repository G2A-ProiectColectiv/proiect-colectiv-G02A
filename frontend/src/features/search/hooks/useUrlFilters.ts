"use client";

import { useCallback, useMemo } from "react";
import { usePathname, useRouter, useSearchParams } from "next/navigation";
import type { z } from "zod";
import {
  buildQueryString,
  parseFilters,
  parsePage,
  type FiltersSchema,
} from "./urlFilters";

export interface UseUrlFilters<S extends FiltersSchema> {
  filters: Partial<z.infer<S>>;
  page: number;
  /** Îmbină `patch` peste filtrele curente (`undefined` șterge un filtru) și resetează pagina la 1. */
  setFilters: (patch: Partial<z.infer<S>>) => void;
  /** Schimbă doar pagina, păstrând filtrele. */
  setPage: (page: number) => void;
  /** Șterge toate filtrele și revine la pagina 1. */
  resetFilters: () => void;
}

/**
 * Starea filtrelor trăiește în URL, ca rezultatele să poată fi partajate (CAUT-04).
 * Folosește `router.replace`, deci filtrarea nu umple istoricul browserului.
 * Componentele care îl folosesc trebuie învelite în `<Suspense>` în `page.tsx`.
 */
export function useUrlFilters<S extends FiltersSchema>(schema: S): UseUrlFilters<S> {
  const router = useRouter();
  const pathname = usePathname();
  const searchParams = useSearchParams();
  const queryString = searchParams.toString();

  const filters = useMemo(
    () => parseFilters(schema, new URLSearchParams(queryString)),
    [schema, queryString],
  );
  const page = useMemo(() => parsePage(new URLSearchParams(queryString)), [queryString]);

  const navigate = useCallback(
    (nextFilters: Record<string, unknown>, nextPage: number) => {
      const query = buildQueryString(nextFilters, nextPage);
      router.replace(query ? `${pathname}?${query}` : pathname, { scroll: false });
    },
    [router, pathname],
  );

  const setFilters = useCallback(
    (patch: Partial<z.infer<S>>) => navigate({ ...filters, ...patch }, 1),
    [navigate, filters],
  );
  const setPage = useCallback(
    (nextPage: number) => navigate(filters, Math.max(1, nextPage)),
    [navigate, filters],
  );
  const resetFilters = useCallback(() => navigate({}, 1), [navigate]);

  return { filters, page, setFilters, setPage, resetFilters };
}
