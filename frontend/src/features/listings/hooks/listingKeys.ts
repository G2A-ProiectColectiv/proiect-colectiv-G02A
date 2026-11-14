import type { ListingSearchParams, MyListingsParams } from "../types";

/** Cheile de cache ale anunțurilor. `all` invalidează tot (listele proprietarului și, mai târziu, căutarea publică). */
export const listingKeys = {
  all: ["listings"] as const,
  mine: ["listings", "mine"] as const,
  mineList: (params: MyListingsParams) => ["listings", "mine", "list", params] as const,
  search: (params: ListingSearchParams) => ["listings", "search", params] as const,
  map: (params: ListingSearchParams, bbox: string) => ["listings", "map", bbox, params] as const,
  /** `viewerId` e în cheie pentru că telefonul apare doar autentificat: la login sau logout se reia cererea. */
  detail: (id: number, viewerId: number | null) => ["listings", "detail", id, viewerId] as const,
  mineDetail: (id: number) => ["listings", "mine", id] as const,
};
