"use client";

import { useEffect, useState } from "react";
import { keepPreviousData, useQuery } from "@tanstack/react-query";
import type { MapBounds } from "@/shared/map";
import { searchListingsForMap } from "../api/listingSearchApi";
import type { ListingSearchFilters } from "../schemas/listingSearchSchema";
import { listingKeys } from "./listingKeys";
import { toSearchParams } from "./useListingSearch";

const MOVE_DEBOUNCE_MS = 400;

/**
 * Markerele din zona vizibilă, cu filtrele curente (HARTA-02…04). Zona se actualizează
 * cu `setBounds` (din `MapBoundsWatcher`); cererea pleacă abia după ce harta stă pe loc.
 */
export function useListingsInBounds(filters: ListingSearchFilters, enabled = true) {
  const [bounds, setBounds] = useState<MapBounds | null>(null);
  const [settled, setSettled] = useState<MapBounds | null>(null);

  useEffect(() => {
    if (!bounds) return;
    const timer = setTimeout(() => setSettled(bounds), MOVE_DEBOUNCE_MS);
    return () => clearTimeout(timer);
  }, [bounds]);

  // Harta nu folosește sortarea sau pagina.
  const { sort, page, size, ...params } = toSearchParams(filters, 1);
  void sort;
  void page;
  void size;

  const bbox = settled ? `${settled.south},${settled.west},${settled.north},${settled.east}` : null;

  const query = useQuery({
    queryKey: listingKeys.map(params, bbox ?? ""),
    queryFn: () => searchListingsForMap(bbox as string, params),
    enabled: enabled && bbox !== null,
    placeholderData: keepPreviousData,
  });

  return { ...query, setBounds };
}
