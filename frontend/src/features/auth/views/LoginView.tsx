import { Icon, type IconName } from "@/shared/components/Icon";
import { LoginForm } from "../components/LoginForm";
import styles from "./LoginView.module.css";

const BENEFITS: { icon: IconName; text: string }[] = [
  { icon: "pin", text: "Locația exactă pe hartă, nu doar „aproape de centru”." },
  { icon: "sliders", text: "Filtre pentru preț, camere, dotări și disponibilitate." },
  { icon: "user", text: "Contact direct cu proprietarul, fără intermediari." },
];

export function LoginView() {
  return (
    <main className={styles.page}>
      <section className={styles.formSide}>
        <div className={styles.card}>
          <h1 className={styles.title}>Bine ai revenit</h1>
          <p className={styles.lead}>Intră în cont ca să-ți gestionezi anunțurile sau cererile.</p>
          <LoginForm />
        </div>
      </section>

      <aside className={styles.panel}>
        <div className={styles.panelContent}>
          <h2 className={styles.panelTitle}>
            Tot ce trebuie să știi despre o locuință, înainte să o vizitezi.
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
