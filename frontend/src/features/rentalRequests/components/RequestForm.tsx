"use client";

import { useState } from "react";
import { Controller, useController, useForm, useWatch } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { PROPERTY_TYPES, PROPERTY_TYPE_LABELS } from "@/features/listings/constants/propertyTypes";
import type { Coordinates } from "@/features/location/types";
import { getApiErrorMessage, getApiErrorStatus, getFieldErrors } from "@/shared/api/apiError";
import { Button } from "@/shared/components/Button";
import { FormField } from "@/shared/components/FormField";
import { Icon } from "@/shared/components/Icon";
import { Input } from "@/shared/components/Input";
import { LinkButton } from "@/shared/components/LinkButton";
import { Select } from "@/shared/components/Select";
import {
  EMPTY_REQUEST_FORM,
  REQUEST_LIMITS,
  requestFormSchema,
  type RequestFormValues,
} from "../schemas/requestSchemas";
import { RequestZonePicker } from "./RequestZonePicker";
import styles from "./RequestForm.module.css";

const FORM_FIELDS = Object.keys(EMPTY_REQUEST_FORM) as (keyof RequestFormValues)[];

function isFormField(field: string): field is keyof RequestFormValues {
  return (FORM_FIELDS as string[]).includes(field);
}

export interface RequestFormProps {
  defaultValues?: RequestFormValues;
  /** Centrul zonei salvat (la editare), ca harta cu cercul să apară imediat. */
  initialCoordinates?: Coordinates | null;
  /** Titlul casetei din dreapta, de exemplu „Publică cererea". */
  asideTitle: string;
  submitLabel: string;
  cancelHref: string;
  /** Trimite datele la server. Dacă aruncă o eroare API, formularul o afișează la câmpul potrivit. */
  onSave: (values: RequestFormValues) => Promise<void>;
}

