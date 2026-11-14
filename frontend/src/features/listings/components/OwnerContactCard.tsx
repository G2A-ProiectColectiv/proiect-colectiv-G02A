"use client";

import Link from "next/link";
import { useAuth } from "@/features/auth/context/AuthContext";
import { Avatar } from "@/shared/components/Avatar";
import type { ListingOwner } from "../types";
import styles from "./OwnerContactCard.module.css";

export interface OwnerContactCardProps {
  owner: ListingOwner;
}

/** Nume și avatar pentru toți; telefonul doar pentru utilizatorii autentificați (DET-03). */
export function OwnerContactCard({ owner }: OwnerContactCardProps) {
  const { user, isReady } = useAuth();

  return (
    <section className={styles.card} aria-label="Proprietar">
      <Avatar
        firstName={owner.firstName}
        lastName={owner.lastName}
        avatarUrl={owner.avatarUrl}
        size="lg"
      />
      <div className={styles.info}>
        <p className={styles.label}>Proprietar</p>
        <p className={styles.name}>
          {owner.firstName} {owner.lastName}
        </p>

        {isReady && user && owner.phone && (
          <a className={styles.phone} href={`tel:${owner.phone}`}>
            {owner.phone}
          </a>
        )}
        {isReady && user && !owner.phone && (
          <p className={styles.hint}>Proprietarul nu a adăugat un număr de telefon.</p>
        )}
        {isReady && !user && (
          <Link className={styles.login} href="/login">
            Autentifică-te pentru a vedea telefonul
          </Link>
        )}
      </div>
    </section>
  );
}
