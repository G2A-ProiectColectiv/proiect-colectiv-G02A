import Link from "next/link";
import { Icon } from "@/shared/components/Icon";
import styles from "./ListingCardPreview.module.css";

export interface ListingCardPreviewProps {
  href: string;
  price: string;
  currency: string;
  title: string;
  meta: string[];
  location: string;
  badge: string;
  badgeTone: "available" | "soon";
  /** Culoarea de fundal a zonei de poză (până când anunțurile au poze reale). */
  tone: "green" | "yellow" | "blue" | "pink";
}

export function ListingCardPreview({
  href,
  price,
  currency,
  title,
  meta,
  location,
  badge,
  badgeTone,
  tone,
}: ListingCardPreviewProps) {
  return (
    <Link href={href} className={styles.card}>
      <span className={`${styles.photo} ${styles[tone]}`}>
        <span className={`${styles.badge} ${styles[badgeTone]}`}>{badge}</span>
        <Icon name="home" size={44} className={styles.placeholder} />
      </span>

      <span className={styles.body}>
        <span className={styles.priceRow}>
          <strong className={styles.price}>
            {price} {currency}
          </strong>
          <span className={styles.perMonth}>/ lună</span>
        </span>
        <strong className={styles.title}>{title}</strong>
        <span className={styles.meta}>
          {meta.map((item) => (
            <span key={item}>{item}</span>
          ))}
        </span>
        <span className={styles.location}>
          <Icon name="pin" size={15} />
          {location}
        </span>
      </span>
    </Link>
  );
}
