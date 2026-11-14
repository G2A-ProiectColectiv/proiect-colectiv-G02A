import { Suspense } from "react";
import type { Metadata } from "next";
import { ListingSearchView } from "@/features/listings/views/ListingSearchView";

export const metadata: Metadata = { title: "Caută locuințe" };

export default function ListingsPage() {
  // `useSearchParams` din filtre cere <Suspense>, altfel `next build` dă eroare.
  return (
    <Suspense>
      <ListingSearchView />
    </Suspense>
  );
}
