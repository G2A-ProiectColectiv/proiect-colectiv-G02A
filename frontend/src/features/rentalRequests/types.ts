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
  /** Apare în căutarea proprietarilor (CAUT-06), nu și în „Cererile mele". */
  tenant?: RequestTenantSummary;
}

/** Chiriașul de pe cardul cererii: doar numele și poza. */
export interface RequestTenantSummary {
  id: number;
  firstName: string;
  lastName: string;
  avatarUrl: string | null;
}

/** Chiriașul din detaliile cererii; proprietarul autentificat vede și telefonul. */
export interface RequestTenant extends RequestTenantSummary {
  phone: string | null;
}

/** `RentalRequestDetailsDto`: cererea văzută de un proprietar (DET-05). */
export interface RentalRequestDetails extends RentalRequest {
  tenant: RequestTenant;
}

/** Parametrii trimiși la `GET /api/requests` (căutarea proprietarilor). `page` începe de la 0. */
export interface RentalRequestSearchParams {
  type?: PropertyType;
  minBudget?: number;
  maxBudget?: number;
  city?: string;
  area?: string;
  minRooms?: number;
  /** Dată ISO (`2026-12-01`). */
  desiredFrom?: string;
  /** `câmp,asc|desc`, ex. `budgetMax,desc`. */
  sort?: string;
  page?: number;
  size?: number;
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
