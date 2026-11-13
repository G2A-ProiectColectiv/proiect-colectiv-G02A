"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { getApiErrorMessage, getApiErrorStatus, getFieldErrors } from "@/shared/api/apiError";
import { Button } from "@/shared/components/Button";
import { FormField } from "@/shared/components/FormField";
import { Icon } from "@/shared/components/Icon";
import { Input } from "@/shared/components/Input";
import { PasswordInput } from "@/shared/components/PasswordInput";
import { useAuth } from "../context/AuthContext";
import { loginSchema, type LoginFormValues } from "../schemas/authSchemas";
import { useLogin } from "../hooks/useLogin";
import styles from "./LoginForm.module.css";

const FIELDS = ["email", "password"] as const;

function isLoginField(field: string): field is (typeof FIELDS)[number] {
  return (FIELDS as readonly string[]).includes(field);
}

const WRONG_CREDENTIALS = "Email sau parolă incorectă.";

export function LoginForm() {
  const {
    register,
    handleSubmit,
    setError,
    reset,
    formState: { errors },
  } = useForm<LoginFormValues>({
    resolver: zodResolver(loginSchema),
    defaultValues: { email: "", password: "" },
  });
  const [formError, setFormError] = useState<string | null>(null);
  const { mutate, isPending } = useLogin();
  const { user } = useAuth();

  // După delogare formularul trebuie să fie gol, chiar dacă pagina de login
  // a fost păstrată în memorie de la autentificarea anterioară.
  useEffect(() => {
    if (!user) {
      reset({ email: "", password: "" });
      setFormError(null);
    }
  }, [user, reset]);

  // La credențiale greșite marcăm ambele câmpuri, fără mesaj separat sub ele.
  const credentialsRejected = formError === WRONG_CREDENTIALS;

  const onSubmit = handleSubmit((values) => {
    setFormError(null);
    mutate(values, {
      onError: (error) => {
        // Același mesaj pentru email inexistent și parolă greșită (AUTH-07).
        if (getApiErrorStatus(error) === 401) {
          setFormError(WRONG_CREDENTIALS);
          return;
        }

        let handled = false;
        for (const [field, message] of Object.entries(getFieldErrors(error))) {
          if (isLoginField(field)) {
            setError(field, { message });
            handled = true;
          }
        }
        if (!handled) setFormError(getApiErrorMessage(error));
      },
    });
  });

  return (
    <form className={styles.form} onSubmit={onSubmit} noValidate>
      {formError && (
        <p className={styles.banner} role="alert">
          <Icon name="alert" size={20} />
          {formError}
        </p>
      )}

      <FormField label="Email" htmlFor="email" error={errors.email?.message}>
        <Input
          id="email"
          type="email"
          autoComplete="email"
          invalid={!!errors.email || credentialsRejected}
          aria-describedby={errors.email ? "email-error" : undefined}
          {...register("email")}
        />
      </FormField>

      <FormField
        label="Parolă"
        htmlFor="password"
        error={errors.password?.message}
        labelAction={
          <Link href="/forgot-password" className={styles.forgot}>
            Ai uitat parola?
          </Link>
        }
      >
        <PasswordInput
          id="password"
          autoComplete="current-password"
          invalid={!!errors.password || credentialsRejected}
          aria-describedby={errors.password ? "password-error" : undefined}
          {...register("password")}
        />
      </FormField>

      <Button type="submit" fullWidth loading={isPending}>
        Intră în cont
      </Button>

      <p className={styles.footer}>
        Nu ai cont?{" "}
        <Link href="/register" className={styles.link}>
          Creează unul gratuit
        </Link>
      </p>
    </form>
  );
}
