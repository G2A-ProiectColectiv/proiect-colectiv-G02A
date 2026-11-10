import L from "leaflet";
import "leaflet/dist/leaflet.css";
import markerIcon2x from "leaflet/dist/images/marker-icon-2x.png";
import markerIcon from "leaflet/dist/images/marker-icon.png";
import markerShadow from "leaflet/dist/images/marker-shadow.png";

/*
 * Leaflet își caută iconițele implicite după o cale pe care bundler-ul nu o păstrează,
 * deci pin-ul ar apărea fără imagine. Îi dăm direct adresele fișierelor importate.
 * Modulul se importă doar din `BaseMap.tsx` (care rulează numai în browser).
 */
delete (L.Icon.Default.prototype as unknown as { _getIconUrl?: unknown })._getIconUrl;

L.Icon.Default.mergeOptions({
  iconRetinaUrl: markerIcon2x.src,
  iconUrl: markerIcon.src,
  shadowUrl: markerShadow.src,
});
