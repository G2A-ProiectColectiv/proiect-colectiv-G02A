import { Icon } from "@/shared/components/Icon";
import { LinkButton } from "@/shared/components/LinkButton";
import { HeroSearch } from "../components/HeroSearch";
import { ListingCardPreview, type ListingCardPreviewProps } from "../components/ListingCardPreview";
import { MapPreview } from "../components/MapPreview";
import styles from "./HomeView.module.css";
import { AccountDeletedMessage } from "@/features/profile/components/AccountDeletedMessage";
import { Suspense } from "react";

const PERKS = ["Căutare fără cont", "Locație pe hartă", "Contact direct"];

/*
 * Anunțuri de exemplu, doar pentru aspect. Lista reală (`GET /api/listings`) se
 * conectează în etapa 1.5, când există și paginile de căutare.
 */
const SAMPLE_LISTINGS: ListingCardPreviewProps[] = [
  {
    href: "/listings",
    price: "380",
    currency: "€",
    title: "Garsonieră modernă, lângă metrou",
    meta: ["1 cam.", "1 baie", "32 m²"],
    location: "București, Drumul Taberei",
    badge: "Disponibil acum",
    badgeTone: "available",
    tone: "green",
  },
  {
    href: "/listings",
    price: "450",
    currency: "€",
    title: "Apartament 2 camere, mobilat complet",
    meta: ["2 cam.", "1 baie", "54 m²"],
    location: "Cluj-Napoca, Mărăști",
    badge: "Disponibil acum",
    badgeTone: "available",
    tone: "yellow",
  },
  {
    href: "/listings",
    price: "1.100",
    currency: "RON",
    title: "Cameră în apartament de studenți",
    meta: ["1 cam.", "1 baie", "16 m²"],
    location: "Iași, Copou",
    badge: "Din 1 dec.",
    badgeTone: "soon",
    tone: "blue",
  },
  {
    href: "/listings",
    price: "900",
    currency: "€",
    title: "Casă cu curte, 4 camere",
    meta: ["4 cam.", "2 băi", "120 m²"],
    location: "Timișoara, Fabric",
    badge: "Disponibil acum",
    badgeTone: "available",
    tone: "pink",
  },
];

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

        <div className={styles.cards}>
          {SAMPLE_LISTINGS.map((listing) => (
            <ListingCardPreview key={listing.title} {...listing} />
          ))}
        </div>
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
            <LinkButton href="/my-requests/new" variant="accent">
              Publică o cerere
            </LinkButton>
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
            <LinkButton href="/my-listings/new" variant="primary">
              Publică un anunț
            </LinkButton>
            <LinkButton href="/requests" variant="text">
              Vezi cererile
            </LinkButton>
          </div>
        </article>
      </section>
    </main>
  );
}
