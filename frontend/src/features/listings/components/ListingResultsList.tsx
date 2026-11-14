import { getApiErrorMessage } from "@/shared/api/apiError";
import { Button } from "@/shared/components/Button";
import { EmptyState } from "@/shared/components/EmptyState";
import { Icon } from "@/shared/components/Icon";
import { Pagination } from "@/shared/components/Pagination";
import { Spinner } from "@/shared/components/Spinner";
import type { PageResponse } from "@/shared/types/api";
import type { ListingSummary } from "../types";
import { ListingCard } from "./ListingCard";
import styles from "./ListingResultsList.module.css";

export interface ListingResultsListProps {
  data: PageResponse<ListingSummary> | undefined;
  isPending: boolean;
  isFetching: boolean;
  error: unknown;
  onRetry: () => void;
  /** Pagina curentă, numărată de la 1 (ca în URL). */
  page: number;
  onPageChange: (page: number) => void;
  onResetFilters: () => void;
}

export function ListingResultsList({
  data,
  isPending,
  isFetching,
  error,
  onRetry,
  page,
  onPageChange,
  onResetFilters,
}: ListingResultsListProps) {
  if (isPending) {
    return (
      <div className={styles.center}>
        <Spinner size="lg" />
      </div>
    );
  }

  if (error || !data) {
    return (
      <EmptyState
        title="Nu am putut încărca anunțurile"
        description={getApiErrorMessage(error)}
        icon={<Icon name="alert" size={26} />}
        action={<Button onClick={onRetry}>Încearcă din nou</Button>}
      />
    );
  }

  // CAUT-07: mesaj explicit când filtrele nu dau niciun rezultat.
  if (data.items.length === 0) {
    return (
      <EmptyState
        title="Nu am găsit niciun anunț"
        description="Niciun anunț nu corespunde filtrelor alese. Încearcă să le relaxezi sau să le resetezi."
        icon={<Icon name="search" size={26} />}
        action={
          <Button variant="secondary" onClick={onResetFilters}>
            Resetează filtrele
          </Button>
        }
      />
    );
  }

  return (
    <div className={styles.results} aria-busy={isFetching}>
      <ul className={styles.grid}>
        {data.items.map((listing) => (
          <li key={listing.id}>
            <ListingCard listing={listing} />
          </li>
        ))}
      </ul>
      <Pagination
        page={page - 1}
        totalPages={data.totalPages}
        onPageChange={(next) => onPageChange(next + 1)}
        ariaLabel="Paginarea rezultatelor"
      />
    </div>
  );
}
