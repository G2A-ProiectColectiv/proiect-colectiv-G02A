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
import { MyListingCard } from "../components/MyListingCard";
import { LISTING_TABS } from "../constants/listingStatuses";
import { useMyListingCounts } from "../hooks/useMyListingCounts";
import { useMyListings } from "../hooks/useMyListings";
import type { ListingStatusChoice } from "../types";
import styles from "./MyListingsView.module.css";

const PAGE_SIZE = 10;
const TABS_ID = "my-listings";

const EMPTY_TITLES: Record<ListingStatusChoice, string> = {
  ACTIVE: "Nu ai niciun anunț activ",
  RENTED: "Nu ai niciun anunț închiriat",
  UNAVAILABLE: "Nu ai niciun anunț indisponibil",
};

export function MyListingsView() {
  const [status, setStatus] = useState<ListingStatusChoice>("ACTIVE");
  const [page, setPage] = useState(0);

  const listings = useMyListings({ status, page, size: PAGE_SIZE });
  const counts = useMyListingCounts();

  function changeTab(next: string) {
    setStatus(next as ListingStatusChoice);
    setPage(0);
  }

  function changePage(next: number) {
    setPage(next);
    window.scrollTo({ top: 0, behavior: "smooth" });
  }

  const data = listings.data;

  return (
    <main className={styles.page}>
      <div className={styles.header}>
        <h1 className={styles.title}>Anunțurile mele</h1>
        <LinkButton href="/my-listings/new" className={styles.newButton}>
          <Icon name="plus" size={18} />
          Anunț nou
        </LinkButton>
      </div>

      <div className={styles.tabs}>
        <Tabs
          idPrefix={TABS_ID}
          ariaLabel="Anunțurile mele după status"
          activeId={status}
          onChange={changeTab}
          tabs={LISTING_TABS.map(({ status: id, label }) => ({
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
        aria-busy={listings.isFetching}
      >
        {listings.isPending && (
          <div className={styles.center}>
            <Spinner size="lg" />
          </div>
        )}

        {listings.isError && (
          <EmptyState
            title="Nu am putut încărca anunțurile"
            description={getApiErrorMessage(listings.error)}
            icon={<Icon name="alert" size={26} />}
            action={<Button onClick={() => listings.refetch()}>Încearcă din nou</Button>}
          />
        )}

        {data && data.items.length === 0 && (
          <EmptyState
            title={EMPTY_TITLES[status]}
            description={
              page > 0
                ? "Pe această pagină nu mai sunt anunțuri."
                : status === "ACTIVE"
                  ? "Publică un anunț ca chiriașii să îl poată găsi."
                  : undefined
            }
            icon={<Icon name="home" size={26} />}
            action={
              page > 0 ? (
                <Button variant="secondary" onClick={() => setPage(0)}>
                  Înapoi la prima pagină
                </Button>
              ) : status === "ACTIVE" ? (
                <LinkButton href="/my-listings/new">Creează anunț</LinkButton>
              ) : undefined
            }
          />
        )}

        {data && data.items.length > 0 && (
          <>
            <ul className={styles.list}>
              {data.items.map((listing) => (
                <li key={listing.id}>
                  <MyListingCard listing={listing} />
                </li>
              ))}
            </ul>
            <Pagination
              page={data.page}
              totalPages={data.totalPages}
              onPageChange={changePage}
              ariaLabel="Paginarea anunțurilor"
            />
          </>
        )}
      </div>
    </main>
  );
}
