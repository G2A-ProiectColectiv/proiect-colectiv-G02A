// Transforma calea avatarului primita de la backend intr-un URL utilizabil.
export function imageUrl(path: string | null | undefined): string | null {
  if (!path) {
    return null;
  }

  if (path.startsWith("http://") || path.startsWith("https://")) {
    return path;
  }

  const cleanPath = path.replace(/^\/?(uploads\/)?/, "");

  return `/uploads/${cleanPath}`;
}
