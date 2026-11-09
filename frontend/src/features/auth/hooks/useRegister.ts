"use client";

import { useMutation } from "@tanstack/react-query";
import { useRouter } from "next/navigation";
import { toast } from "@/shared/components/Toaster";
import { register } from "../api/authApi";

/** După înregistrare nu se primește token: utilizatorul se autentifică separat. */
export function useRegister() {
  const router = useRouter();

  return useMutation({
    mutationFn: register,
    onSuccess: () => {
      toast.success("Contul a fost creat. Acum te poți autentifica.");
      router.push("/login");
    },
  });
}
