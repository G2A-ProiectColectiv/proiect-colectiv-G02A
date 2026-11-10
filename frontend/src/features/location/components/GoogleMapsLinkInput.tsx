"use client";

import { useState, type FocusEventHandler, type Ref } from "react";
import { getApiErrorMessage } from "@/shared/api/apiError";
import { FormField } from "@/shared/components/FormField";
import { Icon } from "@/shared/components/Icon";
import { Input } from "@/shared/components/Input";
import { Spinner } from "@/shared/components/Spinner";
import { useParseLocation } from "../hooks/useParseLocation";
import type { Coordinates } from "../types";
import { isInRomania } from "../utils/isInRomania";
import { LocationPreviewMap } from "./LocationPreviewMap";
import styles from "./GoogleMapsLinkInput.module.css";

export interface GoogleMapsLinkInputProps {
  id: string;
  value: string;
  onChange: (value: string) => void;
  onBlur?: FocusEventHandler<HTMLInputElement>;
  inputRef?: Ref<HTMLInputElement>;
  /** Eroarea de validare a formularului (ex. câmp gol sau eroarea `422` la salvare). */
  error?: string;
  /** Coordonatele deja salvate (la editare), ca harta să apară fără o cerere nouă. */
  initialCoordinates?: Coordinates | null;
  /** Apelat cu coordonatele găsite, sau cu `null` când linkul se schimbă ori nu are coordonate. */
  onLocationChange?: (coordinates: Coordinates | null) => void;
}

function formatCoordinate(value: number): string {
  return value.toFixed(5);
}

export function GoogleMapsLinkInput({
  id,
  value,
  onChange,
  onBlur,
  inputRef,
  error,
  initialCoordinates = null,
  onLocationChange,
}: GoogleMapsLinkInputProps) {
  const parse = useParseLocation();
  // Până când utilizatorul modifică linkul, arătăm coordonatele salvate.
  const [edited, setEdited] = useState(false);

  const coordinates = edited ? parse.coordinates : initialCoordinates;
  const parseError = edited && parse.error ? getApiErrorMessage(parse.error) : null;
  const showPending = edited && parse.isPending;

  function handleChange(next: string) {
    setEdited(true);
    onChange(next);
    onLocationChange?.(null);
    parse.schedule(next, {
      onSuccess: (found) => onLocationChange?.(found),
      onError: () => onLocationChange?.(null),
    });
  }

  const describedBy = error ? `${id}-error` : `${id}-status`;

  return (
    <div className={styles.wrapper}>
      <FormField label="Link Google Maps" htmlFor={id} required error={error}>
        <Input
          id={id}
          ref={inputRef}
          type="text"
          inputMode="url"
          autoComplete="off"
          spellCheck={false}
          maxLength={2000}
          placeholder="https://maps.app.goo.gl/… sau 44.4268, 26.1025"
          invalid={!!error || !!parseError}
          aria-describedby={describedBy}
          value={value}
          onChange={(event) => handleChange(event.target.value)}
          onBlur={onBlur}
        />
      </FormField>

      <details className={styles.help}>
        <summary className={styles.summary}>Cum obții linkul</summary>
        <ol className={styles.steps}>
          <li>Deschide Google Maps și caută locuința sau pune un pin pe ea.</li>
          <li>
            Apasă <strong>Distribuie</strong>.
          </li>
          <li>
            Apasă <strong>Copiază linkul</strong> și lipește-l aici.
          </li>
        </ol>
        <p className={styles.alternative}>
          Poți lipi și coordonate simple, de exemplu <code>44.4268, 26.1025</code> (click dreapta pe
          hartă în Google Maps, apoi copiază coordonatele).
        </p>
      </details>

      <div id={`${id}-status`} className={styles.status} aria-live="polite">
        {showPending && (
          <p className={styles.pending}>
            <Spinner size="sm" label="Caut locația" />
            Caut locația…
          </p>
        )}

        {parseError && (
          <p className={styles.parseError} role="alert">
            <Icon name="alert" size={18} />
            {parseError}
          </p>
        )}

        {coordinates && (
          <>
            <p className={styles.found}>
              <Icon name="check" size={18} />
              Locație găsită: {formatCoordinate(coordinates.latitude)},{" "}
              {formatCoordinate(coordinates.longitude)}. Verifică dacă pin-ul e pe locuința ta.
            </p>

            {!isInRomania(coordinates) && (
              <p className={styles.warning} role="status">
                <Icon name="alert" size={18} />
                Locația pare să fie în afara României. Poți salva anunțul, dar verifică încă o dată
                linkul.
              </p>
            )}

            <LocationPreviewMap coordinates={coordinates} />
          </>
        )}
      </div>
    </div>
  );
}
