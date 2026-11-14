import { Map, OpenInGoogleMapsButton } from "@/shared/map";
import type { Coordinates } from "../types";
import styles from "./LocationPreviewMap.module.css";

export interface LocationPreviewMapProps {
  coordinates: Coordinates;
  /** Text afișat când se apasă pe pin. */
  label?: string;
}

const PREVIEW_ZOOM = 16;

/** Pin pe coordonatele extrase, ca utilizatorul să confirme că locația e corectă (LOC-03). */
export function LocationPreviewMap({ coordinates, label }: LocationPreviewMapProps) {
  const { latitude, longitude } = coordinates;

  return (
    <div className={styles.wrapper}>
      <div className={styles.frame}>
        {/* `key` remontează harta când se schimbă coordonatele, ca să se recentreze. */}
        <Map
          key={`${latitude},${longitude}`}
          center={[latitude, longitude]}
          zoom={PREVIEW_ZOOM}
          markers={[{ id: "location", position: [latitude, longitude], label }]}
          scrollWheelZoom={false}
          ariaLabel="Locația extrasă din link, pe hartă"
        />
      </div>
      <OpenInGoogleMapsButton latitude={latitude} longitude={longitude} />
    </div>
  );
}
