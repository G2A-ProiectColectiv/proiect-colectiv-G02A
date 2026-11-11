/*
 * Tipurile DTO-urilor din backend pentru cererile de închiriere
 * (vezi `docs/md_files/api-endpoints.md`, secțiunea 7). Tipul de locuință și moneda
 * sunt aceleași ca la anunțuri, deci se refolosesc de acolo.
 */

import type { Currency, PropertyType } from "@/features/listings/types";

export type { Currency, PropertyType };

export type RequestStatus = "ACTIVE" | "FOUND" | "CLOSED" | "DELETED";

/** Statusurile care se pot alege la `PATCH /api/requests/{id}/status` (și care apar ca tab-uri). */
export type RequestStatusChoice = Exclude<RequestStatus, "DELETED">;

/** `RentalRequestDto`: toată cererea, cu centrul zonei extras de server din link. */
export interface RentalRequest {
  id: number;
  title: string;
  description: string;
  desiredType: PropertyType;
  budgetMin: number;
  budgetMax: number;
  currency: Currency;
  city: string;
  area: string | null;
  rooms: number | null;
  /** Dată ISO, de exemplu `2026-12-15`. */
  desiredFrom: string;
  googleMapsUrl: string | null;
  centerLatitude: number | null;
  centerLongitude: number | null;
  radiusKm: number | null;
  status: RequestStatus;
  createdAt: string;
  updatedAt: string;
}

/**
 * `RentalRequestSummaryDto`, din lista „Cererile mele" (titlu, buget, oraș, camere, perioadă, status).
 * Câmpurile marcate `?` se afișează doar dacă backendul le trimite.
 */
export interface RentalRequestSummary {
  id: number;
  title: string;
  budgetMin: number;
  budgetMax: number;
  currency: Currency;
  city: string;
  rooms: number | null;
  desiredFrom: string;
  status: RequestStatus;
  desiredType?: PropertyType;
  area?: string | null;
  createdAt?: string;
}

/**
 * Corpul cererilor `POST /api/requests` și `PUT /api/requests/{id}`. Nu conține coordonate:
 * le extrage serverul din link. `googleMapsUrl` și `radiusKm` se trimit doar împreună.
 */
export interface RentalRequestPayload {
  title: string;
  description: string;
  desiredType: PropertyType;
  budgetMin: number;
  budgetMax: number;
  currency: Currency;
  city: string;
  area: string | null;
  rooms: number | null;
  desiredFrom: string;
  googleMapsUrl: string | null;
  radiusKm: number | null;
}

export interface MyRequestsParams {
  status?: RequestStatusChoice;
  /** Numărătoarea începe de la 0. */
  page?: number;
  size?: number;
}

/** Raza zonei căutate, în km (CER-03). */
export const RADIUS_KM = { min: 1, max: 50, default: 2 } as const;
