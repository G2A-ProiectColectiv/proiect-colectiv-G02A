import { Suspense } from "react";
import type { Metadata } from "next";
import { EditListingView } from "@/features/listings/views/EditListingView";
import { Spinner } from "@/shared/components/Spinner";

export const metadata: Metadata = { title: "Editează anunțul" };

type EditListingParams = PageProps<"/my-listings/[id]/edit">["params"];

/*
 * Cu `cacheComponents` activ, citirea parametrilor din URL se face într-un `Suspense`:
 * restul paginii (meniul, scheletul) se poate afișa imediat.
 */
async function EditListingRoute({ params }: { params: EditListingParams }) {
  const { id } = await params;
  return <EditListingView listingId={Number(id)} />;
}

export default function EditListingPage({ params }: PageProps<"/my-listings/[id]/edit">) {
  return (
    <Suspense fallback={<Spinner size="lg" />}>
      <EditListingRoute params={params} />
    </Suspense>
  );
}
