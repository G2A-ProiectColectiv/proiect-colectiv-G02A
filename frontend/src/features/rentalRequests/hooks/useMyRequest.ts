"use client";

import { useQuery } from "@tanstack/react-query";
import { getMineById } from "../api/rentalRequestsApi";
import { requestKeys } from "./requestKeys";

/** O cerere a chiriașului, cu centrul zonei și raza (pentru editare). */
export function useMyRequest(id: number) {
  return useQuery({
    queryKey: requestKeys.mineDetail(id),
    queryFn: () => getMineById(id),
    enabled: Number.isInteger(id) && id > 0,
    // Un 403 sau 404 nu se rezolvă singur; nu are rost să mai încercăm.
    retry: false,
  });
}
