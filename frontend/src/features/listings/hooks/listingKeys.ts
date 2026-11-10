import type { MyListingsParams } from "../types";

/** Cheile de cache ale anunțurilor. `all` invalidează tot (listele proprietarului și, mai târziu, căutarea publică). */
export const listingKeys = {
  all: ["listings"] as const,
  mine: ["listings", "mine"] as const,
  mineList: (params: MyListingsParams) => ["listings", "mine", "list", params] as const,
  mineDetail: (id: number) => ["listings", "mine", id] as const,
};
