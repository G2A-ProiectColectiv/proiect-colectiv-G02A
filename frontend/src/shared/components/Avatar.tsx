import Image from "next/image";
import styles from "./Avatar.module.css";
import { imageUrl } from "@/shared/utils/imageUrl";

interface AvatarProps {
  firstName: string;
  lastName: string;
  avatarUrl?: string | null;
  size?: "sm" | "lg";
}

// Afiseaza poza de profil a utilizatorului sau initialele acestuia.
export function Avatar({ firstName, lastName, avatarUrl, size = "sm", }: AvatarProps) {
  const initials =
    `${firstName.charAt(0)}${lastName.charAt(0)}`
      .toUpperCase() || "?";

  const imageSrc = imageUrl(avatarUrl);

  return (
    <div className={`${styles.avatar} ${styles[size]}`}>
      {imageSrc ? (
        <Image
          src={imageSrc}
          alt={`Poza de profil a utilizatorului ${firstName} ${lastName}`}
          fill
          sizes={size === "lg" ? "112px" : "32px"}
          unoptimized
          className={styles.image}
        />
      ) : (
        <span>{initials}</span>
      )}
    </div>
  );
}
