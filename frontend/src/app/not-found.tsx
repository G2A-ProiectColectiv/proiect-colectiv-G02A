import Link from "next/link";
import styles from "./not-found.module.css";

export default function NotFound() {
  return (
    <main className={styles.main}>
      <h1 className={styles.title}>Pagina nu a fost găsită</h1>
      <p className={styles.description}>Adresa pe care ai accesat-o nu există sau a fost mutată.</p>
      <Link href="/" className={styles.link}>
        Înapoi la pagina principală
      </Link>
    </main>
  );
}
