import { Suspense } from "react";
import type { Metadata } from "next";
import { EditRequestView } from "@/features/rentalRequests/views/EditRequestView";
import { Spinner } from "@/shared/components/Spinner";
import styles from "./page.module.css";

export const metadata: Metadata = { title: "Editează cererea" };

type EditRequestParams = PageProps<"/my-requests/[id]/edit">["params"];

/*
 * Cu `cacheComponents` activ, citirea parametrilor din URL se face într-un `Suspense`:
 * restul paginii (meniul, scheletul) se poate afișa imediat.
 */
async function EditRequestRoute({ params }: { params: EditRequestParams }) {
  const { id } = await params;
  return <EditRequestView requestId={Number(id)} />;
}

export default function EditRequestPage({ params }: PageProps<"/my-requests/[id]/edit">) {
  return (
    <div className={styles.page}>
      <Suspense
        fallback={
          <div className={styles.loading}>
            <Spinner size="lg" />
          </div>
        }
      >
        <EditRequestRoute params={params} />
      </Suspense>
    </div>
  );
}
