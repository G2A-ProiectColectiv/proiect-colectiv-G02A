import Link from "next/link";
import type { ComponentProps } from "react";
import styles from "./LinkButton.module.css";

export type LinkButtonVariant = "primary" | "accent" | "text" | "textOnDark";

export interface LinkButtonProps extends ComponentProps<typeof Link> {
  variant?: LinkButtonVariant;
}

/** Link care arată ca un buton (pentru navigare; pentru acțiuni folosește `Button`). */
export function LinkButton({ variant = "primary", className, ...rest }: LinkButtonProps) {
  const classes = [styles.link, styles[variant], className ?? ""].filter(Boolean).join(" ");
  return <Link className={classes} {...rest} />;
}
