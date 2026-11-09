import { NextResponse, type NextRequest } from "next/server";
import {
  decodeToken,
  isTokenExpired,
  type DecodedToken,
  type Role,
} from "@/shared/auth/decodeToken";
import { TOKEN_COOKIE_NAME } from "@/shared/auth/tokenCookie";

/*
 * Protecția rutelor la nivel de navigare. Securitatea reală este în backend:
 * proxy-ul doar evită afișarea paginilor la care utilizatorul oricum nu are acces.
 * Nu verifică semnătura token-ului, citește doar rolul și expirarea din el.
 */

interface ProtectedRoute {
  /** Prefixul rutei: `/profile` acoperă și `/profile/...`. */
  prefix: string;
  roles: Role[];
}

/** Singurul loc cu rutele protejate; se completează în etapele următoare. */
const protectedRoutes: ProtectedRoute[] = [
  // { prefix: "/profile", roles: ["TENANT", "LANDLORD", "ADMIN"] },  ← etapa 1.2
  // { prefix: "/my-listings", roles: ["LANDLORD"] },                  ← etapa 1.3
];

/** Paginile pe care un utilizator deja autentificat nu are ce căuta. */
const AUTH_PAGES = ["/login", "/register"];

function matchesPrefix(pathname: string, prefix: string): boolean {
  return pathname === prefix || pathname.startsWith(`${prefix}/`);
}

function readSession(request: NextRequest): DecodedToken | null {
  const token = request.cookies.get(TOKEN_COOKIE_NAME)?.value;
  if (!token) return null;

  const decoded = decodeToken(token);
  return decoded && !isTokenExpired(decoded) ? decoded : null;
}

function redirectTo(request: NextRequest, path: string): NextResponse {
  return NextResponse.redirect(new URL(path, request.url));
}

export function proxy(request: NextRequest) {
  const { pathname, search } = request.nextUrl;
  const session = readSession(request);

  // Autentificat pe /login sau /register -> pagina principală.
  if (AUTH_PAGES.includes(pathname)) {
    return session ? redirectTo(request, "/") : NextResponse.next();
  }

  const route = protectedRoutes.find((candidate) => matchesPrefix(pathname, candidate.prefix));
  if (!route) return NextResponse.next();

  // Neautentificat (sau token expirat/invalid) -> /login?next=<ruta>.
  if (!session) {
    const response = redirectTo(request, `/login?next=${encodeURIComponent(pathname + search)}`);
    // Un token expirat sau stricat nu mai are rost să rămână în cookie.
    if (request.cookies.has(TOKEN_COOKIE_NAME)) response.cookies.delete(TOKEN_COOKIE_NAME);
    return response;
  }

  // Rol greșit -> pagina principală.
  if (!route.roles.includes(session.role)) return redirectTo(request, "/");

  return NextResponse.next();
}

export const config = {
  // Nu rulăm pe apelurile către backend (/api, /uploads), pe fișierele Next și pe cele statice.
  matcher: ["/((?!api|uploads|_next/static|_next/image|.*\\..*).*)"],
};
