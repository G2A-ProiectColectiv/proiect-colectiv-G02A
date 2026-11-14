import { Suspense } from "react";
import type { Metadata } from "next";
import { ListingDetailsView } from "@/features/listings/views/ListingDetailsView";
import { Spinner } from "@/shared/components/Spinner";

export const metadata: Metadata = { title: "Detalii anunț" };

type ListingParams = PageProps<"/listings/[id]">["params"];

/*
 * Cu `cacheComponents` activ, citirea parametrilor din URL se face într-un `Suspense`.
 * Pagina e publică; datele se încarcă pe client, ca telefonul să depindă de sesiunea curentă.
 */
async function ListingRoute({ params }: { params: ListingParams }) {
  const { id } = await params;
  return <ListingDetailsView listingId={Number(id)} />;
}

export default function ListingPage({ params }: PageProps<"/listings/[id]">) {
  return (
    <Suspense fallback={<Spinner size="lg" />}>
      <ListingRoute params={params} />
    </Suspense>
  );
}
