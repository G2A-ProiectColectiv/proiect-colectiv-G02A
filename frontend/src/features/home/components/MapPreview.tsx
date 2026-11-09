import { Icon } from "@/shared/components/Icon";
import styles from "./MapPreview.module.css";

/** Ilustrație a hărții cu anunțuri, doar decorativă; harta reală (OpenStreetMap) vine în etapa 1.5. */
export function MapPreview() {
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

      <span className={`${styles.pill} ${styles.pillA}`}>380 €</span>
      <span className={`${styles.pill} ${styles.pillB}`}>1.100 RON</span>
      <span className={`${styles.pill} ${styles.pillSelected} ${styles.pillC}`}>450 €</span>
      <span className={`${styles.pill} ${styles.pillD}`}>520 €</span>
      <span className={styles.cluster}>6</span>

      <div className={styles.card}>
        <span className={styles.thumb}>
          <Icon name="home" size={26} />
        </span>
        <span className={styles.cardText}>
          <strong className={styles.cardTitle}>Apartament 2 camere, mobilat</strong>
          <span className={styles.cardMeta}>Cluj-Napoca, Mărăști · 54 m²</span>
          <strong className={styles.cardPrice}>450 € / lună</strong>
        </span>
      </div>
    </div>
  );
}
