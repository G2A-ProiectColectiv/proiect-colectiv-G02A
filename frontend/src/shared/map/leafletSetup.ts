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

/*
 * În funcție de bundler, importul unei imagini întoarce fie adresa ca text, fie un obiect
 * `{ src }`. Cu Turbopack, în browser vine textul, iar `.src` ar fi `undefined`
 * (Leaflet aruncă atunci „iconUrl not set” și pagina se oprește).
 */
function assetUrl(asset: string | { src: string }): string {
  return typeof asset === "string" ? asset : asset.src;
}

L.Icon.Default.mergeOptions({
  iconRetinaUrl: assetUrl(markerIcon2x),
  iconUrl: assetUrl(markerIcon),
  shadowUrl: assetUrl(markerShadow),
});
