import Link from "next/link";
import { PROPERTY_TYPE_LABELS } from "@/features/listings/constants/propertyTypes";
import { formatDate, formatRooms } from "@/features/listings/utils/formatters";
import { Avatar } from "@/shared/components/Avatar";
import { Icon } from "@/shared/components/Icon";
import type { RentalRequestSummary } from "../types";
import { formatBudget, formatPlace } from "../utils/formatters";
import styles from "./RequestCard.module.css";

export interface RequestCardProps {
  request: RentalRequestSummary;
}

/** Cardul din căutarea proprietarilor: titlu, buget, oraș, camere, perioadă, chiriașul (CAUT-06). */
export function RequestCard({ request }: RequestCardProps) {
  const desiredFrom = formatDate(request.desiredFrom);
  const { tenant } = request;

  return (
    <Link href={`/requests/${request.id}`} className={styles.link}>
      <article className={styles.card}>
        <h3 className={styles.title}>{request.title}</h3>

        <p className={styles.budget}>
          {formatBudget(request.budgetMin, request.budgetMax, request.currency)}
          <span className={styles.perMonth}> / lună</span>
        </p>

        <ul className={styles.chips} aria-label="Detaliile cererii">
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

        {tenant && (
          <div className={styles.tenant}>
            <Avatar
              firstName={tenant.firstName}
              lastName={tenant.lastName}
              avatarUrl={tenant.avatarUrl}
              size="sm"
            />
            <span>
              {tenant.firstName} {tenant.lastName}
            </span>
          </div>
        )}
      </article>
    </Link>
  );
}
