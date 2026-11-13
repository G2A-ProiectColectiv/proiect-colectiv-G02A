"use client";

import { useState } from "react";
import Link from "next/link";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";

import { getApiErrorMessage, getApiErrorStatus } from "@/shared/api/apiError";
import { Button } from "@/shared/components/Button";
import { FormField } from "@/shared/components/FormField";
import { PasswordInput } from "@/shared/components/PasswordInput";

import {
  resetPasswordSchema,
  type ResetPasswordFormValues,
} from "../schemas/passwordResetSchemas";
import { useConfirmPasswordReset } from "../hooks/useConfirmPasswordReset";

import styles from "./ResetPasswordForm.module.css";

interface ResetPasswordFormProps {
  token: string;
}

// Afiseaza formularul pentru introducerea parolei noi.
export function ResetPasswordForm({ token }: ResetPasswordFormProps) {
  const [isSubmitted, setIsSubmitted] = useState(false);
  const [formError, setFormError] = useState<string | null>(null);
  const [invalidToken, setInvalidToken] = useState(false);

  const { mutate, isPending } = useConfirmPasswordReset();

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<ResetPasswordFormValues>({
    resolver: zodResolver(resetPasswordSchema),
    defaultValues: {
      newPassword: "",
      confirmPassword: "",
    },
  });

  // Trimite tokenul si parola noua catre backend dupa validare.
  const onSubmit = handleSubmit((values) => {
    setFormError(null);

    mutate(
      {
        token,
        newPassword: values.newPassword,
      },
      {
        onSuccess: () => {
          setIsSubmitted(true);
        },
        onError: (error) => {
          if (getApiErrorStatus(error) === 400) {
            setInvalidToken(true);
            return;
          }

          setFormError(getApiErrorMessage(error));
        },
      }
    );
  });

  // Afiseaza confirmarea dupa resetarea reusita a parolei.
  if (isSubmitted) {
    return (
      <div className={styles.message} role="status">
        <h2 className={styles.messageTitle}>Parola a fost resetată</h2>
        <p className={styles.description}>
          Acum te poți autentifica folosind parola nouă.
        </p>
        <Link href="/login" className={styles.link}>
          Mergi la autentificare
        </Link>
      </div>
    );
  }

  // Afiseaza un mesaj cand backendul respinge tokenul.
  if (invalidToken) {
    return (
      <div className={styles.message} role="alert">
        <h2 className={styles.messageTitle}>Link invalid sau expirat</h2>
        <p className={styles.description}>
          Linkul de resetare nu este valid, a expirat sau a fost deja folosit.
        </p>
        <Link href="/forgot-password" className={styles.link}>
          Solicită un link nou
        </Link>
      </div>
    );
  }

  return (
    <form className={styles.form} onSubmit={onSubmit} noValidate>
      {formError && (
        <p className={styles.error} role="alert">
          {formError}
        </p>
      )}

      <FormField
        label="Parola nouă"
        htmlFor="newPassword"
        error={errors.newPassword?.message}
      >
        <PasswordInput
          id="newPassword"
          autoComplete="new-password"
          invalid={!!errors.newPassword}
          aria-describedby={errors.newPassword ? "newPassword-error" : undefined}
          {...register("newPassword")}
        />
      </FormField>

      <FormField
        label="Confirmă parola nouă"
        htmlFor="confirmPassword"
        error={errors.confirmPassword?.message}
      >
        <PasswordInput
          id="confirmPassword"
          autoComplete="new-password"
          invalid={!!errors.confirmPassword}
          aria-describedby={
            errors.confirmPassword ? "confirmPassword-error" : undefined
          }
          {...register("confirmPassword")}
        />
      </FormField>

      <Button type="submit" fullWidth loading={isPending}>
        Resetează parola
      </Button>

      <Link href="/login" className={styles.link}>
        Înapoi la autentificare
      </Link>
    </form>
  );
}
