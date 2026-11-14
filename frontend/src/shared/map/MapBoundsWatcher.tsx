"use client";

import { useEffect } from "react";
import { useMap, useMapEvents } from "react-leaflet";

/** Zona vizibilă a hărții. */
export interface MapBounds {
  south: number;
  west: number;
  north: number;
  east: number;
}

export interface MapBoundsWatcherProps {
  onBoundsChange: (bounds: MapBounds) => void;
}

function clamp(value: number, min: number, max: number): number {
  return Math.min(max, Math.max(min, value));
}

/**
 * Anunță zona vizibilă la montare și după fiecare mutare sau zoom. Valorile sunt rotunjite
 * și limitate la intervalele acceptate de server (lat ±90, lng ±180), chiar dacă harta e
 * dată la o parte pe orizontală. Se pune ca element copil în `Map`.
 */
export function MapBoundsWatcher({ onBoundsChange }: MapBoundsWatcherProps) {
  const map = useMap();

  function report() {
    const bounds = map.getBounds();
    onBoundsChange({
      south: round(clamp(bounds.getSouth(), -90, 90)),
      west: round(clamp(bounds.getWest(), -180, 180)),
      north: round(clamp(bounds.getNorth(), -90, 90)),
      east: round(clamp(bounds.getEast(), -180, 180)),
    });
  }

  useMapEvents({ moveend: report });

  // Zona inițială, înainte ca utilizatorul să miște harta.
  useEffect(report, []); // eslint-disable-line react-hooks/exhaustive-deps

  return null;
}

function round(value: number): number {
  return Math.round(value * 10000) / 10000;
}
