"use client";

import { useState, type FormEvent } from "react";
import { Button } from "@/shared/components/Button";
import { FormField } from "@/shared/components/FormField";
import { Icon } from "@/shared/components/Icon";
import { Input } from "@/shared/components/Input";
import { Select } from "@/shared/components/Select";
import { AMENITIES, AMENITY_LABELS } from "../constants/amenities";
import { PROPERTY_TYPES, PROPERTY_TYPE_LABELS } from "../constants/propertyTypes";
import type { ListingSearchFilters } from "../schemas/listingSearchSchema";
import styles from "./ListingFilters.module.css";

export interface ListingFiltersProps {
  filters: ListingSearchFilters;
  onApply: (filters: ListingSearchFilters) => void;
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

/** Toate filtrele din CAUT-01. Se aplică la „Aplică filtrele"; starea rămâne în URL. */
export function ListingFilters({ filters, onApply, onReset }: ListingFiltersProps) {
  const [open, setOpen] = useState(false);

  function onSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const data = new FormData(event.currentTarget);
    const type = text(data, "type");

    onApply({
      // `sort` și `view` nu fac parte din formular, deci le păstrăm.
      sort: filters.sort,
      view: filters.view,
      city: text(data, "city") || undefined,
      type: type || undefined,
      minPrice: numberOrUndefined(data, "minPrice"),
      maxPrice: numberOrUndefined(data, "maxPrice"),
      minRooms: numberOrUndefined(data, "minRooms"),
      minBathrooms: numberOrUndefined(data, "minBathrooms"),
      minArea: numberOrUndefined(data, "minArea"),
      maxArea: numberOrUndefined(data, "maxArea"),
      availableBy: text(data, "availableBy") || undefined,
      amenities: data.getAll("amenities").map(String) as ListingSearchFilters["amenities"],
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
        aria-controls="listing-filters-form"
        onClick={() => setOpen((value) => !value)}
      >
        Filtre
      </Button>

      {/* `key` remontează formularul când filtrele se schimbă din URL (ex. „Resetează"). */}
      <form
        key={JSON.stringify(filters)}
        id="listing-filters-form"
        className={`${styles.form} ${open ? styles.open : ""}`}
        onSubmit={onSubmit}
      >
        <FormField label="Oraș" htmlFor="f-city">
          <Input id="f-city" name="city" defaultValue={filters.city} placeholder="ex. Cluj-Napoca" />
        </FormField>

        <FormField label="Tip" htmlFor="f-type">
          <Select id="f-type" name="type" defaultValue={filters.type ?? ""}>
            <option value="">Orice tip</option>
            {PROPERTY_TYPES.map((type) => (
              <option key={type} value={type}>
                {PROPERTY_TYPE_LABELS[type]}
              </option>
            ))}
          </Select>
        </FormField>

        <div className={styles.pair}>
          <FormField label="Preț minim" htmlFor="f-min-price">
            <Input
              id="f-min-price"
              name="minPrice"
              type="number"
              min={0}
              inputMode="decimal"
              defaultValue={filters.minPrice}
            />
          </FormField>
          <FormField label="Preț maxim" htmlFor="f-max-price">
            <Input
              id="f-max-price"
              name="maxPrice"
              type="number"
              min={0}
              inputMode="decimal"
              defaultValue={filters.maxPrice}
            />
          </FormField>
        </div>

        <div className={styles.pair}>
          <FormField label="Camere (min.)" htmlFor="f-min-rooms">
            <Input
              id="f-min-rooms"
              name="minRooms"
              type="number"
              min={0}
              step={1}
              defaultValue={filters.minRooms}
            />
          </FormField>
          <FormField label="Băi (min.)" htmlFor="f-min-baths">
            <Input
              id="f-min-baths"
              name="minBathrooms"
              type="number"
              min={0}
              step={1}
              defaultValue={filters.minBathrooms}
            />
          </FormField>
        </div>

        <div className={styles.pair}>
          <FormField label="Suprafață min. (m²)" htmlFor="f-min-area">
            <Input
              id="f-min-area"
              name="minArea"
              type="number"
              min={0}
              inputMode="decimal"
              defaultValue={filters.minArea}
            />
          </FormField>
          <FormField label="Suprafață max. (m²)" htmlFor="f-max-area">
            <Input
              id="f-max-area"
              name="maxArea"
              type="number"
              min={0}
              inputMode="decimal"
              defaultValue={filters.maxArea}
            />
          </FormField>
        </div>

        <FormField label="Disponibil până la" htmlFor="f-available-by">
          <Input
            id="f-available-by"
            name="availableBy"
            type="date"
            defaultValue={filters.availableBy}
          />
        </FormField>

        <fieldset className={styles.amenities}>
          <legend className={styles.legend}>Dotări</legend>
          {AMENITIES.map((amenity) => (
            <label key={amenity} className={styles.checkbox}>
              <input
                type="checkbox"
                name="amenities"
                value={amenity}
                defaultChecked={filters.amenities?.includes(amenity)}
              />
              {AMENITY_LABELS[amenity]}
            </label>
          ))}
        </fieldset>

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
