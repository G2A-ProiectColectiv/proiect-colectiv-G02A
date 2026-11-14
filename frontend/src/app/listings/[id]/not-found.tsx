import Link from "next/link";
import styles from "../../not-found.module.css";

export default function ListingNotFound() {
  return (
    <main className={styles.main}>
      <h1 className={styles.title}>Anunțul nu mai este disponibil</h1>
      <p className={styles.description}>
        Anunțul a fost șters, a fost închiriat sau adresa nu este corectă.
      </p>
      <Link href="/listings" className={styles.link}>
        Înapoi la căutare
      </Link>
    </main>
  );
}
