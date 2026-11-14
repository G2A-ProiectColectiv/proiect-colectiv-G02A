import { Icon } from "@/shared/components/Icon";
import { LinkButton } from "@/shared/components/LinkButton";
import { HeroSearch } from "../components/HeroSearch";
import { RecentListings } from "../components/RecentListings";
import { MapPreview } from "../components/MapPreview";
import { RoleRestrictedLink } from "../components/RoleRestrictedLink";
import styles from "./HomeView.module.css";
import { AccountDeletedMessage } from "@/features/profile/components/AccountDeletedMessage";
import { Suspense } from "react";

const PERKS = ["Căutare fără cont", "Locație pe hartă", "Contact direct"];

export function HomeView() {
  return (
    <main className={styles.page}>
      <Suspense fallback={null}>
        <AccountDeletedMessage />
      </Suspense>
      <section className={styles.hero}>
        <div className={styles.heroText}>
          <h1 className={styles.title}>
            Găsește locuința potrivită,{" "}
            <span className={styles.highlight}>direct de la proprietar</span>.
          </h1>
          <p className={styles.lead}>
            Toate detaliile sunt la vedere de la început: preț, dotări, poze și locația exactă pe
            hartă. Fără anunțuri vechi sau incomplete.
          </p>

          <HeroSearch />

          <ul className={styles.perks}>
            {PERKS.map((perk) => (
              <li key={perk} className={styles.perk}>
                <Icon name="check" size={16} />
                {perk}
              </li>
            ))}
          </ul>
        </div>

        <MapPreview />
      </section>

      <section className={styles.recent} aria-labelledby="recent-title">
        <div className={styles.sectionHeader}>
          <h2 id="recent-title" className={styles.sectionTitle}>
            Anunțuri recente
          </h2>
          <LinkButton href="/listings" variant="text">
            Vezi toate
            <Icon name="arrow-right" size={18} />
          </LinkButton>
        </div>

        <RecentListings />
      </section>

      <section className={styles.ctas}>
        <article className={`${styles.cta} ${styles.ctaTenant}`}>
          <span className={`${styles.pill} ${styles.pillOnDark}`}>Pentru chiriași</span>
          <h2 className={styles.ctaTitle}>Nu găsești ce cauți? Spune tu ce vrei.</h2>
          <p className={styles.ctaText}>
            Publică o cerere cu bugetul, zona și perioada, iar proprietarii o pot vedea și te pot
            contacta.
          </p>
          <div className={styles.ctaActions}>
            <RoleRestrictedLink
              href="/my-requests/new"
              variant="accent"
              blockedRole="LANDLORD"
              warningTitle="Nu poți publica o cerere"
              warningMessage="Contul tău este de proprietar, iar cererile de închiriere pot fi publicate doar de chiriași. Pentru a publica o cerere, autentifică-te cu un cont de chiriaș."
            >
              Publică o cerere
            </RoleRestrictedLink>
            <LinkButton href="/listings" variant="textOnDark">
              Caută anunțuri
            </LinkButton>
          </div>
        </article>

        <article className={`${styles.cta} ${styles.ctaLandlord}`}>
          <span className={`${styles.pill} ${styles.pillAccent}`}>Pentru proprietari</span>
          <h2 className={styles.ctaTitle}>Ajungi mai ușor la chiriașul potrivit.</h2>
          <p className={styles.ctaText}>
            Publică anunțul cu poze și locație, apoi caută printre cererile chiriașilor și alege
            cine ți se potrivește.
          </p>
          <div className={styles.ctaActions}>
            <RoleRestrictedLink
              href="/my-listings/new"
              variant="primary"
              blockedRole="TENANT"
              warningTitle="Nu poți publica un anunț"
              warningMessage="Contul tău este de chiriaș, iar anunțurile pot fi publicate doar de proprietari. Pentru a publica un anunț, autentifică-te cu un cont de proprietar."
            >
              Publică un anunț
            </RoleRestrictedLink>
            <LinkButton href="/requests" variant="text">
              Vezi cererile
            </LinkButton>
          </div>
        </article>
      </section>
    </main>
  );
}
