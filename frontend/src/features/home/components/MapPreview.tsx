"use client";

import { formatArea } from "@/features/listings/utils/formatters";
import { Icon } from "@/shared/components/Icon";
import { useHomeListings } from "../hooks/useHomeListings";
import styles from "./MapPreview.module.css";

const PILL_CLASSES = [styles.pillA, styles.pillB, styles.pillC, styles.pillD];
const SELECTED_PILL = 2;

const priceFormat = new Intl.NumberFormat("ro-RO", { maximumFractionDigits: 2 });

function price(amount: number, currency: "RON" | "EUR"): string {
  return `${priceFormat.format(amount)} ${currency === "EUR" ? "€" : "RON"}`;
}

/**
 * Ilustrație a hărții cu anunțuri; harta propriu-zisă e în căutare. Prețurile, numărul
 * și cardul vin din anunțurile reale; pozițiile pe hartă sunt doar decorative.
 */
export function MapPreview() {
  const { data } = useHomeListings();
  const items = data?.items ?? [];
  const selectedIndex = Math.min(SELECTED_PILL, items.length - 1);
  const selected = items[selectedIndex];

  return (
    <div className={styles.map} role="img" aria-label="Exemplu de hartă cu anunțuri">
      <svg
        className={styles.canvas}
        viewBox="0 0 520 480"
        preserveAspectRatio="xMidYMid slice"
        aria-hidden="true"
      >
        <rect width="520" height="480" fill="#e8eee8" />
        <rect x="0" y="45" width="135" height="100" rx="6" fill="#cfe5c9" />
        <rect x="372" y="300" width="148" height="130" rx="6" fill="#cfe5c9" />
        <path
          d="M0 322C110 296 190 352 320 330S470 296 520 300V372C440 362 330 392 200 388S60 372 0 388Z"
          fill="#bcd8e6"
        />
        <g stroke="#ffffff" strokeWidth="9" fill="none">
          <path d="M18 0V480M112 0V480M236 0V480M338 0V480M452 0V480" />
          <path d="M0 40H520M0 150H520M0 262H520M0 430H520" />
        </g>
      </svg>

      {items.slice(0, PILL_CLASSES.length).map((listing, index) => (
        <span
          key={listing.id}
          className={`${styles.pill} ${index === selectedIndex ? styles.pillSelected : ""} ${PILL_CLASSES[index]}`}
        >
          {price(listing.price, listing.currency)}
        </span>
      ))}
      {data && data.totalElements > 1 && (
        <span className={styles.cluster}>{data.totalElements}</span>
      )}

      {selected && (
        <div className={styles.card}>
          <span className={styles.thumb}>
            <Icon name="home" size={26} />
          </span>
          <span className={styles.cardText}>
            <strong className={styles.cardTitle}>{selected.title}</strong>
            <span className={styles.cardMeta}>
              {[selected.city, selected.areaSqm !== undefined ? formatArea(selected.areaSqm) : null]
                .filter(Boolean)
                .join(" · ")}
            </span>
            <strong className={styles.cardPrice}>
              {price(selected.price, selected.currency)} / lună
            </strong>
          </span>
        </div>
      )}
    </div>
  );
}
