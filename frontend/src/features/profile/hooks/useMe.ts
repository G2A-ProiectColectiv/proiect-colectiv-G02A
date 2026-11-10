"use client";

import {
  useQuery
} from "@tanstack/react-query";
import {
  getMe
} from "../api/userApi";

// Obtine datele utilizatorului autentificat.
export function useMe() {
  return useQuery({
    queryKey: ["me"],
    queryFn: getMe,
    retry: false,
  });
}
