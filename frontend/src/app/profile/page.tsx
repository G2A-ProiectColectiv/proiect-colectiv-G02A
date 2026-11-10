import { ProfileView } from "@/features/profile/views/ProfileView";
import styles from "./page.module.css";

// Defineste ruta /profile si afiseaza pagina contului utilizatorului.
export default function ProfilePage() {
  return (
    <div className={styles.page}>
      <ProfileView />
    </div>
  );
}
