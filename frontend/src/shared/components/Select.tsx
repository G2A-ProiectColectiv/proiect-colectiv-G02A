import type { ComponentProps } from "react";
import styles from "./Select.module.css";

export interface SelectProps extends ComponentProps<"select"> {
  /** Marchează câmpul ca invalid (chenar roșu). */
  invalid?: boolean;
}

/** Listă derulantă nativă, stilizată ca `Input`. Opțiunile se dau ca `<option>`. */
export function Select({ invalid = false, className, children, ...rest }: SelectProps) {
  const classes = [styles.select, invalid ? styles.invalid : "", className ?? ""]
    .filter(Boolean)
    .join(" ");

  return (
    <select className={classes} aria-invalid={invalid || undefined} {...rest}>
      {children}
    </select>
  );
}
