import Image from "next/image";
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
  /** Culoarea de fundal a zonei de poză, vizibilă când anunțul nu are poze. */
  tone: "green" | "yellow" | "blue" | "pink";
  /** Adresa pozei principale; fără ea se afișează pictograma. */
  imageSrc?: string | null;
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
  imageSrc,
}: ListingCardPreviewProps) {
  return (
    <Link href={href} className={styles.card}>
      <span className={`${styles.photo} ${styles[tone]}`}>
        {imageSrc ? (
          <Image
            src={imageSrc}
            alt=""
            fill
            sizes="(max-width: 720px) 100vw, 280px"
            unoptimized
            className={styles.img}
          />
        ) : (
          <Icon name="home" size={44} className={styles.placeholder} />
        )}
        <span className={`${styles.badge} ${styles[badgeTone]}`}>{badge}</span>
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
