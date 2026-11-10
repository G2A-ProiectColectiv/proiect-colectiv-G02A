
"use client";

import { useState } from "react";
import { useDeleteAccount } from "../hooks/useDeleteAccount";
import { ConfirmDialog } from "./ConfirmDialog";
import styles from "./DeleteAccountSection.module.css";

// Gestioneaza confirmarea si stergerea contului utilizatorului.
export function DeleteAccountSection() {
  const deleteMutation = useDeleteAccount();

  const [password, setPassword] = useState("");
  const [isDialogOpen, setIsDialogOpen] = useState(false);
  const [error, setError] = useState("");

// Deschide dialogul de confirmare pentru stergerea contului.
  function handleOpenDialog() {
    setError("");
    setIsDialogOpen(true);
  }

  // Inchide dialogul fara a sterge contul.
  function handleCancel() {
    if (deleteMutation.isPending) return;

    setIsDialogOpen(false);
    setPassword("");
    setError("");
  }

  // Trimite cererea de stergere a contului catre backend.
  function handleConfirm() {
    setError("");

    if (!password.trim()) {
      setError("Introdu parola pentru a continua.");
      return;
    }

    deleteMutation.mutate(
      { password },
      {
        onSuccess: () => {
          setPassword("");
          setIsDialogOpen(false);
        },
        onError: () => {
          setError(
            "Contul nu a putut fi șters. Verifică parola și încearcă din nou."
          );
        },
      }
    );
  }

  const isBusy = deleteMutation.isPending;

  return (
    <section className={styles.card}>
      <h2 className={styles.title}>Ștergerea contului</h2>

      <p className={styles.description}>
        Această acțiune este definitivă și nu poate fi anulată.
        Pentru a confirma ștergerea contului, introdu parola.
      </p>

      <button
        type="button"
        className={styles.deleteButton}
        onClick={handleOpenDialog}
        disabled={isBusy}
      >
        Șterge contul
      </button>

      {isDialogOpen && (
        <ConfirmDialog
          title="Confirmi ștergerea contului?"
          description="Contul tău va fi șters definitiv. Această acțiune nu poate fi anulată."
          confirmLabel="Da, șterge contul"
          isPending={isBusy}
          password={password}
          onPasswordChange={setPassword}
          error={error}
          onConfirm={handleConfirm}
          onCancel={handleCancel}
        />
      )}
    </section>
  );
}
