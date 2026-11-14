"use client";

import { getApiErrorMessage } from "@/shared/api/apiError";
import { Button } from "@/shared/components/Button";
import { EmptyState } from "@/shared/components/EmptyState";
import { Icon } from "@/shared/components/Icon";
import { Spinner } from "@/shared/components/Spinner";
import { imageUrl } from "@/shared/utils/imageUrl";
import {
  formatArea,
  formatBathrooms,
  formatDate,
  formatRooms,
} from "@/features/listings/utils/formatters";
import type { ListingSummary } from "@/features/listings/types";
import { useHomeListings } from "../hooks/useHomeListings";
import { ListingCardPreview, type ListingCardPreviewProps } from "./ListingCardPreview";
import styles from "./RecentListings.module.css";

const TONES: ListingCardPreviewProps["tone"][] = ["green", "yellow", "blue", "pink"];

const priceFormat = new Intl.NumberFormat("ro-RO", { maximumFractionDigits: 2 });

/** „Disponibil acum” dacă data de disponibilitate a trecut, altfel „Din 1 dec. 2026”. */
function availability(availableFrom: string | undefined): Pick<ListingCardPreviewProps, "badge" | "badgeTone"> {
  const today = new Date().toISOString().slice(0, 10);
  if (!availableFrom || availableFrom <= today) {
    return { badge: "Disponibil acum", badgeTone: "available" };
  }
  return { badge: `Din ${formatDate(availableFrom)}`, badgeTone: "soon" };
}

function toCardProps(listing: ListingSummary, index: number): ListingCardPreviewProps {
  const meta = [
    listing.rooms !== undefined ? formatRooms(listing.rooms) : null,
    listing.bathrooms !== undefined ? formatBathrooms(listing.bathrooms) : null,
    listing.areaSqm !== undefined ? formatArea(listing.areaSqm) : null,
  ].filter((item): item is string => item !== null);

  return {
    href: `/listings/${listing.id}`,
    price: priceFormat.format(listing.price),
    currency: listing.currency === "EUR" ? "€" : "RON",
    title: listing.title,
    meta,
    location: listing.city ?? "",
    tone: TONES[index % TONES.length],
    imageSrc: listing.primaryImage ? imageUrl(listing.primaryImage) : null,
    ...availability(listing.availableFrom),
  };
}

/** Cele mai noi anunțuri din baza de date, pentru pagina principală. */
export function RecentListings() {
  const listings = useHomeListings();

  if (listings.isPending) {
    return (
      <div className={styles.center}>
        <Spinner size="lg" />
      </div>
    );
  }

  if (listings.isError) {
    return (
      <EmptyState
        title="Nu am putut încărca anunțurile"
        description={getApiErrorMessage(listings.error)}
        icon={<Icon name="alert" size={26} />}
        action={<Button onClick={() => listings.refetch()}>Încearcă din nou</Button>}
      />
    );
  }

  if (listings.data.items.length === 0) {
    return (
      <EmptyState
        title="Nu există anunțuri deocamdată"
        description="Revino curând sau publică tu primul anunț."
        icon={<Icon name="home" size={26} />}
      />
    );
  }

  return (
    <div className={styles.cards}>
      {listings.data.items.map((listing, index) => (
        <ListingCardPreview key={listing.id} {...toCardProps(listing, index)} />
      ))}
    </div>
  );
}
