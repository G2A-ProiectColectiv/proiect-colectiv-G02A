"use client";

import { useState, type FormEvent } from "react";
import { PROPERTY_TYPES, PROPERTY_TYPE_LABELS } from "@/features/listings/constants/propertyTypes";
import { Button } from "@/shared/components/Button";
import { FormField } from "@/shared/components/FormField";
import { Icon } from "@/shared/components/Icon";
import { Input } from "@/shared/components/Input";
import { Select } from "@/shared/components/Select";
import type { RequestSearchFilters } from "../schemas/requestSearchSchema";
import styles from "./RequestFilters.module.css";

export interface RequestFiltersProps {
  filters: RequestSearchFilters;
  onApply: (filters: RequestSearchFilters) => void;
  onReset: () => void;
}

function text(data: FormData, name: string): string {
  return String(data.get(name) ?? "").trim();
}

function numberOrUndefined(data: FormData, name: string): number | undefined {
  const value = text(data, name).replace(",", ".");
  if (!value) return undefined;
  const number = Number(value);
  return Number.isFinite(number) && number >= 0 ? number : undefined;
}

/**
 * Filtrele cererilor (CAUT-02). Intervalul de buget găsește cererile compatibile cu un preț:
 * o cerere apare dacă bugetul ei se suprapune cu intervalul ales.
 */
export function RequestFilters({ filters, onApply, onReset }: RequestFiltersProps) {
  const [open, setOpen] = useState(false);

  function onSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const data = new FormData(event.currentTarget);

    onApply({
      // `sort` nu face parte din formular, deci îl păstrăm.
      sort: filters.sort,
      type: text(data, "type") || undefined,
      city: text(data, "city") || undefined,
      area: text(data, "area") || undefined,
      minBudget: numberOrUndefined(data, "minBudget"),
      maxBudget: numberOrUndefined(data, "maxBudget"),
      minRooms: numberOrUndefined(data, "minRooms"),
      desiredFrom: text(data, "desiredFrom") || undefined,
    });
    setOpen(false);
  }

  return (
    <aside className={styles.wrapper} aria-label="Filtre">
      <Button
        variant="secondary"
        size="sm"
        className={styles.toggle}
        icon={<Icon name="sliders" size={18} />}
        aria-expanded={open}
        aria-controls="request-filters-form"
        onClick={() => setOpen((value) => !value)}
      >
        Filtre
      </Button>

      {/* `key` remontează formularul când filtrele se schimbă din URL (ex. „Resetează"). */}
      <form
        key={JSON.stringify(filters)}
        id="request-filters-form"
        className={`${styles.form} ${open ? styles.open : ""}`}
        onSubmit={onSubmit}
      >
        <FormField label="Oraș" htmlFor="rf-city">
          <Input id="rf-city" name="city" defaultValue={filters.city} placeholder="ex. Cluj-Napoca" />
        </FormField>

        <FormField label="Zonă" htmlFor="rf-area">
          <Input id="rf-area" name="area" defaultValue={filters.area} placeholder="ex. Mănăștur" />
        </FormField>

        <FormField label="Tip dorit" htmlFor="rf-type">
          <Select id="rf-type" name="type" defaultValue={filters.type ?? ""}>
            <option value="">Orice tip</option>
            {PROPERTY_TYPES.map((type) => (
              <option key={type} value={type}>
                {PROPERTY_TYPE_LABELS[type]}
              </option>
            ))}
          </Select>
        </FormField>

        <div className={styles.pair}>
          <FormField label="Buget de la" htmlFor="rf-min-budget">
            <Input
              id="rf-min-budget"
              name="minBudget"
              type="number"
              min={0}
              inputMode="decimal"
              defaultValue={filters.minBudget}
            />
          </FormField>
          <FormField label="Buget până la" htmlFor="rf-max-budget">
            <Input
              id="rf-max-budget"
              name="maxBudget"
              type="number"
              min={0}
              inputMode="decimal"
              defaultValue={filters.maxBudget}
            />
          </FormField>
        </div>

        <FormField label="Camere (min.)" htmlFor="rf-min-rooms">
          <Input
            id="rf-min-rooms"
            name="minRooms"
            type="number"
            min={0}
            step={1}
            defaultValue={filters.minRooms}
          />
        </FormField>

        <FormField
          label="Începe cel mai devreme la"
          htmlFor="rf-desired-from"
          hint="Data de la care locuința ta e disponibilă."
        >
          <Input
            id="rf-desired-from"
            name="desiredFrom"
            type="date"
            defaultValue={filters.desiredFrom}
          />
        </FormField>

        <div className={styles.actions}>
          <Button type="submit" fullWidth>
            Aplică filtrele
          </Button>
          <Button variant="text" fullWidth onClick={onReset}>
            Resetează
          </Button>
        </div>
      </form>
    </aside>
  );
}
