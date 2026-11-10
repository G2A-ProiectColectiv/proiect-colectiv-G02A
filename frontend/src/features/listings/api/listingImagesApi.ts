import { httpClient } from "@/shared/api/httpClient";
import type { ListingImage } from "../types";

/** Încarcă una sau mai multe poze (`multipart/form-data`, câmpul `files`). */
export async function upload(listingId: number, files: File[]): Promise<ListingImage[]> {
  const body = new FormData();
  files.forEach((file) => body.append("files", file));

  const { data } = await httpClient.post<ListingImage[]>(`/listings/${listingId}/images`, body);
  return data;
}

export async function remove(listingId: number, imageId: number): Promise<void> {
  await httpClient.delete(`/listings/${listingId}/images/${imageId}`);
}

export async function setPrimary(listingId: number, imageId: number): Promise<ListingImage[]> {
  const { data } = await httpClient.patch<ListingImage[]>(
    `/listings/${listingId}/images/${imageId}/primary`,
  );
  return data;
}

/** `imageIds` trebuie să conțină toate pozele anunțului, în ordinea dorită. */
export async function reorder(listingId: number, imageIds: number[]): Promise<ListingImage[]> {
  const { data } = await httpClient.put<ListingImage[]>(`/listings/${listingId}/images/order`, {
    imageIds,
  });
  return data;
}
