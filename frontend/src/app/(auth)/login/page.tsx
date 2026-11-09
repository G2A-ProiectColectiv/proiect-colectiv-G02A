import type { Metadata } from "next";
import { LoginView } from "@/features/auth/views/LoginView";

export const metadata: Metadata = { title: "Autentificare" };

export default function LoginPage() {
  return <LoginView />;
}
