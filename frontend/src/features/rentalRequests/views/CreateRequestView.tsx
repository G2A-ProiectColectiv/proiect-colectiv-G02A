"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { Icon } from "@/shared/components/Icon";
import { toast } from "@/shared/components/Toaster";
import { RequestForm } from "../components/RequestForm";
import { useCreateRequest } from "../hooks/useCreateRequest";
import { toRequestPayload, type RequestFormValues } from "../schemas/requestSchemas";
import styles from "./CreateRequestView.module.css";

export function CreateRequestView() {
  const router = useRouter();
  const createRequest = useCreateRequest();

  async function handleSave(values: RequestFormValues) {
    await createRequest.mutateAsync(toRequestPayload(values));
    toast.success("Cererea a fost publicată.");
    router.push("/my-requests");
  }

  return (
    <main className={styles.page}>
      <Link href="/my-requests" className={styles.back}>
        <Icon name="arrow-left" size={18} />
        Cererile mele
      </Link>
      <h1 className={styles.title}>Spune ce cauți</h1>
      <p className={styles.lead}>
        Proprietarii pot vedea cererea ta și te pot contacta dacă au ceva potrivit.
      </p>

      <RequestForm
        asideTitle="Publică cererea"
        submitLabel="Publică cererea"
        cancelHref="/my-requests"
        onSave={handleSave}
      />
    </main>
  );
}
