"use client";

import { useMe } from "../hooks/useMe";
import { AvatarUpload } from "../components/AvatarUpload";
import { ProfileForm } from "../components/ProfileForm";
import { DeleteAccountSection } from "../components/DeleteAccountSection";
import styles from "./ProfileView.module.css";

// Afiseaza pagina de profil si optiunile contului utilizatorului.
export function ProfileView() {
  const { data: profile, isLoading, isError, refetch } = useMe();

  // Afiseaza un mesaj pana la incarcarea datelor profilului.
  if (isLoading) {
    return (
      <main className={styles.container}>
        <p role="status">Se încarcă profilul...</p>
      </main>
    );
  }

  // Afiseaza un mesaj de eroare daca profilul nu poate fi incarcat.
  if (isError || !profile) {
    return (
      <main className={styles.container}>
        <div className={styles.errorState}>
          <p>Nu am putut încărca datele profilului.</p>

          <button
            type="button"
            className={styles.retryButton}
            onClick={() => void refetch()}
          >
            Încearcă din nou
          </button>
        </div>
      </main>
    );
  }

  // Afiseaza componentele profilului dupa incarcarea datelor.
  return (
    <main className={styles.container}>
      <header className={styles.header}>
        <h1 className={styles.heading}>Contul meu</h1>

        <p className={styles.subtitle}>
          Gestionează informațiile personale și setările contului.
        </p>
      </header>

      <div className={styles.sections}>
        <AvatarUpload profile={profile} />

        <ProfileForm profile={profile} />

        <DeleteAccountSection />
      </div>
    </main>
  );
}
