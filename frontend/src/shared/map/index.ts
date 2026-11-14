"use client";

import dynamic from "next/dynamic";

export type { BaseMapProps, MapMarker } from "./BaseMap";
export type { RadiusCircleProps } from "./RadiusCircle";
export type { MapBounds, MapBoundsWatcherProps } from "./MapBoundsWatcher";
export type { ClusterMarker, MarkerClusterLayerProps } from "./MarkerClusterLayer";
export { OpenInGoogleMapsButton } from "./OpenInGoogleMapsButton";
export type { OpenInGoogleMapsButtonProps } from "./OpenInGoogleMapsButton";
export { DirectionsButton } from "./DirectionsButton";
export type { DirectionsButtonProps } from "./DirectionsButton";
export { searchUrl, directionsUrl } from "./googleMapsUrls";

/*
 * Singurul punct de intrare pentru hărți. Leaflet are nevoie de `window`, deci componentele
 * se încarcă doar în browser. Restul aplicației importă `Map` și `RadiusCircle` de aici,
 * niciodată direct din `BaseMap.tsx` sau `RadiusCircle.tsx`.
 */
export const Map = dynamic(() => import("./BaseMap").then((module) => module.BaseMap), {
  ssr: false,
});

/** Anunță zona vizibilă a hărții; se pune ca element copil în `Map`. */
export const MapBoundsWatcher = dynamic(
  () => import("./MapBoundsWatcher").then((module) => module.MapBoundsWatcher),
  { ssr: false },
);

/** Markere grupate (HARTA-05); se pune ca element copil în `Map`. */
export const MarkerClusterLayer = dynamic(
  () => import("./MarkerClusterLayer").then((module) => module.MarkerClusterLayer),
  { ssr: false },
);

/** Cerc cu centru și rază; se pune ca element copil în `Map`. */
export const RadiusCircle = dynamic(
  () => import("./RadiusCircle").then((module) => module.RadiusCircle),
  { ssr: false },
);
