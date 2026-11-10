import { Suspense } from "react";
import type { Metadata } from "next";
import { ResetPasswordView } from "@/features/auth/views/ResetPasswordView";

export const metadata: Metadata = {
  title: "Resetare parolă",
};

// Afiseaza pagina de resetare a parolei.
export default function ResetPasswordPage() {
  return (
    <Suspense fallback={<main>Se încarcă pagina de resetare...</main>}>
      <ResetPasswordView />
    </Suspense>
  );
}
