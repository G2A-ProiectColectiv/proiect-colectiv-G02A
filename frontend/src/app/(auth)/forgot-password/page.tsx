import type { Metadata } from "next";
import { ForgotPasswordView } from "@/features/auth/views/ForgotPasswordView";

export const metadata: Metadata = {
  title: "Recuperare parolă",
};

// Afiseaza pagina pentru solicitarea resetarii parolei.
export default function ForgotPasswordPage() {
  return <ForgotPasswordView />;
}
