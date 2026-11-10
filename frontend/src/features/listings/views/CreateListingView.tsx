"use client";

import { useState } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { Button } from "@/shared/components/Button";
import { Icon } from "@/shared/components/Icon";
import { toast } from "@/shared/components/Toaster";
import { ListingForm } from "../components/ListingForm";
import { ListingImagesManager } from "../components/ListingImagesManager";
import { useCreateListing } from "../hooks/useCreateListing";
import { useMyListing } from "../hooks/useMyListing";
import { toListingRequest, type ListingFormValues } from "../schemas/listingSchemas";
import type { Listing } from "../types";
import styles from "./CreateListingView.module.css";

/** Pasul 1: datele anunțului. Pasul 2: pozele (au nevoie de un anunț deja creat). */
export function CreateListingView() {
  const router = useRouter();
  const createListing = useCreateListing();
  const [created, setCreated] = useState<Listing | null>(null);

  // Pozele se reîncarcă după fiecare operație; până vine răspunsul folosim anunțul tocmai creat.
  const detail = useMyListing(created?.id ?? 0);

  async function handleSave(values: ListingFormValues) {
    const listing = await createListing.mutateAsync(toListingRequest(values));
    setCreated(listing);
    window.scrollTo({ top: 0 });
  }

  function finish() {
    toast.success("Anunțul a fost publicat.");
    router.push("/my-listings");
  }

  if (created) {
    return (
      <main className={styles.page}>
        <h1 className={styles.title}>Adaugă poze</h1>
        <p className={styles.lead}>
          Anunțul „{created.title}” a fost creat. Pozele aduc mai mulți chiriași, dar le poți adăuga
          și mai târziu, din „Editează”.
        </p>

        <section className={styles.card} aria-labelledby="photos-title">
          <h2 id="photos-title" className={styles.cardTitle}>
            <span className={styles.step}>5</span>
            Poze
          </h2>
          <ListingImagesManager
            listingId={created.id}
            images={detail.data?.images ?? created.images}
          />
        </section>

        <div className={styles.actions}>
          <Button onClick={finish} icon={<Icon name="check" size={18} />}>
            Finalizează
          </Button>
        </div>
      </main>
    );
  }

  return (
    <main className={styles.page}>
      <Link href="/my-listings" className={styles.back}>
        <Icon name="arrow-left" size={18} />
        Anunțurile mele
      </Link>
      <h1 className={styles.title}>Publică un anunț</h1>
      <p className={styles.lead}>Completează detaliile, apoi adaugă pozele locuinței.</p>

      <ListingForm submitLabel="Publică anunțul" cancelHref="/my-listings" onSave={handleSave} />
    </main>
  );
}
