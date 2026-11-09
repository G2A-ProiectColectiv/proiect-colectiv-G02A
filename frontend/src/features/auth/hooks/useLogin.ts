"use client";

import { useMutation } from "@tanstack/react-query";
import { useRouter } from "next/navigation";
import { useAuth } from "../context/AuthContext";
import { login } from "../api/authApi";

/** Acceptăm doar căi din aplicație (`/ruta`), nu adrese externe (`//site.ro`, `/\site.ro`). */
function isSafeNextPath(path: string | null): path is string {
  return !!path && path.startsWith("/") && !path.startsWith("//") && !path.startsWith("/\\");
}

/**
 * După autentificare: la ruta din `?next=` (pusă de `proxy.ts` când ai fost trimis la login),
 * altfel chiriașul și proprietarul ajung pe pagina principală, iar adminul pe lista de utilizatori.
 */
export function useLogin() {
  const router = useRouter();
  const { signIn } = useAuth();

  return useMutation({
    mutationFn: login,
    onSuccess: (auth) => {
      signIn(auth);

      const next = new URLSearchParams(window.location.search).get("next");
      const fallback = auth.user.role === "ADMIN" ? "/admin/users" : "/";
      router.replace(isSafeNextPath(next) ? next : fallback);
    },
  });
}
