import type { ComponentProps } from "react";
import styles from "./Input.module.css";

export interface InputProps extends ComponentProps<"input"> {
  /** Marchează câmpul ca invalid (chenar roșu). */
  invalid?: boolean;
}

export function Input({ invalid = false, className, ...rest }: InputProps) {
  const classes = [styles.input, invalid ? styles.invalid : "", className ?? ""]
    .filter(Boolean)
    .join(" ");

  return <input className={classes} aria-invalid={invalid || undefined} {...rest} />;
}
