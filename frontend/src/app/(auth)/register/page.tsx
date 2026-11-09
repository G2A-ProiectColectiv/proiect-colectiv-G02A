import type { Metadata } from "next";
import { RegisterView } from "@/features/auth/views/RegisterView";

export const metadata: Metadata = { title: "Creează cont" };

export default function RegisterPage() {
  return <RegisterView />;
}
