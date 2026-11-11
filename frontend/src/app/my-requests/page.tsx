import type { Metadata } from "next";
import { MyRequestsView } from "@/features/rentalRequests/views/MyRequestsView";
import styles from "./page.module.css";

export const metadata: Metadata = { title: "Cererile mele" };

export default function MyRequestsPage() {
  return (
    <div className={styles.page}>
      <MyRequestsView />
    </div>
  );
}
