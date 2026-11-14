"use client";

import { useQuery } from "@tanstack/react-query";
import { useAuth } from "@/features/auth/context/AuthContext";
import { getRequestDetails } from "../api/requestSearchApi";
import { requestKeys } from "./requestKeys";

/** Detaliile unei cereri (DET-05); telefonul chiriașului vine doar pentru utilizatorii autentificați. */
export function useRequest(id: number) {
  const { user } = useAuth();

  return useQuery({
    queryKey: requestKeys.detail(id, user?.id ?? null),
    queryFn: () => getRequestDetails(id),
    enabled: Number.isInteger(id) && id > 0,
    // Un 404 nu se rezolvă singur.
    retry: false,
  });
}
