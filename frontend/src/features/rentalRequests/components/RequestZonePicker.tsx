"use client";

import { useState, type FocusEventHandler, type Ref } from "react";
import { GoogleMapsLinkInput } from "@/features/location/components/GoogleMapsLinkInput";
import type { Coordinates } from "@/features/location/types";
import { Icon } from "@/shared/components/Icon";
import { Map, RadiusCircle } from "@/shared/map";
import { RADIUS_KM } from "../types";
import styles from "./RequestZonePicker.module.css";

export interface RequestZonePickerProps {
  /** `id`-ul câmpului de link; slider-ul primește `{id}-radius`. */
  id: string;
  value: string;
  onChange: (value: string) => void;
  onBlur?: FocusEventHandler<HTMLInputElement>;
  inputRef?: Ref<HTMLInputElement>;
  /** Eroarea câmpului de link (ex. eroarea `422` la salvare). */
  error?: string;
  /** Centrul zonei deja salvat (la editare), ca harta să apară fără o cerere nouă. */
  initialCoordinates?: Coordinates | null;
  radiusKm: number;
  onRadiusChange: (radiusKm: number) => void;
  radiusError?: string;
}

const MAP_ZOOM = 13;

/**
 * Zona căutată, opțională (CER-03): linkul Google Maps dă centrul, slider-ul dă raza,
 * iar harta arată zona ca cerc. Slider-ul și harta apar doar după ce linkul a fost recunoscut.
 */
export function RequestZonePicker({
  id,
  value,
  onChange,
  onBlur,
  inputRef,
  error,
  initialCoordinates = null,
  radiusKm,
  onRadiusChange,
  radiusError,
}: RequestZonePickerProps) {
  const [center, setCenter] = useState<Coordinates | null>(initialCoordinates);
  const radiusId = `${id}-radius`;
  const showZone = center !== null && value.trim() !== "";

  return (
    <div className={styles.picker}>
      <GoogleMapsLinkInput
        id={id}
        value={value}
        onChange={onChange}
        onBlur={onBlur}
        inputRef={inputRef}
        error={error}
        initialCoordinates={initialCoordinates}
        onLocationChange={setCenter}
        label="Link Google Maps pentru centrul zonei (opțional)"
        required={false}
        showPreviewMap={false}
        foundHint="Verifică dacă cercul acoperă zona în care cauți."
        outsideRomaniaMessage="Locația pare să fie în afara României. Poți salva cererea, dar verifică încă o dată linkul."
      />

      {showZone && (
        <div className={styles.zone}>
          <div className={styles.radius}>
            <div className={styles.radiusHeader}>
              <label htmlFor={radiusId} className={styles.radiusLabel}>
                Rază de căutare
              </label>
              <output htmlFor={radiusId} className={styles.radiusValue}>
                {radiusKm} km
              </output>
            </div>

            <input
              id={radiusId}
              type="range"
              min={RADIUS_KM.min}
              max={RADIUS_KM.max}
              step={1}
              value={radiusKm}
              className={styles.slider}
              aria-valuetext={`${radiusKm} km`}
              aria-invalid={radiusError ? true : undefined}
              aria-describedby={radiusError ? `${radiusId}-error` : undefined}
              onChange={(event) => onRadiusChange(Number(event.target.value))}
            />

            <div className={styles.scale} aria-hidden="true">
              <span>{RADIUS_KM.min} km</span>
              <span>{RADIUS_KM.max} km</span>
            </div>

            {radiusError && (
              <p id={`${radiusId}-error`} className={styles.error} role="alert">
                <Icon name="alert" size={16} />
                {radiusError}
              </p>
            )}
          </div>

          <div className={styles.frame}>
            {/* Harta se recentrează singură când se schimbă centrul (vezi `BaseMap`). */}
            <Map
              center={[center.latitude, center.longitude]}
              zoom={MAP_ZOOM}
              markers={[{ id: "center", position: [center.latitude, center.longitude] }]}
              scrollWheelZoom={false}
              ariaLabel={`Zona căutată pe hartă: o rază de ${radiusKm} km în jurul centrului`}
            >
              <RadiusCircle center={[center.latitude, center.longitude]} radiusKm={radiusKm} />
            </Map>
          </div>
        </div>
      )}
    </div>
  );
}
