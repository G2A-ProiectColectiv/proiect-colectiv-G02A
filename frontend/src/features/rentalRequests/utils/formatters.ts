import { formatPrice } from "@/features/listings/utils/formatters";
import type { Currency } from "../types";

const numberFormat = new Intl.NumberFormat("ro-RO", { maximumFractionDigits: 2 });

/** „450–600 €", „1.000–1.400 RON"; un singur număr dacă minimul e egal cu maximul. */
export function formatBudget(budgetMin: number, budgetMax: number, currency: Currency): string {
  if (budgetMin === budgetMax) return formatPrice(budgetMax, currency);
  return `${numberFormat.format(budgetMin)}–${formatPrice(budgetMax, currency)}`;
}

/** „București, Titan" sau doar „București". */
export function formatPlace(city: string, area?: string | null): string {
  return area ? `${city}, ${area}` : city;
}
