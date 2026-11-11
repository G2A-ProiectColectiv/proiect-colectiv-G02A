"use client";

import { useEffect } from "react";
import { latLng } from "leaflet";
import { Circle, useMap } from "react-leaflet";
import styles from "./RadiusCircle.module.css";

export interface RadiusCircleProps {
  /** `[latitudine, longitudine]` */
  center: [number, number];
  radiusKm: number;
}

const FIT_PADDING: [number, number] = [16, 16];

/**
 * Zona căutată, desenată ca cerc (CER-03). La fiecare schimbare a centrului sau a razei,
 * harta se mută ca cercul să încapă întreg. Se pune doar în `Map` din `shared/map/index.ts`.
 */
export function RadiusCircle({ center, radiusKm }: RadiusCircleProps) {
  const map = useMap();
  const [latitude, longitude] = center;
  const radiusMeters = radiusKm * 1000;

  useEffect(() => {
    // `toBounds` primește latura pătratului în metri, adică diametrul cercului.
    const bounds = latLng(latitude, longitude).toBounds(radiusMeters * 2);
    map.fitBounds(bounds, { padding: FIT_PADDING });
  }, [map, latitude, longitude, radiusMeters]);

  return (
    // `className` se citește doar la crearea cercului (nu prin `pathOptions`, care se aplică cu `setStyle`).
    <Circle center={[latitude, longitude]} radius={radiusMeters} className={styles.circle} />
  );
}
