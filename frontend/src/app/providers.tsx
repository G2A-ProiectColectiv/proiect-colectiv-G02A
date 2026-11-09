"use client";

import { useState, type ReactNode } from "react";
import { QueryClientProvider } from "@tanstack/react-query";
import { AuthProvider } from "@/features/auth/context/AuthContext";
import { createQueryClient } from "@/shared/api/queryClient";
import { Toaster } from "@/shared/components/Toaster";

export function Providers({ children }: { children: ReactNode }) {
  // Un singur QueryClient pe durata vieții aplicației din browser.
  const [queryClient] = useState(createQueryClient);

  return (
    <QueryClientProvider client={queryClient}>
      <AuthProvider>
        {children}
        <Toaster />
      </AuthProvider>
    </QueryClientProvider>
  );
}
