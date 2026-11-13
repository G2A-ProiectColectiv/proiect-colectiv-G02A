"use client";

import { useState } from "react";
import { Icon } from "./Icon";
import { Input, type InputProps } from "./Input";
import styles from "./PasswordInput.module.css";

export type PasswordInputProps = Omit<InputProps, "type">;

/**
 * Câmp de parolă cu buton „arată / ascunde parola". Primește aceleași proprietăți ca `Input`
 * (inclusiv `ref` și `register(...)` din React Hook Form), fără `type`.
 */
export function PasswordInput({ className, disabled, ...rest }: PasswordInputProps) {
  const [visible, setVisible] = useState(false);

  return (
    <div className={styles.wrap}>
      <Input
        {...rest}
        disabled={disabled}
        type={visible ? "text" : "password"}
        className={[styles.input, className ?? ""].filter(Boolean).join(" ")}
      />
      <button
        type="button"
        className={styles.toggle}
        aria-label="Arată parola"
        aria-pressed={visible}
        disabled={disabled}
        // Păstrează cursorul în câmp când apeși butonul.
        onMouseDown={(event) => event.preventDefault()}
        onClick={() => setVisible((current) => !current)}
      >
        <Icon name={visible ? "eye-off" : "eye"} size={20} />
      </button>
    </div>
  );
}
