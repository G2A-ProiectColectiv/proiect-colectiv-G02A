"use client";

import { useSyncExternalStore } from "react";
import styles from "./Toaster.module.css";

type ToastKind = "success" | "error" | "info";

interface ToastItem {
  id: number;
  kind: ToastKind;
  message: string;
}

const SUCCESS_DURATION_MS = 4000;
const ERROR_DURATION_MS = 6000;

let toasts: ToastItem[] = [];
let nextId = 1;
const listeners = new Set<() => void>();
const NO_TOASTS: ToastItem[] = [];

function emit() {
  listeners.forEach((listener) => listener());
}

function subscribe(listener: () => void) {
  listeners.add(listener);
  return () => {
    listeners.delete(listener);
  };
}

function dismiss(id: number) {
  toasts = toasts.filter((item) => item.id !== id);
  emit();
}

function show(kind: ToastKind, message: string, duration: number): number {
  const id = nextId++;
  toasts = [...toasts, { id, kind, message }];
  emit();
  setTimeout(() => dismiss(id), duration);
  return id;
}

/** Afișează o notificare. Poate fi apelat de oriunde din componente client sau hook-uri. */
export const toast = {
  success: (message: string) => show("success", message, SUCCESS_DURATION_MS),
  error: (message: string) => show("error", message, ERROR_DURATION_MS),
  info: (message: string) => show("info", message, SUCCESS_DURATION_MS),
  dismiss,
};

const ICONS: Record<ToastKind, string> = {
  success: "M5 13l4 4L19 7",
  error: "M12 8v5m0 3.5v.01",
  info: "M12 11v5m0-8.5v.01",
};

export function Toaster() {
  const items = useSyncExternalStore(
    subscribe,
    () => toasts,
    () => NO_TOASTS,
  );

  return (
    <div className={styles.region} aria-live="polite">
      {items.map((item) => (
        <div
          key={item.id}
          className={`${styles.toast} ${styles[item.kind]}`}
          role={item.kind === "error" ? "alert" : "status"}
        >
          <span className={styles.icon} aria-hidden="true">
            <svg
              width="16"
              height="16"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              strokeWidth="3"
              strokeLinecap="round"
              strokeLinejoin="round"
            >
              <path d={ICONS[item.kind]} />
            </svg>
          </span>
          <span className={styles.message}>{item.message}</span>
          <button
            type="button"
            className={styles.close}
            aria-label="Închide notificarea"
            onClick={() => dismiss(item.id)}
          >
            <svg
              width="14"
              height="14"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              strokeWidth="3"
              strokeLinecap="round"
            >
              <path d="M6 6l12 12M18 6L6 18" />
            </svg>
          </button>
        </div>
      ))}
    </div>
  );
}
