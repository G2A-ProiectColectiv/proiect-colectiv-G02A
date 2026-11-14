import { directionsUrl } from "./googleMapsUrls";
import styles from "./MapLinkButton.module.css";

export interface DirectionsButtonProps {
  latitude: number;
  longitude: number;
  label?: string;
}

/** Deschide direcțiile către locație în Google Maps, într-un tab nou. */
export function DirectionsButton({
  latitude,
  longitude,
  label = "Direcții",
}: DirectionsButtonProps) {
  return (
    <a
      className={styles.link}
      href={directionsUrl(latitude, longitude)}
      target="_blank"
      rel="noopener noreferrer"
    >
      {label}
    </a>
  );
}
