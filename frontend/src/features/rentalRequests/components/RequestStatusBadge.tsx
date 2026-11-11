import { REQUEST_STATUS_LABELS } from "../constants/requestStatuses";
import type { RequestStatus } from "../types";
import styles from "./RequestStatusBadge.module.css";

export interface RequestStatusBadgeProps {
  status: RequestStatus;
}

export function RequestStatusBadge({ status }: RequestStatusBadgeProps) {
  return (
    <span className={`${styles.badge} ${styles[status]}`}>{REQUEST_STATUS_LABELS[status]}</span>
  );
}
