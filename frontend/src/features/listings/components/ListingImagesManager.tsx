"use client";

import { useState, type ChangeEvent, type DragEvent } from "react";
import Image from "next/image";
import { getApiErrorMessage } from "@/shared/api/apiError";
import { Icon } from "@/shared/components/Icon";
import { Spinner } from "@/shared/components/Spinner";
import { imageUrl } from "@/shared/utils/imageUrl";
import { validateImageFile } from "@/shared/utils/validateImageFile";
import {
  useDeleteListingImage,
  useReorderListingImages,
  useSetPrimaryListingImage,
  useUploadListingImages,
} from "../hooks/useListingImages";
import { MAX_LISTING_IMAGES, type ListingImage } from "../types";
import styles from "./ListingImagesManager.module.css";

/** `imageUrl` poate întoarce `null` pentru o cale goală, iar `next/image` are nevoie de un text. */
function photoSrc(path: string): string {
  return imageUrl(path) ?? "";
}

export interface ListingImagesManagerProps {
  listingId: number;
  images: ListingImage[];
}

/** Încărcare multiplă, ștergere, poză principală (click pe poză) și ordine (tragere sau săgeți). */
export function ListingImagesManager({ listingId, images }: ListingImagesManagerProps) {
  const upload = useUploadListingImages(listingId);
  const remove = useDeleteListingImage(listingId);
  const setPrimary = useSetPrimaryListingImage(listingId);
  const reorder = useReorderListingImages(listingId);

  const [error, setError] = useState<string | null>(null);
  const [isDraggingFiles, setIsDraggingFiles] = useState(false);
  const [draggedId, setDraggedId] = useState<number | null>(null);

  const sorted = [...images].sort((a, b) => a.position - b.position);
  const busy = upload.isPending || remove.isPending || setPrimary.isPending || reorder.isPending;
  const remaining = MAX_LISTING_IMAGES - images.length;

  function addFiles(fileList: FileList | null) {
    setError(null);
    const files = fileList ? Array.from(fileList) : [];
    if (files.length === 0) return;

    if (remaining <= 0) {
      setError(`Un anunț poate avea cel mult ${MAX_LISTING_IMAGES} poze.`);
      return;
    }
    if (files.length > remaining) {
      setError(
        `Poți adăuga încă ${remaining} ${remaining === 1 ? "poză" : "poze"} (maximum ${MAX_LISTING_IMAGES} pe anunț).`,
      );
      return;
    }

    for (const file of files) {
      const problem = validateImageFile(file);
      if (problem) {
        setError(problem);
        return;
      }
    }

    upload.mutate(files, { onError: (failure) => setError(getApiErrorMessage(failure)) });
  }

  function onInputChange(event: ChangeEvent<HTMLInputElement>) {
    addFiles(event.target.files);
    // Altfel aceeași poză, aleasă a doua oară, nu mai declanșează evenimentul.
    event.target.value = "";
  }

  function onDropFiles(event: DragEvent<HTMLLabelElement>) {
    event.preventDefault();
    setIsDraggingFiles(false);
    if (!busy) addFiles(event.dataTransfer.files);
  }

  function onError(failure: unknown) {
    setError(getApiErrorMessage(failure));
  }

  function makePrimary(image: ListingImage) {
    if (image.primary || busy) return;
    setError(null);
    setPrimary.mutate(image.id, { onError });
  }

  function deleteImage(image: ListingImage) {
    setError(null);
    remove.mutate(image.id, { onError });
  }

  /** Mută poza `imageId` pe locul pozei `targetId` și trimite noua ordine la server. */
  function moveTo(imageId: number, targetId: number) {
    if (imageId === targetId || busy) return;

    const order = sorted.map((image) => image.id);
    const from = order.indexOf(imageId);
    const to = order.indexOf(targetId);
    if (from < 0 || to < 0) return;

    order.splice(to, 0, order.splice(from, 1)[0]);
    setError(null);
    reorder.mutate(order, { onError });
  }

  function moveBy(image: ListingImage, offset: -1 | 1) {
    const index = sorted.findIndex((candidate) => candidate.id === image.id);
    const target = sorted[index + offset];
    if (target) moveTo(image.id, target.id);
  }

  return (
    <div className={styles.manager}>
      <div className={styles.header}>
        <p className={styles.hint}>
          Până la {MAX_LISTING_IMAGES} poze, jpg, png sau webp, maximum 5 MB fiecare. Prima poză
          este cea principală.
        </p>
        <span className={styles.counter} aria-live="polite">
          {images.length} / {MAX_LISTING_IMAGES}
        </span>
      </div>

      {remaining > 0 && (
        <label
          className={`${styles.dropzone} ${isDraggingFiles ? styles.dragging : ""} ${busy ? styles.disabled : ""}`}
          onDragOver={(event) => {
            // Reordonarea pozelor are propria tragere; zona de încărcare reacționează doar la fișiere.
            if (event.dataTransfer.types.includes("Files")) {
              event.preventDefault();
              setIsDraggingFiles(true);
            }
          }}
          onDragLeave={() => setIsDraggingFiles(false)}
          onDrop={onDropFiles}
        >
          <input
            type="file"
            className={styles.fileInput}
            accept="image/jpeg,image/png,image/webp"
            multiple
            disabled={busy}
            onChange={onInputChange}
          />
          {upload.isPending ? (
            <Spinner size="md" label="Se încarcă pozele" />
          ) : (
            <Icon name="upload" size={28} />
          )}
          <span className={styles.dropTitle}>
            {upload.isPending ? "Se încarcă pozele…" : "Trage pozele aici sau alege din calculator"}
          </span>
        </label>
      )}

      {error && (
        <p className={styles.error} role="alert">
          <Icon name="alert" size={18} />
          {error}
        </p>
      )}

      {sorted.length > 0 && (
        <>
          <p className={styles.hint}>
            Trage o poză ca să schimbi ordinea sau folosește săgețile. Apasă pe o poză ca să o faci
            principală.
          </p>
          <ul className={styles.grid}>
            {sorted.map((image, index) => (
              <li
                key={image.id}
                className={`${styles.item} ${draggedId === image.id ? styles.dragged : ""}`}
                draggable={!busy}
                onDragStart={(event) => {
                  event.dataTransfer.effectAllowed = "move";
                  setDraggedId(image.id);
                }}
                onDragEnd={() => setDraggedId(null)}
                onDragOver={(event) => {
                  if (draggedId !== null) event.preventDefault();
                }}
                onDrop={(event) => {
                  event.preventDefault();
                  if (draggedId !== null) moveTo(draggedId, image.id);
                  setDraggedId(null);
                }}
              >
                <button
                  type="button"
                  className={styles.photo}
                  aria-label={
                    image.primary
                      ? `Poza ${index + 1}, principală`
                      : `Poza ${index + 1}. Apasă ca să o faci principală`
                  }
                  aria-pressed={image.primary}
                  disabled={busy}
                  onClick={() => makePrimary(image)}
                >
                  <Image
                    src={photoSrc(image.url)}
                    alt=""
                    fill
                    sizes="(max-width: 480px) 45vw, 180px"
                    unoptimized
                    draggable={false}
                    className={styles.img}
                  />
                  {image.primary && <span className={styles.badge}>Principală</span>}
                </button>

                <div className={styles.tools}>
                  <button
                    type="button"
                    className={styles.tool}
                    aria-label="Mută poza înainte"
                    disabled={busy || index === 0}
                    onClick={() => moveBy(image, -1)}
                  >
                    <Icon name="chevron-left" size={18} />
                  </button>
                  <button
                    type="button"
                    className={styles.tool}
                    aria-label="Mută poza înapoi"
                    disabled={busy || index === sorted.length - 1}
                    onClick={() => moveBy(image, 1)}
                  >
                    <Icon name="chevron-right" size={18} />
                  </button>
                  <button
                    type="button"
                    className={`${styles.tool} ${styles.danger}`}
                    aria-label="Șterge poza"
                    disabled={busy}
                    onClick={() => deleteImage(image)}
                  >
                    <Icon name="trash" size={18} />
                  </button>
                </div>
              </li>
            ))}
          </ul>
        </>
      )}
    </div>
  );
}
