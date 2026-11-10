"use client";

import dynamic from "next/dynamic";

export type { BaseMapProps, MapMarker } from "./BaseMap";

/*
 * Singurul punct de intrare pentru hărți. Leaflet are nevoie de `window`, deci componenta
 * se încarcă doar în browser. Restul aplicației importă `Map` de aici, niciodată din `BaseMap.tsx`.
 */
export const Map = dynamic(() => import("./BaseMap").then((module) => module.BaseMap), {
  ssr: false,
});
