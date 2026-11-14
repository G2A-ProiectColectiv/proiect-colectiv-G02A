import { Suspense } from "react";
import type { Metadata } from "next";
import { RequestDetailsView } from "@/features/rentalRequests/views/RequestDetailsView";
import { Spinner } from "@/shared/components/Spinner";

export const metadata: Metadata = { title: "Detalii cerere" };

type RequestParams = PageProps<"/requests/[id]">["params"];

/* Parametrii din URL se citesc într-un `Suspense` (vezi și `app/listings/[id]/page.tsx`). */
async function RequestRoute({ params }: { params: RequestParams }) {
  const { id } = await params;
  return <RequestDetailsView requestId={Number(id)} />;
}

export default function RequestPage({ params }: PageProps<"/requests/[id]">) {
  return (
    <Suspense fallback={<Spinner size="lg" />}>
      <RequestRoute params={params} />
    </Suspense>
  );
}
