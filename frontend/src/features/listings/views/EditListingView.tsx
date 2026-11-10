"use client";

import Link from "next/link";
import { getApiErrorMessage, getApiErrorStatus } from "@/shared/api/apiError";
import { Button } from "@/shared/components/Button";
import { EmptyState } from "@/shared/components/EmptyState";
import { Icon } from "@/shared/components/Icon";
import { LinkButton } from "@/shared/components/LinkButton";
import { Spinner } from "@/shared/components/Spinner";
import { toast } from "@/shared/components/Toaster";
import { ListingForm } from "../components/ListingForm";
import { ListingImagesManager } from "../components/ListingImagesManager";
import { useMyListing } from "../hooks/useMyListing";
import { useUpdateListing } from "../hooks/useUpdateListing";
import { toFormValues, toListingRequest, type ListingFormValues } from "../schemas/listingSchemas";
import styles from "./EditListingView.module.css";

export interface EditListingViewProps {
  listingId: number;
}

export function EditListingView({ listingId }: EditListingViewProps) {
  const query = useMyListing(listingId);
  const updateListing = useUpdateListing(listingId);

  async function handleSave(values: ListingFormValues) {
    await updateListing.mutateAsync(toListingRequest(values));
    toast.success("Modificările au fost salvate.");
  }

  if (query.isPending && query.fetchStatus !== "idle") {
    return (
      <main className={styles.page}>
        <div className={styles.center}>
          <Spinner size="lg" />
        </div>
      </main>
    );
  }

  if (query.isError || !query.data) {
    // Pentru anunțul altcuiva backendul răspunde 403, iar pentru unul inexistent sau șters, 404 (GEST-02).
    const status = getApiErrorStatus(query.error);
    const title =
      status === 403
        ? "Nu ai acces la acest anunț"
        : status === 404 || !query.error
          ? "Anunțul nu a fost găsit"
          : "Nu am putut încărca anunțul";

    return (
      <main className={styles.page}>
        <EmptyState
          title={title}
          description={
            status === 403 || status === 404 || !query.error
              ? "Poți edita doar anunțurile tale care nu au fost șterse."
              : getApiErrorMessage(query.error)
          }
          icon={<Icon name="alert" size={26} />}
          action={
            status === 403 || status === 404 || !query.error ? (
              <LinkButton href="/my-listings">Înapoi la anunțurile mele</LinkButton>
            ) : (
              <Button onClick={() => query.refetch()}>Încearcă din nou</Button>
            )
          }
        />
      </main>
    );
  }

  const listing = query.data;

  return (
    <main className={styles.page}>
      <Link href="/my-listings" className={styles.back}>
        <Icon name="arrow-left" size={18} />
        Anunțurile mele
      </Link>
      <h1 className={styles.title}>Editează anunțul</h1>

      <ListingForm
        defaultValues={toFormValues(listing)}
        initialCoordinates={{ latitude: listing.latitude, longitude: listing.longitude }}
        submitLabel="Salvează modificările"
        cancelHref="/my-listings"
        onSave={handleSave}
      />

      <section className={styles.card} aria-labelledby="photos-title">
        <h2 id="photos-title" className={styles.cardTitle}>
          <span className={styles.step}>5</span>
          Poze
        </h2>
        <ListingImagesManager listingId={listing.id} images={listing.images} />
      </section>
    </main>
  );
}
