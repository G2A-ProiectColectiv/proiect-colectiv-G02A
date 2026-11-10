import { Icon } from "./Icon";
import styles from "./Pagination.module.css";

export interface PaginationProps {
  /** Pagina curentă; numărătoarea începe de la 0, ca în API. */
  page: number;
  totalPages: number;
  onPageChange: (page: number) => void;
  ariaLabel?: string;
}

type PageItem = number | "gap-start" | "gap-end";

/** Prima și ultima pagină, pagina curentă și vecinele ei; restul devin „…". */
function buildItems(page: number, totalPages: number): PageItem[] {
  const wanted = new Set([0, totalPages - 1, page - 1, page, page + 1]);
  const pages = [...wanted].filter((p) => p >= 0 && p < totalPages).sort((a, b) => a - b);

  const items: PageItem[] = [];
  pages.forEach((p, index) => {
    const previous = pages[index - 1];
    if (previous !== undefined && p - previous > 1) {
      items.push(index === 1 ? "gap-start" : "gap-end");
    }
    items.push(p);
  });
  return items;
}

export function Pagination({
  page,
  totalPages,
  onPageChange,
  ariaLabel = "Paginare",
}: PaginationProps) {
  if (totalPages <= 1) return null;

  return (
    <nav className={styles.pagination} aria-label={ariaLabel}>
      <button
        type="button"
        className={styles.item}
        aria-label="Pagina anterioară"
        disabled={page <= 0}
        onClick={() => onPageChange(page - 1)}
      >
        <Icon name="chevron-left" size={18} />
      </button>

      {buildItems(page, totalPages).map((item) =>
        typeof item === "number" ? (
          <button
            key={item}
            type="button"
            className={`${styles.item} ${item === page ? styles.current : ""}`}
            aria-label={`Pagina ${item + 1}`}
            aria-current={item === page ? "page" : undefined}
            onClick={() => onPageChange(item)}
          >
            {item + 1}
          </button>
        ) : (
          <span key={item} className={styles.gap} aria-hidden="true">
            …
          </span>
        ),
      )}

      <button
        type="button"
        className={styles.item}
        aria-label="Pagina următoare"
        disabled={page >= totalPages - 1}
        onClick={() => onPageChange(page + 1)}
      >
        <Icon name="chevron-right" size={18} />
      </button>
    </nav>
  );
}
