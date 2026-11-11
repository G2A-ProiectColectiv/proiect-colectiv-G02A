"use client";

import dynamic from "next/dynamic";

export type { BaseMapProps, MapMarker } from "./BaseMap";
export type { RadiusCircleProps } from "./RadiusCircle";

/*
 * Singurul punct de intrare pentru hărți. Leaflet are nevoie de `window`, deci componentele
 * se încarcă doar în browser. Restul aplicației importă `Map` și `RadiusCircle` de aici,
 * niciodată direct din `BaseMap.tsx` sau `RadiusCircle.tsx`.
 */
export const Map = dynamic(() => import("./BaseMap").then((module) => module.BaseMap), {
  ssr: false,
});

/** Cerc cu centru și rază; se pune ca element copil în `Map`. */
export const RadiusCircle = dynamic(
  () => import("./RadiusCircle").then((module) => module.RadiusCircle),
  { ssr: false },
);
