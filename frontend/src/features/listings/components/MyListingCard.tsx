import Image from "next/image";
import { Icon } from "@/shared/components/Icon";
import { imageUrl } from "@/shared/utils/imageUrl";
import { PROPERTY_TYPE_LABELS } from "../constants/propertyTypes";
import type { ListingSummary } from "../types";
import {
  formatArea,
  formatBathrooms,
  formatDate,
  formatPrice,
  formatRooms,
} from "../utils/formatters";
import { ListingStatusActions } from "./ListingStatusActions";
import { ListingStatusBadge } from "./ListingStatusBadge";
import styles from "./MyListingCard.module.css";

/** `imageUrl` poate întoarce `null` pentru o cale goală, iar `next/image` are nevoie de un text. */
function photoSrc(path: string): string {
  return imageUrl(path) ?? "";
}

export interface MyListingCardProps {
  listing: ListingSummary;
}

export function MyListingCard({ listing }: MyListingCardProps) {
  const details = [
    listing.type ? PROPERTY_TYPE_LABELS[listing.type] : null,
    listing.rooms !== undefined ? formatRooms(listing.rooms) : null,
    listing.bathrooms !== undefined ? formatBathrooms(listing.bathrooms) : null,
    listing.areaSqm !== undefined ? formatArea(listing.areaSqm) : null,
  ].filter((item): item is string => item !== null);

  const published = formatDate(listing.createdAt);

  return (
    <article className={styles.card}>
      <div className={styles.photo}>
        {listing.primaryImage ? (
          <Image
            src={photoSrc(listing.primaryImage)}
            alt=""
            fill
            sizes="(max-width: 640px) 100vw, 200px"
            unoptimized
            className={styles.img}
          />
        ) : (
          <Icon name="home" size={40} className={styles.placeholder} />
        )}
      </div>

      <div className={styles.body}>
        <div className={styles.top}>
          <h3 className={styles.title}>{listing.title}</h3>
          <ListingStatusBadge status={listing.status} />
        </div>

        <p className={styles.price}>
          {formatPrice(listing.price, listing.currency)}
          <span className={styles.perMonth}> / lună</span>
        </p>

        {listing.city && (
          <p className={styles.location}>
            <Icon name="pin" size={16} />
            {listing.city}
          </p>
        )}

        {details.length > 0 && (
          <p className={styles.meta}>
            {details.map((item) => (
              <span key={item}>{item}</span>
            ))}
            {published && <span>Publicat pe {published}</span>}
          </p>
        )}

        <ListingStatusActions listing={listing} />
      </div>
    </article>
  );
}
