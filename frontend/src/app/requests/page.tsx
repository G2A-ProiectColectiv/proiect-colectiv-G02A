import { Suspense } from "react";
import type { Metadata } from "next";
import { RequestSearchView } from "@/features/rentalRequests/views/RequestSearchView";

export const metadata: Metadata = { title: "Cereri de la chiriași" };

export default function RequestsPage() {
  // `useSearchParams` din filtre cere <Suspense>, altfel `next build` dă eroare.
  return (
    <Suspense>
      <RequestSearchView />
    </Suspense>
  );
}
