import Image from "next/image";
import Link from "next/link";
import { Icon } from "@/shared/components/Icon";
import { imageUrl } from "@/shared/utils/imageUrl";
import { PROPERTY_TYPE_LABELS } from "../constants/propertyTypes";
import type { ListingSummary } from "../types";
import { formatArea, formatPrice, formatRooms } from "../utils/formatters";
import styles from "./ListingCard.module.css";

export interface ListingCardProps {
  listing: ListingSummary;
}

/** Cardul din rezultatele căutării: poza principală, titlu, preț, oraș, camere (CAUT-06). */
export function ListingCard({ listing }: ListingCardProps) {
  const photo = listing.primaryImage ? imageUrl(listing.primaryImage) : null;
  const details = [
    listing.type ? PROPERTY_TYPE_LABELS[listing.type] : null,
    listing.rooms !== undefined ? formatRooms(listing.rooms) : null,
    listing.areaSqm !== undefined ? formatArea(listing.areaSqm) : null,
  ].filter((item): item is string => item !== null);

  return (
    <Link href={`/listings/${listing.id}`} className={styles.link}>
      <article className={styles.card}>
      <div className={styles.photo}>
        {photo ? (
          <Image
            src={photo}
            alt=""
            fill
            sizes="(max-width: 720px) 100vw, (max-width: 1100px) 50vw, 340px"
            unoptimized
            className={styles.img}
          />
        ) : (
          <Icon name="home" size={40} className={styles.placeholder} />
        )}
      </div>

      <div className={styles.body}>
        <p className={styles.price}>
          {formatPrice(listing.price, listing.currency)}
          <span className={styles.perMonth}> / lună</span>
        </p>
        <h3 className={styles.title}>{listing.title}</h3>

        {listing.city && (
          <p className={styles.meta}>
            <Icon name="pin" size={16} />
            {listing.city}
          </p>
        )}

        {details.length > 0 && (
          <p className={styles.meta}>
            {details.map((item) => (
              <span key={item}>{item}</span>
            ))}
          </p>
        )}
      </div>
      </article>
    </Link>
  );
}
