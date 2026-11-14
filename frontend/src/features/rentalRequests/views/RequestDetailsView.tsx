"use client";

import Link from "next/link";
import { notFound } from "next/navigation";
import { useAuth } from "@/features/auth/context/AuthContext";
import { getApiErrorMessage, getApiErrorStatus } from "@/shared/api/apiError";
import { Avatar } from "@/shared/components/Avatar";
import { Button } from "@/shared/components/Button";
import { EmptyState } from "@/shared/components/EmptyState";
import { Icon } from "@/shared/components/Icon";
import { Spinner } from "@/shared/components/Spinner";
import { RequestDetails } from "../components/RequestDetails";
import { useRequest } from "../hooks/useRequest";
import styles from "./RequestDetailsView.module.css";

export interface RequestDetailsViewProps {
  requestId: number;
}

export function RequestDetailsView({ requestId }: RequestDetailsViewProps) {
  const query = useRequest(requestId);
  const { user, isReady } = useAuth();

  if (!Number.isInteger(requestId) || requestId <= 0 || getApiErrorStatus(query.error) === 404) {
    notFound();
  }

  if (query.isPending) {
    return (
      <main className={styles.center}>
        <Spinner size="lg" />
      </main>
    );
  }

  if (query.isError) {
    return (
      <main className={styles.center}>
        <EmptyState
          title="Nu am putut încărca cererea"
          description={getApiErrorMessage(query.error)}
          icon={<Icon name="alert" size={26} />}
          action={<Button onClick={() => query.refetch()}>Încearcă din nou</Button>}
        />
      </main>
    );
  }

  const request = query.data;
  const { tenant } = request;

  return (
    <main className={styles.page}>
      <div className={styles.layout}>
        <RequestDetails request={request} />

        <aside className={styles.side}>
          <section className={styles.tenant} aria-label="Chiriaș">
            <Avatar
              firstName={tenant.firstName}
              lastName={tenant.lastName}
              avatarUrl={tenant.avatarUrl}
              size="lg"
            />
            <div className={styles.info}>
              <p className={styles.label}>Chiriaș</p>
              <p className={styles.name}>
                {tenant.firstName} {tenant.lastName}
              </p>
              {isReady && user && tenant.phone && (
                <a className={styles.phone} href={`tel:${tenant.phone}`}>
                  {tenant.phone}
                </a>
              )}
              {isReady && user && !tenant.phone && (
                <p className={styles.label}>Chiriașul nu a adăugat un număr de telefon.</p>
              )}
              {isReady && !user && (
                <Link className={styles.phone} href={`/login?next=/requests/${request.id}`}>
                  Autentifică-te pentru a vedea telefonul
                </Link>
              )}
            </div>
          </section>
        </aside>
      </div>
    </main>
  );
}
