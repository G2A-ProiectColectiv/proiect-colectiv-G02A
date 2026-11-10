"use client";

import { useState } from "react";
import { Controller, useForm, useWatch } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { getApiErrorMessage, getApiErrorStatus, getFieldErrors } from "@/shared/api/apiError";
import { Button } from "@/shared/components/Button";
import { FormField } from "@/shared/components/FormField";
import { Icon } from "@/shared/components/Icon";
import { Input } from "@/shared/components/Input";
import { LinkButton } from "@/shared/components/LinkButton";
import { Select } from "@/shared/components/Select";
import { GoogleMapsLinkInput } from "@/features/location/components/GoogleMapsLinkInput";
import type { Coordinates } from "@/features/location/types";
import { AMENITIES, AMENITY_LABELS } from "../constants/amenities";
import { PROPERTY_TYPES, PROPERTY_TYPE_LABELS } from "../constants/propertyTypes";
import {
  EMPTY_LISTING_FORM,
  LISTING_LIMITS,
  listingFormSchema,
  type ListingFormValues,
} from "../schemas/listingSchemas";
import styles from "./ListingForm.module.css";

const FORM_FIELDS = Object.keys(EMPTY_LISTING_FORM) as (keyof ListingFormValues)[];

function isFormField(field: string): field is keyof ListingFormValues {
  return (FORM_FIELDS as string[]).includes(field);
}

/** „Detalii completate" nu ține cont de link (are propria bifă) și nici de dotări (sunt opționale). */
const detailsSchema = listingFormSchema.omit({ googleMapsUrl: true, amenities: true });

export interface ListingFormProps {
  defaultValues?: ListingFormValues;
  /** Coordonatele salvate (la editare), ca harta să apară imediat. */
  initialCoordinates?: Coordinates | null;
  submitLabel: string;
  cancelHref: string;
  /** Trimite datele la server. Dacă aruncă o eroare API, formularul o afișează la câmpul potrivit. */
  onSave: (values: ListingFormValues) => Promise<void>;
}

