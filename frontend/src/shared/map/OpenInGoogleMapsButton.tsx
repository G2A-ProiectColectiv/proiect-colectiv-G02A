import { searchUrl } from "./googleMapsUrls";
import styles from "./MapLinkButton.module.css";

export interface OpenInGoogleMapsButtonProps {
  latitude: number;
  longitude: number;
  label?: string;
}

/** Deschide locația în Google Maps, într-un tab nou (în aplicație pe telefon). */
export function OpenInGoogleMapsButton({
  latitude,
  longitude,
  label = "Deschide în Google Maps",
}: OpenInGoogleMapsButtonProps) {
  return (
    <a
      className={styles.link}
      href={searchUrl(latitude, longitude)}
      target="_blank"
      rel="noopener noreferrer"
    >
      {label}
    </a>
  );
}
