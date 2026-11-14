"use client";

import { ResultsCount } from "@/features/search/components/ResultsCount";
import { SortSelect } from "@/features/search/components/SortSelect";
import { ViewToggle } from "@/features/search/components/ViewToggle";
import { useUrlFilters } from "@/features/search/hooks/useUrlFilters";
import { ListingFilters } from "../components/ListingFilters";
import { ListingResultsList } from "../components/ListingResultsList";
import { ListingResultsMap } from "../components/ListingResultsMap";
import { useListingSearch } from "../hooks/useListingSearch";
import { useListingsInBounds } from "../hooks/useListingsInBounds";
import { LISTING_SORT_OPTIONS, listingSearchSchema } from "../schemas/listingSearchSchema";
import styles from "./ListingSearchView.module.css";

const SORT_OPTIONS = LISTING_SORT_OPTIONS.map((option) => ({ ...option }));
/** Cât întoarce cel mult serverul pentru hartă (HARTA-04). */
const MAP_MARKER_LIMIT = 500;

export function ListingSearchView() {
  const { filters, page, setFilters, setPage, resetFilters } = useUrlFilters(listingSearchSchema);
  const view = filters.view ?? "list";
  const isMap = view === "map";

  // Fiecare vedere își cere datele doar când e afișată.
  const search = useListingSearch(filters, page, !isMap);
  const map = useListingsInBounds(filters, isMap);

  function changePage(next: number) {
    setPage(next);
    window.scrollTo({ top: 0, behavior: "smooth" });
  }

  const markers = map.data ?? [];

  return (
    <main className={styles.page}>
      <h1 className={styles.title}>Caută locuințe</h1>

      <div className={styles.layout}>
        <ListingFilters filters={filters} onApply={setFilters} onReset={resetFilters} />

        <section className={styles.results} aria-label="Rezultate">
          <div className={styles.toolbar}>
            {!isMap && search.data && (
              <ResultsCount total={search.data.totalElements} noun={["anunț", "anunțuri"]} />
            )}
            {isMap && map.data && (
              <ResultsCount total={markers.length} noun={["anunț în zonă", "anunțuri în zonă"]} />
            )}
            <div className={styles.toolbarActions}>
              {!isMap && (
                <SortSelect
                  options={SORT_OPTIONS}
                  value={filters.sort}
                  onChange={(sort) => setFilters({ sort })}
                />
              )}
              <ViewToggle
                view={view}
                onChange={(next) => setFilters({ view: next === "list" ? undefined : next })}
              />
            </div>
          </div>

          {isMap ? (
            <>
              <ListingResultsMap
                markers={markers}
                onBoundsChange={map.setBounds}
                isFetching={map.isFetching}
              />
              {markers.length >= MAP_MARKER_LIMIT && (
                <p className={styles.note}>
                  Se afișează primele {MAP_MARKER_LIMIT} anunțuri din zonă. Apropie harta sau
                  adaugă filtre pentru a le vedea pe celelalte.
                </p>
              )}
              {map.isError && (
                <p className={styles.note} role="alert">
                  Nu am putut încărca anunțurile din această zonă.
                </p>
              )}
            </>
          ) : (
            <ListingResultsList
              data={search.data}
              isPending={search.isPending}
              isFetching={search.isFetching}
              error={search.error}
              onRetry={() => search.refetch()}
              page={page}
              onPageChange={changePage}
              onResetFilters={resetFilters}
            />
          )}
        </section>
      </div>
    </main>
  );
}
