"use client";

import { useState } from "react";
import Link from "next/link";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { getApiErrorMessage, getApiErrorStatus, getFieldErrors } from "@/shared/api/apiError";
import { Button } from "@/shared/components/Button";
import { FormField } from "@/shared/components/FormField";
import { Icon, type IconName } from "@/shared/components/Icon";
import { Input } from "@/shared/components/Input";
import { registerSchema, type RegisterFormValues } from "../schemas/authSchemas";
import { useRegister } from "../hooks/useRegister";
import type { SelfRegisterRole } from "../types";
import styles from "./RegisterForm.module.css";

const FIELDS = ["firstName", "lastName", "email", "password", "role"] as const;

function isRegisterField(field: string): field is (typeof FIELDS)[number] {
  return (FIELDS as readonly string[]).includes(field);
}

const ROLE_OPTIONS: {
  value: SelfRegisterRole;
  title: string;
  icon: IconName;
}[] = [
  {
    value: "TENANT",
    title: "Chiriaș",
    icon: "search",
  },
  {
    value: "LANDLORD",
    title: "Proprietar",
    icon: "home",
  },
];

export function RegisterForm() {
  const {
    register,
    handleSubmit,
    setError,
    formState: { errors },
  } = useForm<RegisterFormValues>({
    resolver: zodResolver(registerSchema),
    defaultValues: { firstName: "", lastName: "", email: "", password: "", role: "TENANT" },
  });
  const [formError, setFormError] = useState<string | null>(null);
  const { mutate, isPending } = useRegister();

  const onSubmit = handleSubmit((values) => {
    setFormError(null);
    mutate(values, {
      onError: (error) => {
        // Emailul deja folosit apare la câmpul email (AUTH-03).
        if (getApiErrorStatus(error) === 409) {
          setError("email", { message: "Există deja un cont cu acest email." });
          return;
        }

        let handled = false;
        for (const [field, message] of Object.entries(getFieldErrors(error))) {
          if (isRegisterField(field)) {
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

      <fieldset className={styles.roles}>
        <legend className={styles.legend}>Eu sunt...</legend>
        <div className={styles.roleOptions}>
          {ROLE_OPTIONS.map((option) => (
            <label key={option.value} className={styles.roleOption}>
              <input
                type="radio"
                value={option.value}
                defaultChecked={option.value === "TENANT"}
                className={styles.roleInput}
                {...register("role")}
              />
              <span className={styles.roleCard}>
                <span className={styles.radio} aria-hidden="true" />
                <span className={styles.roleIcon}>
                  <Icon name={option.icon} size={22} />
                </span>
                <span className={styles.roleLabels}>
                  <span className={styles.roleTitle}>{option.title}</span>
                </span>
              </span>
            </label>
          ))}
        </div>
        {errors.role && (
          <p className={styles.roleError} role="alert">
            {errors.role.message}
          </p>
        )}
      </fieldset>

      <div className={styles.nameRow}>
        <FormField label="Prenume" htmlFor="firstName" error={errors.firstName?.message}>
          <Input
            id="firstName"
            autoComplete="given-name"
            invalid={!!errors.firstName}
            aria-describedby={errors.firstName ? "firstName-error" : undefined}
            {...register("firstName")}
          />
        </FormField>
        <FormField label="Nume" htmlFor="lastName" error={errors.lastName?.message}>
          <Input
            id="lastName"
            autoComplete="family-name"
            invalid={!!errors.lastName}
            aria-describedby={errors.lastName ? "lastName-error" : undefined}
            {...register("lastName")}
          />
        </FormField>
      </div>

      <FormField label="Email" htmlFor="email" error={errors.email?.message}>
        <Input
          id="email"
          type="email"
          autoComplete="email"
          invalid={!!errors.email}
          aria-describedby={errors.email ? "email-error" : undefined}
          {...register("email")}
        />
      </FormField>

      <FormField
        label="Parolă"
        htmlFor="password"
        hint="Alege o parolă pe care n-o folosești în alt loc."
        error={errors.password?.message}
      >
        <Input
          id="password"
          type="password"
          autoComplete="new-password"
          invalid={!!errors.password}
          aria-describedby={errors.password ? "password-error" : "password-hint"}
          {...register("password")}
        />
      </FormField>

      <Button type="submit" fullWidth loading={isPending}>
        Creează cont
      </Button>

      <p className={styles.footer}>
        Ai deja cont?{" "}
        <Link href="/login" className={styles.link}>
          Intră în cont
        </Link>
      </p>
    </form>
  );
}
