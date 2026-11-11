"use client";

import Link from "next/link";
import { getApiErrorMessage, getApiErrorStatus } from "@/shared/api/apiError";
import { Button } from "@/shared/components/Button";
import { EmptyState } from "@/shared/components/EmptyState";
import { Icon } from "@/shared/components/Icon";
import { LinkButton } from "@/shared/components/LinkButton";
import { Spinner } from "@/shared/components/Spinner";
import { toast } from "@/shared/components/Toaster";
import { RequestForm } from "../components/RequestForm";
import { useMyRequest } from "../hooks/useMyRequest";
import { useUpdateRequest } from "../hooks/useUpdateRequest";
import { toFormValues, toRequestPayload, type RequestFormValues } from "../schemas/requestSchemas";
import type { RentalRequest } from "../types";
import styles from "./EditRequestView.module.css";

export interface EditRequestViewProps {
  requestId: number;
}

function centerOf(request: RentalRequest) {
  return request.centerLatitude !== null && request.centerLongitude !== null
    ? { latitude: request.centerLatitude, longitude: request.centerLongitude }
    : null;
}

export function EditRequestView({ requestId }: EditRequestViewProps) {
  const query = useMyRequest(requestId);
  const updateRequest = useUpdateRequest(requestId);

  async function handleSave(values: RequestFormValues) {
    await updateRequest.mutateAsync(toRequestPayload(values));
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
    // Pentru cererea altcuiva backendul răspunde 403, iar pentru una inexistentă sau ștearsă, 404 (CER-05).
    const status = getApiErrorStatus(query.error);
    const notOwnedOrMissing = status === 403 || status === 404 || !query.error;
    const title =
      status === 403
        ? "Nu ai acces la această cerere"
        : notOwnedOrMissing
          ? "Cererea nu a fost găsită"
          : "Nu am putut încărca cererea";

    return (
      <main className={styles.page}>
        <EmptyState
          title={title}
          description={
            notOwnedOrMissing
              ? "Poți edita doar cererile tale care nu au fost șterse."
              : getApiErrorMessage(query.error)
          }
          icon={<Icon name="alert" size={26} />}
          action={
            notOwnedOrMissing ? (
              <LinkButton href="/my-requests">Înapoi la cererile mele</LinkButton>
            ) : (
              <Button onClick={() => query.refetch()}>Încearcă din nou</Button>
            )
          }
        />
      </main>
    );
  }

  const request = query.data;

  return (
    <main className={styles.page}>
      <Link href="/my-requests" className={styles.back}>
        <Icon name="arrow-left" size={18} />
        Cererile mele
      </Link>
      <h1 className={styles.title}>Editează cererea</h1>
      <p className={styles.lead}>
        Modificările se văd imediat de proprietarii care caută chiriași.
      </p>

      <RequestForm
        defaultValues={toFormValues(request)}
        initialCoordinates={centerOf(request)}
        asideTitle="Salvează cererea"
        submitLabel="Salvează modificările"
        cancelHref="/my-requests"
        onSave={handleSave}
      />
    </main>
  );
}
