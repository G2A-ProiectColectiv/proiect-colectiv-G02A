"use client";

import { useEffect, useRef, useState, type MouseEvent } from "react";
import Link from "next/link";
import { usePathname } from "next/navigation";
import { useAuth } from "@/features/auth/context/AuthContext";
import type { Role } from "@/features/auth/types";
import { Button } from "@/shared/components/Button";
import { Icon } from "@/shared/components/Icon";
import styles from "./Navbar.module.css";
import { Avatar } from "@/shared/components/Avatar";

interface NavLink {
  href: string;
  label: string;
}

/*
 * Link-urile fiecărui rol. Unele pagini apar în etapele următoare (1.2–1.6),
 * deci până atunci aceste adrese duc la pagina 404.
 */
const LISTINGS_LINK: NavLink = { href: "/listings", label: "Anunțuri" };

const GUEST_LINKS: NavLink[] = [LISTINGS_LINK];

const LINKS_BY_ROLE: Record<Role, NavLink[]> = {
  TENANT: [LISTINGS_LINK, { href: "/my-requests", label: "Cererile mele" }],
  LANDLORD: [
    { href: "/requests", label: "Cereri" },
    { href: "/my-listings", label: "Anunțurile mele" },
  ],
  ADMIN: [{ href: "/admin/users", label: "Utilizatori" }],
};

const MENU_ID = "site-menu";

function isLinkActive(link: NavLink, pathname: string): boolean {
  return pathname === link.href || pathname.startsWith(`${link.href}/`);
}

export function Navbar() {
  const { user, isReady, logout } = useAuth();
  const pathname = usePathname();
  const headerRef = useRef<HTMLElement>(null);

  /*
   * Meniul de telefon (≤720px) se deschide cu butonul cu 3 linii. Reținem pe ce pagină
   * a fost deschis, deci se închide singur când navighezi în altă pagină.
   */
  const [openedAt, setOpenedAt] = useState<string | null>(null);
  const isOpen = openedAt === pathname;

  useEffect(() => {
    if (!isOpen) return;

    const close = () => setOpenedAt(null);
    const onKeyDown = (event: KeyboardEvent) => {
      if (event.key === "Escape") close();
    };
    const onPointerDown = (event: PointerEvent) => {
      if (!headerRef.current?.contains(event.target as Node)) close();
    };

    window.addEventListener("keydown", onKeyDown);
    window.addEventListener("pointerdown", onPointerDown);
    return () => {
      window.removeEventListener("keydown", onKeyDown);
      window.removeEventListener("pointerdown", onPointerDown);
    };
  }, [isOpen]);

  // Un click pe un link sau buton din meniu îl închide (și când linkul duce la aceeași pagină).
  function closeOnItemClick(event: MouseEvent<HTMLDivElement>) {
    if ((event.target as HTMLElement).closest("a, button")) setOpenedAt(null);
  }

  // Până când sesiunea e citită în browser nu afișăm link-uri, ca să nu apară cele de vizitator.
  const links = !isReady ? [] : user ? LINKS_BY_ROLE[user.role] : GUEST_LINKS;

  return (
    <header className={styles.header} ref={headerRef}>
      <div className={styles.inner}>
        <Link href="/" className={styles.brand}>
          <span className={styles.logo}>
            <Icon name="key" size={30} />
          </span>
          <span className={styles.brandName}>
            KeySwap<span className={styles.brandDot}>.</span>
          </span>
        </Link>

        <button
          type="button"
          className={styles.menuButton}
          aria-label={isOpen ? "Închide meniul" : "Deschide meniul"}
          aria-expanded={isOpen}
          aria-controls={MENU_ID}
          onClick={() => setOpenedAt(isOpen ? null : pathname)}
        >
          <span className={styles.bars} aria-hidden="true">
            <span className={styles.bar} />
            <span className={styles.bar} />
            <span className={styles.bar} />
          </span>
        </button>

        <div
          id={MENU_ID}
          className={`${styles.menu} ${isOpen ? styles.open : ""}`}
          onClick={closeOnItemClick}
        >
          <div className={styles.menuInner}>
            <nav className={styles.nav} aria-label="Navigare principală">
              {links.map((link) => {
                const isActive = isLinkActive(link, pathname);
                return (
                  <Link
                    key={link.href}
                    href={link.href}
                    className={`${styles.navLink} ${isActive ? styles.active : ""}`}
                    aria-current={isActive ? "page" : undefined}
                  >
                    {link.label}
                  </Link>
                );
              })}
            </nav>

            <div className={styles.actions}>
              {isReady && !user && (
                <>
                  <Link href="/login" className={styles.textAction}>
                    Autentificare
                  </Link>
                  <Link href="/register" className={styles.primaryAction}>
                    Creează cont
                  </Link>
                </>
              )}

              {isReady && user && (
                <>
                  {user.role === "LANDLORD" && (
                    <Link href="/my-listings/new" className={styles.primaryAction}>
                      Anunț nou
                    </Link>
                  )}


                  <Link href="/profile" className={styles.profile}>
                    <Avatar
                      firstName={user.firstName}
                      lastName={user.lastName}
                      avatarUrl={user.avatarUrl}
                      size="sm"
                    />
                    <span className={styles.userName}>
                      {user.role === "ADMIN" ? "Administrator" : user.firstName}
                    </span>
                  </Link>


                  <Button variant="text" size="sm" className={styles.logout} onClick={() => logout()}>
                    Deconectare
                  </Button>
                </>
              )}
            </div>
          </div>
        </div>
      </div>
    </header>
  );
}
