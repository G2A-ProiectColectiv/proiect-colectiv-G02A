import { RegisterForm } from "../components/RegisterForm";
import styles from "./RegisterView.module.css";

const ROLES = [
  {
    title: "Chiriașul",
    description:
      "Caută pe hartă și cu filtre, vede datele de contact după autentificare și poate publica o cerere cu bugetul lui.",
  },
  {
    title: "Proprietarul",
    description:
      "Publică anunțuri cu până la 10 poze, le marchează închiriate când e cazul și caută printre cererile chiriașilor.",
  },
];

export function RegisterView() {
  return (
    <main className={styles.page}>
      <section className={styles.formSide}>
        <div className={styles.card}>
          <h1 className={styles.title}>Creează-ți contul</h1>
          <p className={styles.lead}>Durează un minut. Alegi acum ce vrei să faci pe platformă.</p>
          <RegisterForm />
        </div>
      </section>

      <aside className={styles.panel}>
        <div className={styles.panelContent}>
          <h2 className={styles.panelTitle}>Două roluri, un singur loc.</h2>
          <dl className={styles.roles}>
            {ROLES.map((role) => (
              <div key={role.title} className={styles.role}>
                <dt className={styles.roleTitle}>{role.title}</dt>
                <dd className={styles.roleText}>{role.description}</dd>
              </div>
            ))}
          </dl>
        </div>
      </aside>
    </main>
  );
}
