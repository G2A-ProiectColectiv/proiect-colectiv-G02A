import type { MyRequestsParams } from "../types";

/** Cheile de cache ale cererilor. `all` invalidează tot (listele chiriașului și, mai târziu, căutarea). */
export const requestKeys = {
  all: ["requests"] as const,
  mine: ["requests", "mine"] as const,
  mineList: (params: MyRequestsParams) => ["requests", "mine", "list", params] as const,
  mineDetail: (id: number) => ["requests", "mine", id] as const,
};
