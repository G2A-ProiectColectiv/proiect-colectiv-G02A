import type { ListingStatus, ListingStatusChoice } from "../types";

export const LISTING_STATUS_LABELS: Record<ListingStatus, string> = {
  ACTIVE: "Activ",
  RENTED: "Închiriat",
  UNAVAILABLE: "Indisponibil",
  DELETED: "Șters",
};

/** Tab-urile din „Anunțurile mele" (GEST-05), în ordinea afișării. */
export const LISTING_TABS: { status: ListingStatusChoice; label: string }[] = [
  { status: "ACTIVE", label: "Active" },
  { status: "RENTED", label: "Închiriate" },
  { status: "UNAVAILABLE", label: "Indisponibile" },
];
