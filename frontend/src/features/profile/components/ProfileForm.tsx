"use client";

import { useEffect, useState } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { profileSchema, type ProfileFormValues } from "../schemas/profileSchemas";
import { useUpdateProfile } from "../hooks/useUpdateProfile";
import type { Profile } from "../types";
import styles from "./ProfileForm.module.css";

interface ProfileFormProps {
  profile: Profile;
}

// Afiseaza formularul pentru vizualizarea si editarea datelor personale.
export function ProfileForm({ profile }: ProfileFormProps) {
  const updateMutation = useUpdateProfile();

  const [message, setMessage] = useState("");
  const [serverError, setServerError] = useState("");

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<ProfileFormValues>({
    resolver: zodResolver(profileSchema),
    defaultValues: {
      firstName: profile.firstName,
      lastName: profile.lastName,
      phone: profile.phone ?? "",
      bio: profile.bio ?? "",
    },
  });

  // Actualizeaza campurile formularului cand se modifica datele profilului.
  useEffect(() => {
    reset({
      firstName: profile.firstName,
      lastName: profile.lastName,
      phone: profile.phone ?? "",
      bio: profile.bio ?? "",
    });
  }, [profile, reset]);

  // Valideaza si trimite datele modificate catre backend.
  function onSubmit(values: ProfileFormValues) {
    setMessage("");
    setServerError("");

    updateMutation.mutate(
      {
        firstName: values.firstName.trim(),
        lastName: values.lastName.trim(),
        phone: values.phone?.trim() || null,
        bio: values.bio?.trim() || null,
      },
      {
        onSuccess: () => {
          setMessage("Profilul a fost actualizat cu succes.");
        },
        onError: () => {
          setServerError(
            "Profilul nu a putut fi actualizat. Verifică datele și încearcă din nou."
          );
        },
      }
    );
  }

  return (
    <form
      className={styles.form}
      onSubmit={handleSubmit(onSubmit)}
      noValidate
    >
      <div className={styles.heading}>
        <h2 className={styles.title}>Informații personale</h2>
        <p className={styles.description}>
          Actualizează informațiile asociate contului tău.
        </p>
      </div>

      <div className={styles.row}>
        <div className={styles.field}>
          <label htmlFor="firstName">Prenume</label>
          <input
            id="firstName"
            className={styles.input}
            {...register("firstName")}
          />
          {errors.firstName && (
            <p className={styles.error}>{errors.firstName.message}</p>
          )}
        </div>

        <div className={styles.field}>
          <label htmlFor="lastName">Nume</label>
          <input
            id="lastName"
            className={styles.input}
            {...register("lastName")}
          />
          {errors.lastName && (
            <p className={styles.error}>{errors.lastName.message}</p>
          )}
        </div>
      </div>

      <div className={styles.field}>
        <label htmlFor="email">Email</label>
        <input
          id="email"
          type="email"
          value={profile.email}
          className={`${styles.input} ${styles.disabled}`}
          disabled
          readOnly
        />
        <span className={styles.hint}>
          Adresa de email nu poate fi modificată din profil.
        </span>
      </div>

      <div className={styles.field}>
        <label htmlFor="phone">Număr de telefon</label>
        <input
          id="phone"
          type="tel"
          placeholder="07XXXXXXXX sau +407XXXXXXXX"
          className={styles.input}
          {...register("phone")}
        />
        {errors.phone && (
          <p className={styles.error}>{errors.phone.message}</p>
        )}
      </div>

      <div className={styles.field}>
        <label htmlFor="bio">Descriere</label>
        <textarea
          id="bio"
          rows={5}
          placeholder="Scrie câteva informații despre tine..."
          className={styles.textarea}
          {...register("bio")}
        />
        {errors.bio && (
          <p className={styles.error}>{errors.bio.message}</p>
        )}
      </div>

      {message && (
        <p className={styles.success} role="status">
          {message}
        </p>
      )}

      {serverError && (
        <p className={styles.error} role="alert">
          {serverError}
        </p>
      )}

      <div className={styles.actions}>
        <button
          type="submit"
          className={styles.saveButton}
          disabled={updateMutation.isPending}
        >
          {updateMutation.isPending
            ? "Se salvează..."
            : "Salvează modificările"}
        </button>
      </div>
    </form>
  );
}
