import Link from "next/link";
import styles from "../../not-found.module.css";

export default function RequestNotFound() {
  return (
    <main className={styles.main}>
      <h1 className={styles.title}>Cererea nu mai este disponibilă</h1>
      <p className={styles.description}>
        Cererea a fost închisă, a fost ștearsă sau adresa nu este corectă.
      </p>
      <Link href="/requests" className={styles.link}>
        Înapoi la cereri
      </Link>
    </main>
  );
}
