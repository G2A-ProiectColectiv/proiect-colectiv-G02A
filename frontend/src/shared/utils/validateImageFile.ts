const MAX_SIZE = 5 * 1024 * 1024;

const ALLOWED_TYPES = [
  "image/jpeg",
  "image/png",
  "image/jpg",
];

// Verifica formatul si dimensiunea imaginii selectate.
export function validateImageFile(file: File): string | null {
  if (!ALLOWED_TYPES.includes(file.type)) {
    return "Sunt acceptate doar imagini JPG, PNG sau JPEG.";
  }

  if (file.size === 0 || file.size > MAX_SIZE) {
    return "Imaginea trebuie să aibă maximum 5 MB.";
  }

  return null;
}