export function RequestForm({
  defaultValues = EMPTY_REQUEST_FORM,
  initialCoordinates = null,
  asideTitle,
  submitLabel,
  cancelHref,
  onSave,
}: RequestFormProps) {
  const {
    register,
    control,
    handleSubmit,
    setError,
    setFocus,
    formState: { errors, isSubmitting },
  } = useForm<RequestFormValues>({
    resolver: zodResolver(requestFormSchema),
    defaultValues,
  });
  const radius = useController({ control, name: "radiusKm" });
  const [formError, setFormError] = useState<string | null>(null);

  const title = useWatch({ control, name: "title" });
  const titleLength = (title ?? "").length;

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

          <section className={styles.card} aria-labelledby="section-what">
            <h2 id="section-what" className={styles.sectionTitle}>
              <span className={styles.step}>1</span>
              Ce cauți
            </h2>

            <div className={styles.fields}>
              <FormField label="Titlu" htmlFor="title" required error={errors.title?.message}>
                <Input
                  id="title"
                  maxLength={REQUEST_LIMITS.title}
                  autoComplete="off"
                  placeholder="De exemplu: Apartament 2 camere pentru o familie"
                  invalid={!!errors.title}
                  aria-describedby={errors.title ? "title-error" : "title-counter"}
                  {...register("title")}
                />
                <p id="title-counter" className={styles.counter}>
                  {titleLength} / {REQUEST_LIMITS.title} de caractere
                </p>
              </FormField>

              <div className={styles.row2}>
                <FormField
                  label="Tip de locuință"
                  htmlFor="desiredType"
                  required
                  error={errors.desiredType?.message}
                >
                  <Select
                    id="desiredType"
                    invalid={!!errors.desiredType}
                    aria-describedby={errors.desiredType ? "desiredType-error" : undefined}
                    {...register("desiredType")}
                  >
                    {PROPERTY_TYPES.map((type) => (
                      <option key={type} value={type}>
                        {PROPERTY_TYPE_LABELS[type]}
                      </option>
                    ))}
                  </Select>
                </FormField>

                <FormField label="Camere (opțional)" htmlFor="rooms" error={errors.rooms?.message}>
                  <Input
                    id="rooms"
                    inputMode="numeric"
                    autoComplete="off"
                    invalid={!!errors.rooms}
                    aria-describedby={errors.rooms ? "rooms-error" : undefined}
                    {...register("rooms")}
                  />
                </FormField>
              </div>

              <FormField
                label="Descriere"
                htmlFor="description"
                required
                error={errors.description?.message}
              >
                <textarea
                  id="description"
                  rows={5}
                  placeholder="Câte persoane sunteți, ce e important pentru voi, cât timp vreți să stați."
                  className={`${styles.textarea} ${errors.description ? styles.invalid : ""}`}
                  aria-invalid={errors.description ? true : undefined}
                  aria-describedby={errors.description ? "description-error" : undefined}
                  {...register("description")}
                />
              </FormField>
            </div>
          </section>

          <section className={styles.card} aria-labelledby="section-budget">
            <h2 id="section-budget" className={styles.sectionTitle}>
              <span className={styles.step}>2</span>
              Buget și perioadă
            </h2>

            <div className={styles.fields}>
              <div className={styles.row}>
                <FormField
                  label="Buget minim"
                  htmlFor="budgetMin"
                  required
                  error={errors.budgetMin?.message}
                >
                  <Input
                    id="budgetMin"
                    inputMode="decimal"
                    autoComplete="off"
                    invalid={!!errors.budgetMin}
                    aria-describedby={errors.budgetMin ? "budgetMin-error" : undefined}
                    {...register("budgetMin")}
                  />
                </FormField>

                <FormField
                  label="Buget maxim"
                  htmlFor="budgetMax"
                  required
                  error={errors.budgetMax?.message}
                >
                  <Input
                    id="budgetMax"
                    inputMode="decimal"
                    autoComplete="off"
                    invalid={!!errors.budgetMax}
                    aria-describedby={errors.budgetMax ? "budgetMax-error" : undefined}
                    {...register("budgetMax")}
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
                    <option value="EUR">EUR (€)</option>
                    <option value="RON">RON</option>
                  </Select>
                </FormField>
              </div>

              <div className={styles.row}>
                <FormField
                  label="Caut de la"
                  htmlFor="desiredFrom"
                  required
                  error={errors.desiredFrom?.message}
                >
                  <Input
                    id="desiredFrom"
                    type="date"
                    invalid={!!errors.desiredFrom}
                    aria-describedby={errors.desiredFrom ? "desiredFrom-error" : undefined}
                    {...register("desiredFrom")}
                  />
                </FormField>
              </div>
            </div>
          </section>

          <section className={styles.card} aria-labelledby="section-zone">
            <h2 id="section-zone" className={styles.sectionTitle}>
              <span className={styles.step}>3</span>
              Zona căutată
            </h2>
            <p className={styles.sectionHint}>
              Orașul este obligatoriu. Pe hartă poți marca opțional centrul zonei.
            </p>

            <div className={styles.fields}>
              <div className={styles.row2}>
                <FormField label="Oraș" htmlFor="city" required error={errors.city?.message}>
                  <Input
                    id="city"
                    autoComplete="address-level2"
                    maxLength={REQUEST_LIMITS.city}
                    invalid={!!errors.city}
                    aria-describedby={errors.city ? "city-error" : undefined}
                    {...register("city")}
                  />
                </FormField>

                <FormField label="Zonă (opțional)" htmlFor="area" error={errors.area?.message}>
                  <Input
                    id="area"
                    autoComplete="off"
                    maxLength={REQUEST_LIMITS.area}
                    placeholder="De exemplu: Titan"
                    invalid={!!errors.area}
                    aria-describedby={errors.area ? "area-error" : undefined}
                    {...register("area")}
                  />
                </FormField>
              </div>

              <Controller
                control={control}
                name="googleMapsUrl"
                render={({ field, fieldState }) => (
                  <RequestZonePicker
                    id="googleMapsUrl"
                    value={field.value}
                    onChange={field.onChange}
                    onBlur={field.onBlur}
                    inputRef={field.ref}
                    error={fieldState.error?.message}
                    initialCoordinates={initialCoordinates}
                    radiusKm={radius.field.value}
                    onRadiusChange={radius.field.onChange}
                    radiusError={radius.fieldState.error?.message}
                  />
                )}
              />
            </div>
          </section>
        </div>

        <aside className={styles.aside}>
          <h2 className={styles.asideTitle}>{asideTitle}</h2>
          <p className={styles.asideText}>
            Proprietarii văd cererea și te pot contacta. O poți închide oricând, după ce ai găsit o
            locuință.
          </p>

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
