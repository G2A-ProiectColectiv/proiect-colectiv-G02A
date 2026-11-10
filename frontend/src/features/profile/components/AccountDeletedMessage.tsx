"use client";

import { useSearchParams } from "next/navigation";
import styles from "./AccountDeletedMessage.module.css";

// Afiseaza mesajul dupa stergerea cu succes a contului.
export function AccountDeletedMessage() {
  const searchParams = useSearchParams();
  const accountDeleted = searchParams.get("accountDeleted");

  if (accountDeleted !== "1") {
    return null;
  }

  return (
    <div className={styles.message} role="status">
      Contul tău a fost șters cu succes.
    </div>
  );
}
