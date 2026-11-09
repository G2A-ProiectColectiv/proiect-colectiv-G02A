import type { ComponentProps, ReactNode } from "react";
import { Spinner } from "./Spinner";
import styles from "./Button.module.css";

export type ButtonVariant = "primary" | "secondary" | "text" | "danger" | "dangerOutline";

export interface ButtonProps extends ComponentProps<"button"> {
  variant?: ButtonVariant;
  /** `sm` păstrează ținta de atingere minimă de 44 px. */
  size?: "md" | "sm";
  loading?: boolean;
  fullWidth?: boolean;
  /** Pictogramă afișată înaintea textului. */
  icon?: ReactNode;
}

export function Button({
  variant = "primary",
  size = "md",
  loading = false,
  fullWidth = false,
  icon,
  type = "button",
  className,
  disabled,
  children,
  ...rest
}: ButtonProps) {
  const classes = [
    styles.button,
    styles[variant],
    size === "sm" ? styles.sm : "",
    fullWidth ? styles.fullWidth : "",
    className ?? "",
  ]
    .filter(Boolean)
    .join(" ");

  return (
    <button
      type={type}
      className={classes}
      disabled={disabled || loading}
      aria-busy={loading || undefined}
      {...rest}
    >
      {loading ? <Spinner size="sm" tone="inherit" decorative /> : icon}
      {children}
    </button>
  );
}
