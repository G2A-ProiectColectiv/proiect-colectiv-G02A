import type { Metadata } from "next";
import { MyListingsView } from "@/features/listings/views/MyListingsView";

export const metadata: Metadata = { title: "Anunțurile mele" };

export default function MyListingsPage() {
  return <MyListingsView />;
}
