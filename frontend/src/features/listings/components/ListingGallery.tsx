"use client";

import { useState } from "react";
import Image from "next/image";
import { Icon } from "@/shared/components/Icon";
import { imageUrl } from "@/shared/utils/imageUrl";
import type { ListingImage } from "../types";
import styles from "./ListingGallery.module.css";

export interface ListingGalleryProps {
  images: ListingImage[];
  title: string;
}

/** Poza principală mare, cu miniaturi dedesubt. Prima afișată e cea marcată principală. */
export function ListingGallery({ images, title }: ListingGalleryProps) {
  const ordered = [...images].sort(
    (a, b) => Number(b.primary) - Number(a.primary) || a.position - b.position,
  );
  const [selectedId, setSelectedId] = useState<number | null>(null);
  const selected = ordered.find((image) => image.id === selectedId) ?? ordered[0];

  if (!selected) {
    return (
      <div className={`${styles.main} ${styles.empty}`}>
        <Icon name="home" size={56} className={styles.placeholder} />
      </div>
    );
  }

  return (
    <div className={styles.gallery}>
      <div className={styles.main}>
        <Image
          src={imageUrl(selected.url) ?? ""}
          alt={title}
          fill
          sizes="(max-width: 900px) 100vw, 640px"
          unoptimized
          priority
          className={styles.img}
        />
      </div>

      {ordered.length > 1 && (
        <ul className={styles.thumbs} aria-label="Pozele anunțului">
          {ordered.map((image, index) => (
            <li key={image.id}>
              <button
                type="button"
                className={`${styles.thumb} ${image.id === selected.id ? styles.active : ""}`}
                aria-label={`Poza ${index + 1}`}
                aria-current={image.id === selected.id}
                onClick={() => setSelectedId(image.id)}
              >
                <Image
                  src={imageUrl(image.url) ?? ""}
                  alt=""
                  fill
                  sizes="96px"
                  unoptimized
                  className={styles.img}
                />
              </button>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
