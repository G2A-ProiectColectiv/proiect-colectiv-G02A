import { PROPERTY_TYPE_LABELS } from "@/features/listings/constants/propertyTypes";
import { formatDate, formatRooms } from "@/features/listings/utils/formatters";
import { Icon } from "@/shared/components/Icon";
import type { RentalRequestSummary } from "../types";
import { formatBudget, formatPlace } from "../utils/formatters";
import { RequestStatusActions } from "./RequestStatusActions";
import { RequestStatusBadge } from "./RequestStatusBadge";
import styles from "./MyRequestCard.module.css";

export interface MyRequestCardProps {
  request: RentalRequestSummary;
}

export function MyRequestCard({ request }: MyRequestCardProps) {
  const published = request.createdAt ? formatDate(request.createdAt) : "";
  const desiredFrom = formatDate(request.desiredFrom);

  return (
    <article className={styles.card}>
      <div className={styles.body}>
        <div className={styles.top}>
          <h3 className={styles.title}>{request.title}</h3>
          <RequestStatusBadge status={request.status} />
        </div>

        {published && <p className={styles.published}>Publicată pe {published}</p>}

        <ul className={styles.chips} aria-label="Detaliile cererii">
          <li className={`${styles.chip} ${styles.budget}`}>
            Buget {formatBudget(request.budgetMin, request.budgetMax, request.currency)}
          </li>
          <li className={styles.chip}>
            <Icon name="pin" size={16} />
            {formatPlace(request.city, request.area)}
          </li>
          {request.desiredType && (
            <li className={styles.chip}>{PROPERTY_TYPE_LABELS[request.desiredType]}</li>
          )}
          {request.rooms !== null && request.rooms !== undefined && (
            <li className={styles.chip}>{formatRooms(request.rooms)}</li>
          )}
          {desiredFrom && <li className={styles.chip}>De la {desiredFrom}</li>}
        </ul>
      </div>

      <RequestStatusActions request={request} />
    </article>
  );
}
