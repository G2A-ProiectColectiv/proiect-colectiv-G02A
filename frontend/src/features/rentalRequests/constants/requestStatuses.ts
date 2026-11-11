import type { RequestStatus, RequestStatusChoice } from "../types";

export const REQUEST_STATUS_LABELS: Record<RequestStatus, string> = {
  ACTIVE: "Activă",
  FOUND: "Găsită",
  CLOSED: "Închisă",
  DELETED: "Ștearsă",
};

/** Tab-urile din „Cererile mele" (CER-08), în ordinea afișării. */
export const REQUEST_TABS: { status: RequestStatusChoice; label: string }[] = [
  { status: "ACTIVE", label: "Active" },
  { status: "FOUND", label: "Găsite" },
  { status: "CLOSED", label: "Închise" },
];
