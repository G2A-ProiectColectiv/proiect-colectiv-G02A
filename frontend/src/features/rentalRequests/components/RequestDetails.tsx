import { PROPERTY_TYPE_LABELS } from "@/features/listings/constants/propertyTypes";
import { formatDate, formatRooms } from "@/features/listings/utils/formatters";
import { Map, OpenInGoogleMapsButton, RadiusCircle } from "@/shared/map";
import type { RentalRequestDetails } from "../types";
import { formatBudget, formatPlace } from "../utils/formatters";
import styles from "./RequestDetails.module.css";

export interface RequestDetailsProps {
  request: RentalRequestDetails;
}

/** Preferințele, bugetul, perioada și zona pe hartă ca cerc (DET-05). */
export function RequestDetails({ request }: RequestDetailsProps) {
  const { centerLatitude, centerLongitude, radiusKm } = request;
  const hasZone = centerLatitude !== null && centerLongitude !== null && radiusKm !== null;

  const facts = [
    { label: "Tip dorit", value: PROPERTY_TYPE_LABELS[request.desiredType] },
    { label: "Camere", value: request.rooms !== null ? formatRooms(request.rooms) : "Oricâte" },
    { label: "De la data", value: formatDate(request.desiredFrom) },
    { label: "Locație", value: formatPlace(request.city, request.area) },
  ];

  return (
    <div className={styles.details}>
      <header className={styles.header}>
        <h1 className={styles.title}>{request.title}</h1>
        <p className={styles.budget}>
          Buget {formatBudget(request.budgetMin, request.budgetMax, request.currency)}
          <span className={styles.perMonth}> / lună</span>
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
        <p className={styles.description}>{request.description}</p>
      </section>

      {hasZone && (
        <section className={styles.zone} aria-label="Zona căutată">
          <h2 className={styles.heading}>Zona căutată</h2>
          <p className={styles.zoneHint}>Aproximativ {radiusKm} km în jurul punctului marcat.</p>
          <div className={styles.map}>
            <Map
              center={[centerLatitude, centerLongitude]}
              zoom={13}
              scrollWheelZoom={false}
              ariaLabel="Zona căutată de chiriaș, pe hartă"
            >
              <RadiusCircle center={[centerLatitude, centerLongitude]} radiusKm={radiusKm} />
            </Map>
          </div>
          <div>
            <OpenInGoogleMapsButton latitude={centerLatitude} longitude={centerLongitude} />
          </div>
        </section>
      )}
    </div>
  );
}
