/** Răspuns paginat de pe server (`PageResponse<T>` din backend). `page` începe de la 0. */
export interface PageResponse<T> {
  items: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

/** O eroare de validare pe un câmp (`FieldErrorDto` din backend). */
export interface FieldError {
  field: string;
  message: string;
}

/** Formatul unic de eroare întors de API (`ApiError` din backend). */
export interface ApiError {
  status: number;
  message: string;
  path: string;
  timestamp: string;
  /** Apare doar la erorile de validare (400). */
  fieldErrors?: FieldError[];
}
