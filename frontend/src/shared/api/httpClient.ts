import axios, { isAxiosError } from "axios";
import { clearToken, getToken } from "@/shared/auth/tokenCookie";

const LOGIN_PATH = "/login";
const AUTH_PAGES = [LOGIN_PATH, "/register"];

/**
 * Singurul client HTTP al aplicației. Nicio componentă nu importă Axios direct.
 * Adresele sunt relative la `/api`, iar Next.js le trimite către backend (rewrites).
 */
export const httpClient = axios.create({ baseURL: "/api" });

httpClient.interceptors.request.use((config) => {
  const token = getToken();
  if (token) config.headers.set("Authorization", `Bearer ${token}`);
  return config;
});

httpClient.interceptors.response.use(
  (response) => response,
  (error: unknown) => {
    const isUnauthorized = isAxiosError(error) && error.response?.status === 401;
    // La /auth/* un 401 înseamnă „credențiale greșite", iar formularul îl afișează el însuși.
    const isAuthCall = isAxiosError(error) && (error.config?.url ?? "").startsWith("/auth/");

    if (isUnauthorized && !isAuthCall && typeof window !== "undefined") {
      clearToken();
      if (!AUTH_PAGES.includes(window.location.pathname)) {
        // Reîncărcare completă, intenționat: aici nu avem acces la router, iar astfel
        // se golește și starea (cache-ul) rămasă de la sesiunea expirată.
        // eslint-disable-next-line @next/next/no-location-assign-relative-destination
        window.location.assign(LOGIN_PATH);
      }
    }

    return Promise.reject(error);
  },
);
