"use client";

import type { ReactNode } from "react";
import { Marker, Popup } from "react-leaflet";
import MarkerClusterGroup from "react-leaflet-cluster";
import "react-leaflet-cluster/dist/assets/MarkerCluster.css";
import "react-leaflet-cluster/dist/assets/MarkerCluster.Default.css";

export interface ClusterMarker {
  id: string | number;
  /** `[latitudine, longitudine]` */
  position: [number, number];
  /** Conținutul ferestrei care se deschide la apăsarea pe pin. */
  popup?: ReactNode;
}

export interface MarkerClusterLayerProps {
  markers: ClusterMarker[];
}

/** Markere grupate când sunt apropiate (HARTA-05). Se pune ca element copil în `Map`. */
export function MarkerClusterLayer({ markers }: MarkerClusterLayerProps) {
  return (
    <MarkerClusterGroup chunkedLoading>
      {markers.map((marker) => (
        <Marker key={marker.id} position={marker.position}>
          {marker.popup && <Popup>{marker.popup}</Popup>}
        </Marker>
      ))}
    </MarkerClusterGroup>
  );
}
