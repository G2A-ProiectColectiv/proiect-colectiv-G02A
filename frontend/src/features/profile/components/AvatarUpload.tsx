"use client";

import { useEffect, useState, type ChangeEvent } from "react";
import { Avatar } from "@/shared/components/Avatar";
import { useUploadAvatar } from "../hooks/useUploadAvatar";
import type { Profile } from "../types";
import styles from "./AvatarUpload.module.css";
import { validateImageFile } from "@/shared/utils/validateImageFile";

interface AvatarUploadProps {
  profile: Profile;
}

// Gestioneaza selectarea, incarcarea si stergerea pozei de profil.
export function AvatarUpload({ profile }: AvatarUploadProps) {
  const { upload, remove } = useUploadAvatar();

  const [file, setFile] = useState<File | null>(null);
  const [preview, setPreview] = useState<string | null>(null);
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  // Genereaza o previzualizare a imaginii selectate.
  // Elibereaza URL-ul imaginii cand previzualizarea se schimba
  // sau cand componenta este demontata.
  useEffect(() => {
    return () => {
      if (preview) {
        URL.revokeObjectURL(preview);
      }
    };
  }, [preview]);

  // Verifica imaginea selectata si genereaza previzualizarea.
  function handleFileChange(event: ChangeEvent<HTMLInputElement>) {
    const selected = event.target.files?.[0];

    if (!selected) return;

    setMessage("");
    setError("");
    setFile(null);
    setPreview(null);
    event.target.value = "";

    const validationError = validateImageFile(selected);

    if (validationError) {
      setError(validationError);
      return;
    }

    setFile(selected);
    setPreview(URL.createObjectURL(selected));
  }

  // Trimite imaginea selectata catre backend.
  function handleUpload() {
    if (!file) return;

    setError("");
    setMessage("");

    upload.mutate(file, {
      onSuccess: () => {
        setFile(null);
        setPreview(null);
        setMessage("Poza de profil a fost actualizată.");
      },
      onError: () => {
        setError("Poza nu a putut fi încărcată. Încearcă din nou.");
      },
    });
  }

  // Solicita stergerea pozei de profil existente.
  function handleRemove() {
    setError("");
    setMessage("");

    remove.mutate(undefined, {
      onSuccess: () => {
        setFile(null);
        setPreview(null);
        setMessage("Poza de profil a fost eliminată.");
      },
      onError: () => {
        setError("Poza nu a putut fi eliminată.");
      },
    });
  }

  const isBusy = upload.isPending || remove.isPending;

  return (
    <section className={styles.card}>
      <h2 className={styles.title}>Poza de profil</h2>

      <div className={styles.content}>
        {preview ? (
          // eslint-disable-next-line @next/next/no-img-element
          <img src={preview} alt="Previzualizarea pozei selectate" className={styles.preview} />
        ) : (
          <Avatar
            firstName={profile.firstName}
            lastName={profile.lastName}
            avatarUrl={profile.avatarUrl}
            size="lg"
          />
        )}

        <div className={styles.controls}>
          <p className={styles.hint}>JPG, PNG sau JPEG. Maximum 5 MB.</p>

          <label className={styles.chooseButton}>
            Alege o imagine
            <input
              type="file"
              accept=".jpg,.jpeg,.png"
              onChange={handleFileChange}
              disabled={isBusy}
              className={styles.fileInput}
            />
          </label>

          {file && (
            <button
              type="button"
              onClick={handleUpload}
              disabled={isBusy}
              className={styles.primaryButton}
            >
              {upload.isPending ? "Se încarcă..." : "Încarcă poza"}
            </button>
          )}

          {profile.avatarUrl && (
            <button
              type="button"
              onClick={handleRemove}
              disabled={isBusy}
              className={styles.removeButton}
            >
              {remove.isPending ? "Se elimină..." : "Elimină poza"}
            </button>
          )}
        </div>
      </div>

      {message && (
        <p role="status" className={styles.success}>
          {message}
        </p>
      )}

      {error && (
        <p role="alert" className={styles.error}>
          {error}
        </p>
      )}
    </section>
  );
}
