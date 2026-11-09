import type { ReactNode } from "react";
import styles from "./Icon.module.css";

export type IconName =
  "home" | "key" | "search" | "pin" | "check" | "alert" | "sliders" | "user" | "arrow-right";

const SHAPES: Record<IconName, ReactNode> = {
  home: <path d="M3 10.5 12 3l9 7.5V20a1 1 0 0 1-1 1h-5v-6H9v6H4a1 1 0 0 1-1-1z" />,
  // Aceeași cheie ca în favicon (src/app/icon.svg), în diagonală.
  key: (
    <g transform="rotate(-45 12 12)">
      <circle cx="7.9" cy="12" r="3" />
      <path d="M10.9 12h8.2M16.1 12v3M18.8 12v2.6" />
    </g>
  ),
  search: (
    <>
      <circle cx="11" cy="11" r="7" />
      <path d="m20 20-3.5-3.5" />
    </>
  ),
  pin: (
    <>
      <path d="M12 21s-7-6.2-7-11a7 7 0 0 1 14 0c0 4.8-7 11-7 11z" />
      <circle cx="12" cy="10" r="2.5" />
    </>
  ),
  check: <path d="m5 13 4 4L19 7" />,
  alert: (
    <>
      <circle cx="12" cy="12" r="9" />
      <path d="M12 7.5v5.5M12 16.5v.01" />
    </>
  ),
  sliders: (
    <>
      <path d="M4 7h10M18 7h2M4 17h2M10 17h10" />
      <circle cx="16" cy="7" r="2" />
      <circle cx="8" cy="17" r="2" />
    </>
  ),
  user: (
    <>
      <circle cx="12" cy="8" r="4" />
      <path d="M4 21c0-4.4 3.6-7 8-7s8 2.6 8 7" />
    </>
  ),
  "arrow-right": <path d="M5 12h14m-6-6 6 6-6 6" />,
};

export interface IconProps {
  name: IconName;
  size?: number;
  className?: string;
}

/** Pictogramă decorativă (ascunsă cititoarelor de ecran); textul din jur dă înțelesul. */
export function Icon({ name, size = 20, className }: IconProps) {
  return (
    <svg
      className={[styles.icon, className ?? ""].filter(Boolean).join(" ")}
      width={size}
      height={size}
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="2"
      strokeLinecap="round"
      strokeLinejoin="round"
      aria-hidden="true"
      focusable="false"
    >
      {SHAPES[name]}
    </svg>
  );
}
