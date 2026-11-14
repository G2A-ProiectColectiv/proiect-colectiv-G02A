/*
 * Tipurile DTO-urilor din backend (vezi `docs/md_files/api-endpoints.md`, secțiunea 5).
 * Enumurile sunt aceleași ca în schema bazei de date.
 */

export type PropertyType = "APARTMENT" | "STUDIO" | "ROOM" | "HOUSE" | "OTHER";

export type Amenity =
  | "FURNISHED"
  | "PARKING"
  | "BALCONY"
  | "AIR_CONDITIONING"
  | "WASHING_MACHINE"
  | "PETS_ALLOWED"
  | "INTERNET";

export type Currency = "RON" | "EUR";

export type ListingStatus = "ACTIVE" | "RENTED" | "UNAVAILABLE" | "DELETED";

/** Statusurile care se pot alege la `PATCH /api/listings/{id}/status` (și care apar ca tab-uri). */
export type ListingStatusChoice = Exclude<ListingStatus, "DELETED">;

/** `ListingImageDto`. `url` este calea relativă (`listings/12/a1b2c3.webp`); se afișează cu `imageUrl()`. */
export interface ListingImage {
  id: number;
  url: string;
  primary: boolean;
  position: number;
}

/** `ListingDto`: tot anunțul, cu coordonatele extrase de server și pozele. */
export interface Listing {
  id: number;
  title: string;
  description: string;
  type: PropertyType;
  price: number;
  currency: Currency;
  rooms: number;
  bathrooms: number;
  areaSqm: number;
  address: string;
  city: string;
  amenities: Amenity[];
  /** Dată ISO, de exemplu `2026-12-01`. */
  availableFrom: string;
  googleMapsUrl: string | null;
  latitude: number;
  longitude: number;
  status: ListingStatus;
  images: ListingImage[];
  createdAt: string;
  updatedAt: string;
}

/** Proprietarul din pagina publică. `phone` e `null` pentru vizitatorii neautentificați (DET-03). */
export interface ListingOwner {
  id: number;
  firstName: string;
  lastName: string;
  avatarUrl: string | null;
  phone: string | null;
}

/** `ListingDetailsDto`: pagina publică a unui anunț. */
export interface ListingDetails extends Listing {
  owner: ListingOwner;
}

/** `ListingSummaryDto`, din lista „Anunțurile mele". Câmpurile marcate `?` se afișează doar dacă backendul le trimite. */
export interface ListingSummary {
  id: number;
  title: string;
  price: number;
  currency: Currency;
  status: ListingStatus;
  /** Calea relativă a pozei principale, sau `null` dacă anunțul nu are poze. */
  primaryImage: string | null;
  createdAt: string;
  type?: PropertyType;
  rooms?: number;
  bathrooms?: number;
  areaSqm?: number;
  city?: string;
  availableFrom?: string;
}

/** Corpul cererilor `POST /api/listings` și `PUT /api/listings/{id}`. Nu conține coordonate: le extrage serverul. */
export interface ListingRequest {
  title: string;
  description: string;
  type: PropertyType;
  price: number;
  currency: Currency;
  rooms: number;
  bathrooms: number;
  areaSqm: number;
  address: string;
  city: string;
  amenities: Amenity[];
  availableFrom: string;
  googleMapsUrl: string;
}

export interface MyListingsParams {
  status?: ListingStatusChoice;
  /** Numărătoarea începe de la 0. */
  page?: number;
  size?: number;
}

/** Parametrii trimiși la `GET /api/listings` (căutarea publică). `page` începe de la 0. */
export interface ListingSearchParams {
  minPrice?: number;
  maxPrice?: number;
  type?: PropertyType;
  minRooms?: number;
  minBathrooms?: number;
  minArea?: number;
  maxArea?: number;
  city?: string;
  /** Dotările separate prin virgulă; anunțul trebuie să le aibă pe toate. */
  amenities?: string;
  /** Dată ISO (`2026-12-01`). */
  availableBy?: string;
  /** `câmp,asc|desc`, ex. `price,asc`. */
  sort?: string;
  page?: number;
  size?: number;
}

/** `ListingMapMarkerDto` de la `GET /api/listings/map`. */
export interface ListingMapMarker {
  id: number;
  title: string;
  price: number;
  currency: Currency;
  primaryImage: string | null;
  latitude: number;
  longitude: number;
}

export const MAX_LISTING_IMAGES = 10;
