"use client";

import { useState } from "react";
import Link from "next/link";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";

import { getApiErrorMessage } from "@/shared/api/apiError";
import { Button } from "@/shared/components/Button";
import { FormField } from "@/shared/components/FormField";
import { Input } from "@/shared/components/Input";

import {
  forgotPasswordSchema,
  type ForgotPasswordFormValues,
} from "../schemas/passwordResetSchemas";

import { useRequestPasswordReset } from "../hooks/useRequestPasswordReset";
import styles from "./ForgotPasswordForm.module.css";

// Afiseaza formularul pentru solicitarea resetarii parolei.
export function ForgotPasswordForm() {
  const [isSubmitted, setIsSubmitted] = useState(false);
  const [formError, setFormError] = useState<string | null>(null);

  const { mutate, isPending } = useRequestPasswordReset();

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<ForgotPasswordFormValues>({
    resolver: zodResolver(forgotPasswordSchema),
    defaultValues: {
      email: "",
    },
  });

  // Valideaza emailul si trimite cererea de resetare catre backend.
  const onSubmit = handleSubmit((values) => {
    setFormError(null);

    mutate(values, {
      onSuccess: () => {
        setIsSubmitted(true);
      },
      onError: (error) => {
        setFormError(getApiErrorMessage(error));
      },
    });
  });

  // Afiseaza confirmarea numai dupa ce backendul accepta cererea.
  if (isSubmitted) {
    return (
      <div className={styles.success} role="status">
        <h2 className={styles.successTitle}>
          Verifică-ți emailul
        </h2>

        <p className={styles.description}>
          Dacă există un cont asociat acestei adrese,
          vei primi instrucțiuni pentru resetarea parolei.
        </p>

        <Link href="/login" className={styles.backLink}>
          Înapoi la autentificare
        </Link>
      </div>
    );
  }

  return (
    <form
      className={styles.form}
      onSubmit={onSubmit}
      noValidate
    >
      {formError && (
        <p className={styles.error} role="alert">
          {formError}
        </p>
      )}

      <FormField
        label="Email"
        htmlFor="email"
        error={errors.email?.message}
      >
        <Input
          id="email"
          type="email"
          autoComplete="email"
          placeholder="exemplu@email.com"
          invalid={!!errors.email}
          aria-describedby={
            errors.email ? "email-error" : undefined
          }
          {...register("email")}
        />
      </FormField>

      <Button
        type="submit"
        fullWidth
        loading={isPending}
      >
        Trimite linkul de resetare
      </Button>

      <Link href="/login" className={styles.backLink}>
        Înapoi la autentificare
      </Link>
    </form>
  );
}
