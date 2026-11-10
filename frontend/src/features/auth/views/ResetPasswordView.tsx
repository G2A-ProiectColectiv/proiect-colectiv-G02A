"use client";

import Link from "next/link";
import { useSearchParams } from "next/navigation";
import { Icon, type IconName } from "@/shared/components/Icon";
import { ResetPasswordForm } from "../components/ResetPasswordForm";
import styles from "./ResetPasswordView.module.css";

const BENEFITS: { icon: IconName; text: string }[] = [
  { icon: "pin", text: "Locația exactă pe hartă, nu doar „aproape de centru”." },
  { icon: "sliders", text: "Filtre pentru preț, camere, dotări și disponibilitate." },
  { icon: "user", text: "Contact direct cu proprietarul, fără intermediari." },
];

// Afiseaza pagina de resetare si citeste tokenul din URL.
export function ResetPasswordView() {
  const searchParams = useSearchParams();
  const token = searchParams.get("token")?.trim();

  return (
    <main className={styles.page}>
      <section className={styles.formSide}>
        <div className={styles.card}>
          <h1 className={styles.title}>Resetează parola</h1>

          {token ? (
            <>
              <p className={styles.lead}>
                Introdu parola nouă și confirm-o pentru a finaliza
                resetarea contului.
              </p>

              <ResetPasswordForm token={token} />
            </>
          ) : (
            <div className={styles.missingToken} role="alert">
              <p>
                Linkul de resetare este invalid sau nu conține un token.
              </p>

              <Link href="/forgot-password" className={styles.link}>
                Solicită un link nou
              </Link>
            </div>
          )}
        </div>
      </section>

      <aside className={styles.panel}>
        <div className={styles.panelContent}>
          <h2 className={styles.panelTitle}>
            Tot ce trebuie să știi despre o locuință,
            înainte să o vizitezi.
          </h2>

          <ul className={styles.list}>
            {BENEFITS.map((benefit) => (
              <li key={benefit.text} className={styles.item}>
                <span className={styles.itemIcon}>
                  <Icon name={benefit.icon} size={20} />
                </span>
                {benefit.text}
              </li>
            ))}
          </ul>
        </div>
      </aside>
    </main>
  );
}
