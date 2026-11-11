"use client";

import { useState } from "react";
import { getApiErrorMessage } from "@/shared/api/apiError";
import { Button } from "@/shared/components/Button";
import { EmptyState } from "@/shared/components/EmptyState";
import { Icon } from "@/shared/components/Icon";
import { LinkButton } from "@/shared/components/LinkButton";
import { Pagination } from "@/shared/components/Pagination";
import { Spinner } from "@/shared/components/Spinner";
import { Tabs, tabButtonId, tabPanelId } from "@/shared/components/Tabs";
import { MyRequestCard } from "../components/MyRequestCard";
import { REQUEST_TABS } from "../constants/requestStatuses";
import { useMyRequestCounts } from "../hooks/useMyRequestCounts";
import { useMyRequests } from "../hooks/useMyRequests";
import type { RequestStatusChoice } from "../types";
import styles from "./MyRequestsView.module.css";

const PAGE_SIZE = 10;
const TABS_ID = "my-requests";

const EMPTY_TITLES: Record<RequestStatusChoice, string> = {
  ACTIVE: "Nu ai nicio cerere activă",
  FOUND: "Nu ai nicio cerere găsită",
  CLOSED: "Nu ai nicio cerere închisă",
};

export function MyRequestsView() {
  const [status, setStatus] = useState<RequestStatusChoice>("ACTIVE");
  const [page, setPage] = useState(0);

  const requests = useMyRequests({ status, page, size: PAGE_SIZE });
  const counts = useMyRequestCounts();

  function changeTab(next: string) {
    setStatus(next as RequestStatusChoice);
    setPage(0);
  }

  function changePage(next: number) {
    setPage(next);
    window.scrollTo({ top: 0, behavior: "smooth" });
  }

  const data = requests.data;

  return (
    <main className={styles.page}>
      <div className={styles.header}>
        <h1 className={styles.title}>Cererile mele</h1>
        <LinkButton href="/my-requests/new" className={styles.newButton}>
          <Icon name="plus" size={18} />
          Cerere nouă
        </LinkButton>
      </div>

      <div className={styles.tabs}>
        <Tabs
          idPrefix={TABS_ID}
          ariaLabel="Cererile mele după status"
          activeId={status}
          onChange={changeTab}
          tabs={REQUEST_TABS.map(({ status: id, label }) => ({
            id,
            label,
            count: counts[id],
          }))}
        />
      </div>

      <div
        id={tabPanelId(TABS_ID)}
        role="tabpanel"
        aria-labelledby={tabButtonId(TABS_ID, status)}
        className={styles.panel}
        aria-busy={requests.isFetching}
      >
        {requests.isPending && (
          <div className={styles.center}>
            <Spinner size="lg" />
          </div>
        )}

        {requests.isError && (
          <EmptyState
            title="Nu am putut încărca cererile"
            description={getApiErrorMessage(requests.error)}
            icon={<Icon name="alert" size={26} />}
            action={<Button onClick={() => requests.refetch()}>Încearcă din nou</Button>}
          />
        )}

        {data && data.items.length === 0 && (
          <EmptyState
            title={EMPTY_TITLES[status]}
            description={
              page > 0
                ? "Pe această pagină nu mai sunt cereri."
                : status === "ACTIVE"
                  ? "Spune ce cauți, iar proprietarii te pot contacta dacă au ceva potrivit."
                  : undefined
            }
            icon={<Icon name="search" size={26} />}
            action={
              page > 0 ? (
                <Button variant="secondary" onClick={() => setPage(0)}>
                  Înapoi la prima pagină
                </Button>
              ) : status === "ACTIVE" ? (
                <LinkButton href="/my-requests/new">Publică o cerere</LinkButton>
              ) : undefined
            }
          />
        )}

        {data && data.items.length > 0 && (
          <>
            <ul className={styles.list}>
              {data.items.map((request) => (
                <li key={request.id}>
                  <MyRequestCard request={request} />
                </li>
              ))}
            </ul>
            <Pagination
              page={data.page}
              totalPages={data.totalPages}
              onPageChange={changePage}
              ariaLabel="Paginarea cererilor"
            />
          </>
        )}
      </div>
    </main>
  );
}