export function ListingForm({
  defaultValues = EMPTY_LISTING_FORM,
  initialCoordinates = null,
  submitLabel,
  cancelHref,
  onSave,
}: ListingFormProps) {
  const {
    register,
    control,
    handleSubmit,
    setError,
    setFocus,
    formState: { errors, isSubmitting },
  } = useForm<ListingFormValues>({
    resolver: zodResolver(listingFormSchema),
    defaultValues,
  });
  const [formError, setFormError] = useState<string | null>(null);
  const [locationConfirmed, setLocationConfirmed] = useState(initialCoordinates !== null);

  const values = useWatch({ control });
  const titleLength = (values.title ?? "").length;
  const detailsComplete = detailsSchema.safeParse(values).success;

  const onSubmit = handleSubmit(async (formValues) => {
    setFormError(null);
    try {
      await onSave(formValues);
    } catch (error) {
      // Linkul fără coordonate (422) se arată la câmpul de link, cu mesajul de la LOC-09.
      if (getApiErrorStatus(error) === 422) {
        setError("googleMapsUrl", { message: getApiErrorMessage(error) });
        setFocus("googleMapsUrl");
        return;
      }

      let handled = false;
      for (const [field, message] of Object.entries(getFieldErrors(error))) {
        if (isFormField(field)) {
          setError(field, { message });
          handled = true;
        }
      }
      if (!handled) setFormError(getApiErrorMessage(error));
    }
  });

  return (
    <form className={styles.form} onSubmit={onSubmit} noValidate>
      <div className={styles.layout}>
        <div className={styles.sections}>
          {formError && (
            <p className={styles.banner} role="alert">
              <Icon name="alert" size={20} />
              {formError}
            </p>
          )}

          <section className={styles.card} aria-labelledby="section-about">
            <h2 id="section-about" className={styles.sectionTitle}>
              <span className={styles.step}>1</span>
              Despre locuință
            </h2>
            <p className={styles.sectionHint}>
              Un titlu clar și o descriere sinceră aduc mai multe mesaje.
            </p>

            <div className={styles.fields}>
              <FormField label="Titlu" htmlFor="title" required error={errors.title?.message}>
                <Input
                  id="title"
                  maxLength={LISTING_LIMITS.title}
                  autoComplete="off"
                  invalid={!!errors.title}
                  aria-describedby={errors.title ? "title-error" : "title-counter"}
                  {...register("title")}
                />
                <p id="title-counter" className={styles.counter}>
                  {titleLength} / {LISTING_LIMITS.title} de caractere
                </p>
              </FormField>

              <FormField label="Tip" htmlFor="type" required error={errors.type?.message}>
                <Select
                  id="type"
                  invalid={!!errors.type}
                  aria-describedby={errors.type ? "type-error" : undefined}
                  {...register("type")}
                >
                  {PROPERTY_TYPES.map((type) => (
                    <option key={type} value={type}>
                      {PROPERTY_TYPE_LABELS[type]}
                    </option>
                  ))}
                </Select>
              </FormField>

              <FormField
                label="Descriere"
                htmlFor="description"
                required
                error={errors.description?.message}
              >
                <textarea
                  id="description"
                  rows={6}
                  className={`${styles.textarea} ${errors.description ? styles.invalid : ""}`}
                  aria-invalid={errors.description ? true : undefined}
                  aria-describedby={errors.description ? "description-error" : undefined}
                  {...register("description")}
                />
              </FormField>
            </div>
          </section>

          <section className={styles.card} aria-labelledby="section-details">
            <h2 id="section-details" className={styles.sectionTitle}>
              <span className={styles.step}>2</span>
              Preț și detalii
            </h2>

            <div className={styles.fields}>
              <div className={styles.row}>
                <FormField
                  label="Chirie lunară"
                  htmlFor="price"
                  required
                  error={errors.price?.message}
                >
                  <Input
                    id="price"
                    inputMode="decimal"
                    autoComplete="off"
                    invalid={!!errors.price}
                    aria-describedby={errors.price ? "price-error" : undefined}
                    {...register("price")}
                  />
                </FormField>

                <FormField
                  label="Moneda"
                  htmlFor="currency"
                  required
                  error={errors.currency?.message}
                >
                  <Select
                    id="currency"
                    invalid={!!errors.currency}
                    aria-describedby={errors.currency ? "currency-error" : undefined}
                    {...register("currency")}
                  >
                    <option value="RON">RON</option>
                    <option value="EUR">EUR (€)</option>
                  </Select>
                </FormField>

                <FormField
                  label="Disponibil de la"
                  htmlFor="availableFrom"
                  required
                  error={errors.availableFrom?.message}
                >
                  <Input
                    id="availableFrom"
                    type="date"
                    invalid={!!errors.availableFrom}
                    aria-describedby={errors.availableFrom ? "availableFrom-error" : undefined}
                    {...register("availableFrom")}
                  />
                </FormField>
              </div>

              <div className={styles.row}>
                <FormField label="Camere" htmlFor="rooms" required error={errors.rooms?.message}>
                  <Input
                    id="rooms"
                    inputMode="numeric"
                    autoComplete="off"
                    invalid={!!errors.rooms}
                    aria-describedby={errors.rooms ? "rooms-error" : undefined}
                    {...register("rooms")}
                  />
                </FormField>

                <FormField
                  label="Băi"
                  htmlFor="bathrooms"
                  required
                  error={errors.bathrooms?.message}
                >
                  <Input
                    id="bathrooms"
                    inputMode="numeric"
                    autoComplete="off"
                    invalid={!!errors.bathrooms}
                    aria-describedby={errors.bathrooms ? "bathrooms-error" : undefined}
                    {...register("bathrooms")}
                  />
                </FormField>

                <FormField
                  label="Suprafață (m²)"
                  htmlFor="areaSqm"
                  required
                  error={errors.areaSqm?.message}
                >
                  <Input
                    id="areaSqm"
                    inputMode="decimal"
                    autoComplete="off"
                    invalid={!!errors.areaSqm}
                    aria-describedby={errors.areaSqm ? "areaSqm-error" : undefined}
                    {...register("areaSqm")}
                  />
                </FormField>
              </div>
            </div>
          </section>

          <section className={styles.card} aria-labelledby="section-location">
            <h2 id="section-location" className={styles.sectionTitle}>
              <span className={styles.step}>3</span>
              Locație
            </h2>
            <p className={styles.sectionHint}>
              Adresa o scrii tu. Poziția pe hartă o luăm din linkul Google Maps.
            </p>

            <div className={styles.fields}>
              <div className={styles.row2}>
                <FormField
                  label="Adresă"
                  htmlFor="address"
                  required
                  error={errors.address?.message}
                >
                  <Input
                    id="address"
                    autoComplete="street-address"
                    maxLength={LISTING_LIMITS.address}
                    invalid={!!errors.address}
                    aria-describedby={errors.address ? "address-error" : undefined}
                    {...register("address")}
                  />
                </FormField>

                <FormField label="Oraș" htmlFor="city" required error={errors.city?.message}>
                  <Input
                    id="city"
                    autoComplete="address-level2"
                    maxLength={LISTING_LIMITS.city}
                    invalid={!!errors.city}
                    aria-describedby={errors.city ? "city-error" : undefined}
                    {...register("city")}
                  />
                </FormField>
              </div>

              <Controller
                control={control}
                name="googleMapsUrl"
                render={({ field, fieldState }) => (
                  <GoogleMapsLinkInput
                    id="googleMapsUrl"
                    value={field.value}
                    onChange={field.onChange}
                    onBlur={field.onBlur}
                    inputRef={field.ref}
                    error={fieldState.error?.message}
                    initialCoordinates={initialCoordinates}
                    onLocationChange={(coordinates) => setLocationConfirmed(coordinates !== null)}
                  />
                )}
              />
            </div>
          </section>

          <section className={styles.card} aria-labelledby="section-amenities">
            <h2 id="section-amenities" className={styles.sectionTitle}>
              <span className={styles.step}>4</span>
              Dotări
            </h2>

            <Controller
              control={control}
              name="amenities"
              render={({ field }) => (
                <div className={styles.amenities} role="group" aria-labelledby="section-amenities">
                  {AMENITIES.map((amenity) => {
                    const checked = field.value.includes(amenity);
                    return (
                      <label
                        key={amenity}
                        className={`${styles.chip} ${checked ? styles.chipOn : ""}`}
                      >
                        <input
                          type="checkbox"
                          className={styles.chipInput}
                          checked={checked}
                          onChange={() =>
                            field.onChange(
                              checked
                                ? field.value.filter((value) => value !== amenity)
                                : [...field.value, amenity],
                            )
                          }
                        />
                        {checked && <Icon name="check" size={16} />}
                        {AMENITY_LABELS[amenity]}
                      </label>
                    );
                  })}
                </div>
              )}
            />
          </section>
        </div>

        <aside className={styles.aside}>
          <h2 className={styles.asideTitle}>
            {detailsComplete && locationConfirmed ? "Gata de publicare" : "Completează anunțul"}
          </h2>
          <ul className={styles.checklist}>
            <li className={`${styles.check} ${detailsComplete ? styles.done : ""}`}>
              <span className={styles.checkIcon} aria-hidden="true">
                {detailsComplete && <Icon name="check" size={14} />}
              </span>
              Detalii completate
            </li>
            <li className={`${styles.check} ${locationConfirmed ? styles.done : ""}`}>
              <span className={styles.checkIcon} aria-hidden="true">
                {locationConfirmed && <Icon name="check" size={14} />}
              </span>
              Locație confirmată
            </li>
          </ul>

          <div className={styles.asideActions}>
            <Button type="submit" fullWidth loading={isSubmitting}>
              {submitLabel}
            </Button>
            <LinkButton href={cancelHref} variant="text" className={styles.cancel}>
              Anulează
            </LinkButton>
          </div>
        </aside>
      </div>
    </form>
  );
}
