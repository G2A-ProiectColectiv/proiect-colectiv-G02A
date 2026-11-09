import type { ReactNode } from "react";
import { Icon } from "./Icon";
import styles from "./FormField.module.css";

export interface FormFieldProps {
  label: string;
  /** `id`-ul câmpului din interior; leagă eticheta de câmp. */
  htmlFor: string;
  hint?: string;
  error?: string;
  required?: boolean;
  /** Element afișat în dreapta etichetei (ex. linkul „Ai uitat parola?"). */
  labelAction?: ReactNode;
  className?: string;
  children: ReactNode;
}

/**
 * Etichetă + câmp + indiciu sau eroare. Textul de ajutor are `id="{htmlFor}-hint"`,
 * iar eroarea `id="{htmlFor}-error"`, ca să le poți lega prin `aria-describedby`.
 */
export function FormField({
  label,
  htmlFor,
  hint,
  error,
  required = false,
  labelAction,
  className,
  children,
}: FormFieldProps) {
  return (
    <div className={[styles.field, className ?? ""].filter(Boolean).join(" ")}>
      <div className={styles.labelRow}>
        <label htmlFor={htmlFor} className={styles.label}>
          {label}
          {required && (
            <span className={styles.required} aria-hidden="true">
              {" "}
              *
            </span>
          )}
        </label>
        {labelAction}
      </div>
      {children}
      {error ? (
        <p id={`${htmlFor}-error`} className={styles.error} role="alert">
          <Icon name="alert" size={16} />
          {error}
        </p>
      ) : hint ? (
        <p id={`${htmlFor}-hint`} className={styles.hint}>
          {hint}
        </p>
      ) : null}
    </div>
  );
}
