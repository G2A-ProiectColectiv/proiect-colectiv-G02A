import type { Metadata } from "next";
import { CreateRequestView } from "@/features/rentalRequests/views/CreateRequestView";
import styles from "./page.module.css";

export const metadata: Metadata = { title: "Cerere nouă" };

export default function NewRequestPage() {
  return (
    <div className={styles.page}>
      <CreateRequestView />
    </div>
  );
}
