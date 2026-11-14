"use client";

import { notFound } from "next/navigation";
import { getApiErrorMessage, getApiErrorStatus } from "@/shared/api/apiError";
import { Button } from "@/shared/components/Button";
import { EmptyState } from "@/shared/components/EmptyState";
import { Icon } from "@/shared/components/Icon";
import { Spinner } from "@/shared/components/Spinner";
import { DirectionsButton, Map, OpenInGoogleMapsButton } from "@/shared/map";
import { ListingDetails } from "../components/ListingDetails";
import { ListingGallery } from "../components/ListingGallery";
import { OwnerContactCard } from "../components/OwnerContactCard";
import { useListing } from "../hooks/useListing";
import styles from "./ListingDetailsView.module.css";

const MAP_ZOOM = 15;

export interface ListingDetailsViewProps {
  listingId: number;
}

export function ListingDetailsView({ listingId }: ListingDetailsViewProps) {
  const query = useListing(listingId);

  // `notFound()` afișează `not-found.tsx` al rutei: „Anunțul nu mai este disponibil” (DET-04).
  if (!Number.isInteger(listingId) || listingId <= 0 || getApiErrorStatus(query.error) === 404) {
    notFound();
  }

  if (query.isPending) {
    return (
      <main className={styles.center}>
        <Spinner size="lg" />
      </main>
    );
  }

  if (query.isError) {
    return (
      <main className={styles.center}>
        <EmptyState
          title="Nu am putut încărca anunțul"
          description={getApiErrorMessage(query.error)}
          icon={<Icon name="alert" size={26} />}
          action={<Button onClick={() => query.refetch()}>Încearcă din nou</Button>}
        />
      </main>
    );
  }

  const listing = query.data;
  const { latitude, longitude } = listing;

  return (
    <main className={styles.page}>
      <div className={styles.layout}>
        <div className={styles.main}>
          <ListingGallery images={listing.images} title={listing.title} />
          <ListingDetails listing={listing} />
        </div>

        <aside className={styles.side}>
          <OwnerContactCard owner={listing.owner} />

          <section className={styles.location} aria-label="Locația">
            <h2 className={styles.heading}>Locația</h2>
            <div className={styles.map}>
              <Map
                center={[latitude, longitude]}
                zoom={MAP_ZOOM}
                markers={[{ id: listing.id, position: [latitude, longitude], label: listing.title }]}
                scrollWheelZoom={false}
                ariaLabel="Locația anunțului pe hartă"
              />
            </div>
            <div className={styles.mapActions}>
              <OpenInGoogleMapsButton latitude={latitude} longitude={longitude} />
              <DirectionsButton latitude={latitude} longitude={longitude} />
            </div>
          </section>
        </aside>
      </div>
    </main>
  );
}
