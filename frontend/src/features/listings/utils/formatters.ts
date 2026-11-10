import type { Currency } from "../types";

const CURRENCY_SYMBOLS: Record<Currency, string> = { RON: "RON", EUR: "€" };

const numberFormat = new Intl.NumberFormat("ro-RO", { maximumFractionDigits: 2 });
const dateFormat = new Intl.DateTimeFormat("ro-RO", {
  day: "numeric",
  month: "short",
  year: "numeric",
});

/** „450 €", „1.100 RON" */
export function formatPrice(price: number, currency: Currency): string {
  return `${numberFormat.format(price)} ${CURRENCY_SYMBOLS[currency]}`;
}

/** „54 m²" */
export function formatArea(areaSqm: number): string {
  return `${numberFormat.format(areaSqm)} m²`;
}

/** „2 camere", „1 cameră" */
export function formatRooms(rooms: number): string {
  return rooms === 1 ? "1 cameră" : `${rooms} camere`;
}

/** „2 băi", „1 baie" */
export function formatBathrooms(bathrooms: number): string {
  return bathrooms === 1 ? "1 baie" : `${bathrooms} băi`;
}

/** „3 nov. 2026" pentru o dată sau un moment ISO. */
export function formatDate(iso: string): string {
  // O dată fără oră (`2026-12-01`) o citim ca dată locală, ca să nu alunece o zi în urmă.
  const date = /^\d{4}-\d{2}-\d{2}$/.test(iso) ? new Date(`${iso}T00:00:00`) : new Date(iso);
  return Number.isNaN(date.getTime()) ? "" : dateFormat.format(date);
}
