import type { z } from "zod";

/** Schema filtrelor: un obiect Zod ale cărui câmpuri sunt opționale (ex. `z.coerce.number().optional()`). */
export type FiltersSchema = z.ZodObject<z.ZodRawShape>;

export const PAGE_PARAM = "page";

/**
 * Citește filtrele din URL. Fiecare câmp se validează separat: o valoare invalidă
 * se ignoră, fără să strice restul filtrelor.
 */
export function parseFilters<S extends FiltersSchema>(
  schema: S,
  params: URLSearchParams,
): Partial<z.infer<S>> {
  const result: Record<string, unknown> = {};
  for (const [key, fieldSchema] of Object.entries(schema.shape)) {
    const raw = params.get(key);
    if (raw === null || raw === "") continue;
    const parsed = (fieldSchema as z.ZodType).safeParse(raw);
    if (parsed.success && parsed.data !== undefined) {
      result[key] = parsed.data;
    }
  }
  return result as Partial<z.infer<S>>;
}

/** Pagina din URL (minim 1); orice valoare invalidă înseamnă pagina 1. */
export function parsePage(params: URLSearchParams): number {
  const page = Number(params.get(PAGE_PARAM));
  return Number.isInteger(page) && page >= 1 ? page : 1;
}

/** Scrie filtrele și pagina într-un query string. Valorile goale nu apar; pagina 1 nu apare. */
export function buildQueryString(filters: Record<string, unknown>, page = 1): string {
  const params = new URLSearchParams();
  for (const [key, value] of Object.entries(filters)) {
    if (value === undefined || value === null || value === "") continue;
    if (Array.isArray(value)) {
      if (value.length > 0) params.set(key, value.join(","));
      continue;
    }
    params.set(key, String(value));
  }
  if (page > 1) params.set(PAGE_PARAM, String(page));
  return params.toString();
}
