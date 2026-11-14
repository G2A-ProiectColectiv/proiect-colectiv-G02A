"use client";

import { ResultsCount } from "@/features/search/components/ResultsCount";
import { SortSelect } from "@/features/search/components/SortSelect";
import { useUrlFilters } from "@/features/search/hooks/useUrlFilters";
import { getApiErrorMessage } from "@/shared/api/apiError";
import { Button } from "@/shared/components/Button";
import { EmptyState } from "@/shared/components/EmptyState";
import { Icon } from "@/shared/components/Icon";
import { Pagination } from "@/shared/components/Pagination";
import { Spinner } from "@/shared/components/Spinner";
import { RequestCard } from "../components/RequestCard";
import { RequestFilters } from "../components/RequestFilters";
import { useRequestSearch } from "../hooks/useRequestSearch";
import { REQUEST_SORT_OPTIONS, requestSearchSchema } from "../schemas/requestSearchSchema";
import styles from "./RequestSearchView.module.css";

const SORT_OPTIONS = REQUEST_SORT_OPTIONS.map((option) => ({ ...option }));

export function RequestSearchView() {
  const { filters, page, setFilters, setPage, resetFilters } = useUrlFilters(requestSearchSchema);
  const search = useRequestSearch(filters, page);
  const data = search.data;

  function changePage(next: number) {
    setPage(next);
    window.scrollTo({ top: 0, behavior: "smooth" });
  }

  return (
    <main className={styles.page}>
      <h1 className={styles.title}>Cereri de la chiriași</h1>

      <div className={styles.layout}>
        <RequestFilters filters={filters} onApply={setFilters} onReset={resetFilters} />

        <section className={styles.results} aria-label="Rezultate">
          <div className={styles.toolbar}>
            {data && <ResultsCount total={data.totalElements} noun={["cerere", "cereri"]} />}
            <SortSelect
              options={SORT_OPTIONS}
              value={filters.sort}
              onChange={(sort) => setFilters({ sort })}
            />
          </div>

          {search.isPending && (
            <div className={styles.center}>
              <Spinner size="lg" />
            </div>
          )}

          {search.isError && (
            <EmptyState
              title="Nu am putut încărca cererile"
              description={getApiErrorMessage(search.error)}
              icon={<Icon name="alert" size={26} />}
              action={<Button onClick={() => search.refetch()}>Încearcă din nou</Button>}
            />
          )}

          {data && data.items.length === 0 && (
            <EmptyState
              title="Nu am găsit nicio cerere"
              description="Nicio cerere nu corespunde filtrelor alese. Încearcă să le relaxezi sau să le resetezi."
              icon={<Icon name="search" size={26} />}
              action={
                <Button variant="secondary" onClick={resetFilters}>
                  Resetează filtrele
                </Button>
              }
            />
          )}

          {data && data.items.length > 0 && (
            <div className={styles.listWrap} aria-busy={search.isFetching}>
              <ul className={styles.grid}>
                {data.items.map((request) => (
                  <li key={request.id}>
                    <RequestCard request={request} />
                  </li>
                ))}
              </ul>
              <Pagination
                page={page - 1}
                totalPages={data.totalPages}
                onPageChange={(next) => changePage(next + 1)}
                ariaLabel="Paginarea cererilor"
              />
            </div>
          )}
        </section>
      </div>
    </main>
  );
}
