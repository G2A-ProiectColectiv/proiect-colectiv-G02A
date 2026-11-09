"use client";

import type { FormEvent } from "react";
import { useRouter } from "next/navigation";
import { Button } from "@/shared/components/Button";
import { FormField } from "@/shared/components/FormField";
import { Icon } from "@/shared/components/Icon";
import { Input } from "@/shared/components/Input";
import { Select } from "@/shared/components/Select";
import styles from "./HeroSearch.module.css";

const TYPE_OPTIONS = [
  { value: "", label: "Orice tip" },
  { value: "APARTMENT", label: "Apartament" },
  { value: "STUDIO", label: "Garsonieră" },
  { value: "ROOM", label: "Cameră" },
  { value: "HOUSE", label: "Casă" },
  { value: "OTHER", label: "Altele" },
];

const PARAMS = ["city", "type", "maxPrice"] as const;

/** Căutare rapidă: trimite la `/listings` doar filtrele completate (nu `type=` gol, care ar da 400). */
export function HeroSearch() {
  const router = useRouter();

  function onSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const data = new FormData(event.currentTarget);
    const params = new URLSearchParams();

    for (const name of PARAMS) {
      const value = String(data.get(name) ?? "").trim();
      if (value) params.set(name, value);
    }

    const query = params.toString();
    router.push(query ? `/listings?${query}` : "/listings");
  }

  return (
    <form className={styles.card} onSubmit={onSubmit} role="search" aria-label="Căutare rapidă">
      <FormField label="Oraș" htmlFor="hero-city" className={styles.city}>
        <div className={styles.inputWrap}>
          <Icon name="pin" size={18} className={styles.leadingIcon} />
          <Input
            id="hero-city"
            name="city"
            placeholder="ex. Cluj-Napoca"
            autoComplete="address-level2"
            className={styles.cityInput}
          />
        </div>
      </FormField>

      <FormField label="Tip" htmlFor="hero-type" className={styles.type}>
        <Select id="hero-type" name="type" defaultValue="">
          {TYPE_OPTIONS.map((option) => (
            <option key={option.value} value={option.value}>
              {option.label}
            </option>
          ))}
        </Select>
      </FormField>

      <FormField label="Preț maxim" htmlFor="hero-max-price" className={styles.price}>
        <div className={styles.inputWrap}>
          <Input
            id="hero-max-price"
            name="maxPrice"
            type="number"
            inputMode="numeric"
            min={0}
            placeholder="ex. 500"
            className={styles.priceInput}
          />
          <span className={styles.suffix} aria-hidden="true">
            €
          </span>
        </div>
      </FormField>

      <Button type="submit" icon={<Icon name="search" size={18} />} className={styles.submit}>
        Caută
      </Button>
    </form>
  );
}
