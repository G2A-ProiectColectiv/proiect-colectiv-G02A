import { isAxiosError } from "axios";
import type { ApiError } from "@/shared/types/api";

const DEFAULT_MESSAGE = "A apărut o eroare. Încearcă din nou.";
const NETWORK_MESSAGE = "Nu am putut contacta serverul. Verifică conexiunea și încearcă din nou.";

function isApiError(data: unknown): data is ApiError {
  if (typeof data !== "object" || data === null) return false;
  const candidate = data as Record<string, unknown>;
  return typeof candidate.status === "number" && typeof candidate.message === "string";
}

/** Întoarce corpul `ApiError` al răspunsului, sau `null` dacă eroarea nu vine din API. */
export function toApiError(error: unknown): ApiError | null {
  if (!isAxiosError(error)) return null;
  const data: unknown = error.response?.data;
  return isApiError(data) ? data : null;
}

/** Codul HTTP al erorii, sau `undefined` dacă nu există răspuns (ex. serverul nu răspunde). */
export function getApiErrorStatus(error: unknown): number | undefined {
  return isAxiosError(error) ? error.response?.status : undefined;
}

/** Mesajul de arătat utilizatorului pentru o eroare de la apelul HTTP. */
export function getApiErrorMessage(error: unknown, fallback: string = DEFAULT_MESSAGE): string {
  const apiError = toApiError(error);
  if (apiError) return apiError.message;
  if (isAxiosError(error) && !error.response) return NETWORK_MESSAGE;
  return fallback;
}

/** Erorile de validare, ca hartă `câmp → mesaj` (prima eroare a fiecărui câmp). */
export function getFieldErrors(error: unknown): Record<string, string> {
  const result: Record<string, string> = {};
  for (const { field, message } of toApiError(error)?.fieldErrors ?? []) {
    if (!(field in result)) result[field] = message;
  }
  return result;
}
