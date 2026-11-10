import { LISTING_STATUS_LABELS } from "../constants/listingStatuses";
import type { ListingStatus } from "../types";
import styles from "./ListingStatusBadge.module.css";

export interface ListingStatusBadgeProps {
  status: ListingStatus;
}

export function ListingStatusBadge({ status }: ListingStatusBadgeProps) {
  return (
    <span className={`${styles.badge} ${styles[status]}`}>{LISTING_STATUS_LABELS[status]}</span>
  );
}
