"use client";

import Image from "next/image";
import Link from "next/link";
import {
  Map,
  MapBoundsWatcher,
  MarkerClusterLayer,
  OpenInGoogleMapsButton,
  type ClusterMarker,
  type MapBounds,
} from "@/shared/map";
import { imageUrl } from "@/shared/utils/imageUrl";
import type { ListingMapMarker } from "../types";
import { formatPrice } from "../utils/formatters";
import styles from "./ListingResultsMap.module.css";

export interface ListingResultsMapProps {
  markers: ListingMapMarker[];
  onBoundsChange: (bounds: MapBounds) => void;
  isFetching: boolean;
}

/** Centrul României, ca harta să înceapă cu toată țara vizibilă. */
const INITIAL_CENTER: [number, number] = [45.9, 24.9];
const INITIAL_ZOOM = 6;

function toClusterMarker(marker: ListingMapMarker): ClusterMarker {
  const photo = marker.primaryImage ? imageUrl(marker.primaryImage) : null;

  return {
    id: marker.id,
    position: [marker.latitude, marker.longitude],
    popup: (
      <div className={styles.popup}>
        {photo && (
          <Image
            src={photo}
            alt=""
            width={200}
            height={120}
            unoptimized
            className={styles.popupImage}
          />
        )}
        <strong className={styles.popupTitle}>{marker.title}</strong>
        <span>{formatPrice(marker.price, marker.currency)} / lună</span>
        <Link href={`/listings/${marker.id}`}>Vezi anunțul</Link>
        <OpenInGoogleMapsButton latitude={marker.latitude} longitude={marker.longitude} />
      </div>
    ),
  };
}

/** Harta cu anunțurile din zona vizibilă (HARTA-02…06). */
export function ListingResultsMap({ markers, onBoundsChange, isFetching }: ListingResultsMapProps) {
  return (
    <div className={styles.frame} aria-busy={isFetching}>
      <Map
        center={INITIAL_CENTER}
        zoom={INITIAL_ZOOM}
        ariaLabel="Harta anunțurilor"
        className={styles.map}
      >
        <MapBoundsWatcher onBoundsChange={onBoundsChange} />
        <MarkerClusterLayer markers={markers.map(toClusterMarker)} />
      </Map>
    </div>
  );
}
