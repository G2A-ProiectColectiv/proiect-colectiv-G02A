"use client";

import { useEffect } from "react";
import styles from "./ConfirmDialog.module.css";

interface ConfirmDialogProps {
  title: string;
  description: string;
  confirmLabel: string;
  isPending?: boolean;
  password?: string;
  onPasswordChange?: (value: string) => void;
  error?: string;
  onConfirm: () => void;
  onCancel: () => void;
}

// Afiseaza un dialog pentru confirmarea unei actiuni importante.
export function ConfirmDialog({
                                title,
                                description,
                                confirmLabel,
                                isPending = false,
                                password,
                                onPasswordChange,
                                error,
                                onConfirm,
                                onCancel,
                              }: ConfirmDialogProps) {

  // Permite inchiderea dialogului cu tasta Escape.
  useEffect(() => {
    function handleKeyDown(event: KeyboardEvent) {
      if (event.key === "Escape" && !isPending) {
        onCancel();
      }
    }

    document.addEventListener("keydown", handleKeyDown);

    return () => {
      document.removeEventListener("keydown", handleKeyDown);
    };
  }, [isPending, onCancel]);

  return (
    <div className={styles.overlay}>
      <div
        className={styles.dialog}
        role="dialog"
        aria-modal="true"
        aria-labelledby="confirm-dialog-title"
        aria-describedby="confirm-dialog-description"
      >
        <h2 id="confirm-dialog-title" className={styles.title}>
          {title}
        </h2>

        <p id="confirm-dialog-description" className={styles.description}>
          {description}
        </p>

        {onPasswordChange && (
          <div className={styles.field}>
            <label htmlFor="confirm-dialog-password">
              Parola contului
            </label>

            <input
              id="confirm-dialog-password"
              type="password"
              autoComplete="current-password"
              value={password ?? ""}
              onChange={(event) => onPasswordChange(event.target.value)}
              disabled={isPending}
              className={styles.input}
            />
          </div>
        )}

        {error && (
          <p role="alert" className={styles.error}>
            {error}
          </p>
        )}

        <div className={styles.actions}>
          <button
            type="button"
            className={styles.cancelButton}
            onClick={onCancel}
            disabled={isPending}
          >
            Anulează
          </button>

          <button
            type="button"
            className={styles.confirmButton}
            onClick={onConfirm}
            disabled={isPending}
          >
            {isPending ? "Se procesează..." : confirmLabel}
          </button>
        </div>
      </div>
    </div>
  );
}
