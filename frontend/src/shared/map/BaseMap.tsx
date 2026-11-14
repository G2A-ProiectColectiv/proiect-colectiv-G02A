"use client";

import { useEffect, useRef, useState, type ReactNode } from "react";
import { MapContainer, Marker, Popup, TileLayer, useMap } from "react-leaflet";
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
  /** `[latitudine, longitudine]`. Când se schimbă după montare, harta se mută pe noul centru. */
  center: [number, number];
  zoom?: number;
  markers?: MapMarker[];
  scrollWheelZoom?: boolean;
  ariaLabel?: string;
  className?: string;
  /** Straturi suplimentare desenate în hartă (ex. `RadiusCircle`). */
  children?: ReactNode;
}

/**
 * Mută harta când se schimbă centrul sau zoom-ul. Nu remontăm `MapContainer` cu `key`:
 * dacă harta e distrusă în timp ce un strat se adaugă (Fast Refresh, StrictMode, coordonate
 * schimbate repede), Leaflet dă „Cannot read properties of undefined (reading 'appendChild')”.
 */
function Recenter({ center, zoom }: { center: [number, number]; zoom: number }) {
  const map = useMap();
  const [latitude, longitude] = center;
  const isFirstRender = useRef(true);

  useEffect(() => {
    // La montare, `MapContainer` a pus deja harta pe centrul inițial.
    if (isFirstRender.current) {
      isFirstRender.current = false;
      return;
    }
    map.setView([latitude, longitude], zoom);
  }, [map, latitude, longitude, zoom]);

  return null;
}

/**
 * Hartă OpenStreetMap. Se importă doar prin `shared/map/index.ts`, ca să nu fie randată pe server.
 *
 * `MapContainer` din react-leaflet 5 își creează harta o singură dată (la primul `ref`) și o
 * șterge la cleanup-ul efectului, dar nu o mai recreează dacă efectele sunt repornite fără
 * remontarea elementului. Asta se întâmplă în `next dev` (StrictMode și Fast Refresh) și dă
 * „Map container is being reused by another instance”, „… reading 'appendChild'” sau
 * „… reading '_leaflet_pos'”. De aceea, la orice cleanup schimbăm `key`, ca React să monteze
 * un `MapContainer` nou, cu un element DOM nou și o hartă nouă.
 */
export function BaseMap(props: BaseMapProps) {
  const [generation, setGeneration] = useState(0);

  useEffect(() => {
    return () => setGeneration((value) => value + 1);
  }, []);

  return <MapView key={generation} {...props} />;
}

function MapView({
  center,
  zoom = 13,
  markers = [],
  scrollWheelZoom = true,
  ariaLabel = "Hartă",
  className,
  children,
}: BaseMapProps) {
  return (
    <MapContainer
      center={center}
      zoom={zoom}
      scrollWheelZoom={scrollWheelZoom}
      className={[styles.map, className ?? ""].filter(Boolean).join(" ")}
      aria-label={ariaLabel}
    >
      <Recenter center={center} zoom={zoom} />
      <TileLayer
        attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>'
        url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
      />
      {markers.map((marker) => (
        <Marker key={marker.id} position={marker.position}>
          {marker.label && <Popup>{marker.label}</Popup>}
        </Marker>
      ))}
      {children}
    </MapContainer>
  );
}
