import styles from "./Spinner.module.css";

export interface SpinnerProps {
  size?: "sm" | "md" | "lg";
  /** `primary` folosește culoarea principală, `inherit` culoarea textului din jur. */
  tone?: "primary" | "inherit";
  /** Text pentru cititoarele de ecran. */
  label?: string;
  /** Când e într-un element care are deja text (ex. un buton), îl ascundem din accesibilitate. */
  decorative?: boolean;
}

export function Spinner({
  size = "md",
  tone = "primary",
  label = "Se încarcă",
  decorative = false,
}: SpinnerProps) {
  const className = [styles.spinner, styles[size], tone === "inherit" ? styles.inherit : ""]
    .filter(Boolean)
    .join(" ");

  if (decorative) return <span className={className} aria-hidden="true" />;
  return <span className={className} role="status" aria-label={label} />;
}
