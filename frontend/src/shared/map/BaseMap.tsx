"use client";

import { MapContainer, Marker, Popup, TileLayer } from "react-leaflet";
import "./leafletSetup";
import styles from "./BaseMap.module.css";

export interface MapMarker {
  id: string | number;
  /** `[latitudine, longitudine]` */
  position: [number, number];
  /** Text afișat când se apasă pe pin. */
  label?: string;
}

export interface BaseMapProps {
  /** `[latitudine, longitudine]`. Centrul se citește doar la montare; schimbă `key` ca să recentrezi. */
  center: [number, number];
  zoom?: number;
  markers?: MapMarker[];
  scrollWheelZoom?: boolean;
  ariaLabel?: string;
  className?: string;
}

/** Hartă OpenStreetMap. Se importă doar prin `shared/map/index.ts`, ca să nu fie randată pe server. */
export function BaseMap({
  center,
  zoom = 13,
  markers = [],
  scrollWheelZoom = true,
  ariaLabel = "Hartă",
  className,
}: BaseMapProps) {
  return (
    <MapContainer
      center={center}
      zoom={zoom}
      scrollWheelZoom={scrollWheelZoom}
      className={[styles.map, className ?? ""].filter(Boolean).join(" ")}
      aria-label={ariaLabel}
    >
      <TileLayer
        attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>'
        url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
      />
      {markers.map((marker) => (
        <Marker key={marker.id} position={marker.position}>
          {marker.label && <Popup>{marker.label}</Popup>}
        </Marker>
      ))}
    </MapContainer>
  );
}
