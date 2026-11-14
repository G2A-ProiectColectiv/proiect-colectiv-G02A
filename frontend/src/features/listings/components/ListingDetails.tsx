import { AMENITY_LABELS } from "../constants/amenities";
import { PROPERTY_TYPE_LABELS } from "../constants/propertyTypes";
import type { ListingDetails as ListingDetailsData } from "../types";
import {
  formatArea,
  formatBathrooms,
  formatDate,
  formatPrice,
  formatRooms,
} from "../utils/formatters";
import styles from "./ListingDetails.module.css";

export interface ListingDetailsProps {
  listing: ListingDetailsData;
}

/** Descriere, preț, camere, băi, suprafață, dotări și disponibilitate (DET-01). */
export function ListingDetails({ listing }: ListingDetailsProps) {
  const facts = [
    { label: "Tip", value: PROPERTY_TYPE_LABELS[listing.type] },
    { label: "Camere", value: formatRooms(listing.rooms) },
    { label: "Băi", value: formatBathrooms(listing.bathrooms) },
    { label: "Suprafață", value: formatArea(listing.areaSqm) },
    { label: "Disponibil din", value: formatDate(listing.availableFrom) },
  ];

  return (
    <div className={styles.details}>
      <header className={styles.header}>
        <h1 className={styles.title}>{listing.title}</h1>
        <p className={styles.price}>
          {formatPrice(listing.price, listing.currency)}
          <span className={styles.perMonth}> / lună</span>
        </p>
        <p className={styles.address}>
          {listing.address}, {listing.city}
        </p>
      </header>

      <dl className={styles.facts}>
        {facts.map((fact) => (
          <div key={fact.label} className={styles.fact}>
            <dt>{fact.label}</dt>
            <dd>{fact.value}</dd>
          </div>
        ))}
      </dl>

      <section>
        <h2 className={styles.heading}>Descriere</h2>
        <p className={styles.description}>{listing.description}</p>
      </section>

      {listing.amenities.length > 0 && (
        <section>
          <h2 className={styles.heading}>Dotări</h2>
          <ul className={styles.amenities}>
            {listing.amenities.map((amenity) => (
              <li key={amenity} className={styles.amenity}>
                {AMENITY_LABELS[amenity]}
              </li>
            ))}
          </ul>
        </section>
      )}
    </div>
  );
}
