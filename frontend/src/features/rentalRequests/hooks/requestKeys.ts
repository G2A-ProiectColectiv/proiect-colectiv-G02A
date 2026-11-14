import type { MyRequestsParams, RentalRequestSearchParams } from "../types";

/** Cheile de cache ale cererilor. `all` invalidează tot (listele chiriașului și, mai târziu, căutarea). */
export const requestKeys = {
  all: ["requests"] as const,
  mine: ["requests", "mine"] as const,
  mineList: (params: MyRequestsParams) => ["requests", "mine", "list", params] as const,
  search: (params: RentalRequestSearchParams) => ["requests", "search", params] as const,
  /** `viewerId` e în cheie pentru că telefonul apare doar autentificat: la login sau logout se reia cererea. */
  detail: (id: number, viewerId: number | null) => ["requests", "detail", id, viewerId] as const,
  mineDetail: (id: number) => ["requests", "mine", id] as const,
};
