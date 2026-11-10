"use client";

import { useCallback, useEffect, useRef } from "react";
import { useMutation } from "@tanstack/react-query";
import { parseLocation } from "../api/locationApi";
import type { Coordinates } from "../types";

/** Cât așteptăm după ultima modificare a câmpului, ca să nu trimitem o cerere la fiecare literă lipită. */
const DEBOUNCE_MS = 500;

export interface ParseCallbacks {
  onSuccess?: (coordinates: Coordinates) => void;
  onError?: () => void;
}

/** Extragerea coordonatelor din linkul Google Maps, cu întârziere de 500 ms după ultima modificare. */
export function useParseLocation() {
  const { mutate, reset, data, error, isPending } = useMutation({ mutationFn: parseLocation });
  const timer = useRef<ReturnType<typeof setTimeout> | null>(null);

  const cancel = useCallback(() => {
    if (timer.current) clearTimeout(timer.current);
    timer.current = null;
  }, []);

  /** Programează extragerea; un apel nou anulează apelul programat anterior. */
  const schedule = useCallback(
    (url: string, callbacks?: ParseCallbacks) => {
      cancel();
      reset();

      const trimmed = url.trim();
      if (!trimmed) return;

      timer.current = setTimeout(() => {
        timer.current = null;
        mutate(trimmed, callbacks);
      }, DEBOUNCE_MS);
    },
    [cancel, reset, mutate],
  );

  // La demontare nu lăsăm un apel programat să pornească.
  useEffect(() => cancel, [cancel]);

  return { schedule, cancel, coordinates: data ?? null, error, isPending };
}
