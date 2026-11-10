"use client";

import { useEffect, useRef, useState, type FormEvent } from "react";
import { Button } from "./Button";
import { FormField } from "./FormField";
import { Input } from "./Input";
import styles from "./ConfirmDialog.module.css";

export interface ConfirmDialogField {
  label: string;
  type?: "text" | "password";
  autoComplete?: string;
  /** Eroarea afișată sub câmp (ex. „Parola introdusă nu este corectă."). */
  error?: string;
}

export interface ConfirmDialogProps {
  open: boolean;
  title: string;
  message: string;
  confirmLabel: string;
  cancelLabel?: string;
  /** `danger` pentru acțiuni care nu se pot anula (ștergeri). */
  tone?: "primary" | "danger";
  /** Se afișează un spinner pe butonul de confirmare și dialogul nu se mai poate închide. */
  loading?: boolean;
  /** Câmp opțional în dialog (ex. parola); valoarea ajunge în `onConfirm`. */
  field?: ConfirmDialogField;
  onConfirm: (fieldValue: string) => void;
  onCancel: () => void;
}

/** Dialog modal de confirmare, făcut cu elementul nativ `<dialog>` (focus și Escape vin din browser). */
export function ConfirmDialog(props: ConfirmDialogProps) {
  const { open, loading = false, onCancel } = props;
  const dialogRef = useRef<HTMLDialogElement>(null);

  useEffect(() => {
    const dialog = dialogRef.current;
    if (!dialog) return;
    if (open && !dialog.open) dialog.showModal();
    if (!open && dialog.open) dialog.close();
  }, [open]);

  return (
    <dialog
      ref={dialogRef}
      className={styles.dialog}
      aria-labelledby="confirm-dialog-title"
      aria-describedby="confirm-dialog-message"
      onCancel={(event) => {
        // Escape: închiderea o controlăm noi, ca să nu se închidă în timpul unei acțiuni.
        event.preventDefault();
        if (!loading) onCancel();
      }}
      onClick={(event) => {
        // Click pe fundalul întunecat (în afara casetei) = renunțare.
        if (event.target === dialogRef.current && !loading) onCancel();
      }}
    >
      {open && <ConfirmDialogBody {...props} />}
    </dialog>
  );
}

function ConfirmDialogBody({
  title,
  message,
  confirmLabel,
  cancelLabel = "Anulează",
  tone = "primary",
  loading = false,
  field,
  onConfirm,
  onCancel,
}: ConfirmDialogProps) {
  // Corpul se montează la deschidere și dispare la închidere, deci câmpul pornește mereu gol.
  const [value, setValue] = useState("");

  function onSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    onConfirm(value);
  }

  return (
    <form className={styles.body} onSubmit={onSubmit}>
      <h2 id="confirm-dialog-title" className={styles.title}>
        {title}
      </h2>
      <p id="confirm-dialog-message" className={styles.message}>
        {message}
      </p>

      {field && (
        <FormField label={field.label} htmlFor="confirm-dialog-field" error={field.error}>
          <Input
            id="confirm-dialog-field"
            type={field.type ?? "text"}
            autoComplete={field.autoComplete}
            invalid={!!field.error}
            aria-describedby={field.error ? "confirm-dialog-field-error" : undefined}
            value={value}
            onChange={(event) => setValue(event.target.value)}
          />
        </FormField>
      )}

      <div className={styles.actions}>
        <Button variant="secondary" disabled={loading} onClick={onCancel}>
          {cancelLabel}
        </Button>
        <Button type="submit" variant={tone === "danger" ? "danger" : "primary"} loading={loading}>
          {confirmLabel}
        </Button>
      </div>
    </form>
  );
}
