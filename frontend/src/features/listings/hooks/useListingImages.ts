"use client";

import { useMutation, useQueryClient } from "@tanstack/react-query";
import { remove, reorder, setPrimary, upload } from "../api/listingImagesApi";
import { listingKeys } from "./listingKeys";

/*
 * Toate operațiile pe poze reîncarcă anunțul (și listele, unde apare poza principală),
 * ca interfața să arate exact ce e salvat pe server.
 */
function useRefreshListings() {
  const queryClient = useQueryClient();
  return () => queryClient.invalidateQueries({ queryKey: listingKeys.all });
}

export function useUploadListingImages(listingId: number) {
  const refresh = useRefreshListings();
  return useMutation({
    mutationFn: (files: File[]) => upload(listingId, files),
    onSuccess: refresh,
  });
}

export function useDeleteListingImage(listingId: number) {
  const refresh = useRefreshListings();
  return useMutation({
    mutationFn: (imageId: number) => remove(listingId, imageId),
    onSuccess: refresh,
  });
}

export function useSetPrimaryListingImage(listingId: number) {
  const refresh = useRefreshListings();
  return useMutation({
    mutationFn: (imageId: number) => setPrimary(listingId, imageId),
    onSuccess: refresh,
  });
}

export function useReorderListingImages(listingId: number) {
  const refresh = useRefreshListings();
  return useMutation({
    mutationFn: (imageIds: number[]) => reorder(listingId, imageIds),
    onSuccess: refresh,
  });
}
