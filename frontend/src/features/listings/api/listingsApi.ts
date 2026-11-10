import { httpClient } from "@/shared/api/httpClient";
import type { PageResponse } from "@/shared/types/api";
import type {
  Listing,
  ListingRequest,
  ListingStatusChoice,
  ListingSummary,
  MyListingsParams,
} from "../types";

export async function createListing(request: ListingRequest): Promise<Listing> {
  const { data } = await httpClient.post<Listing>("/listings", request);
  return data;
}

export async function updateListing(id: number, request: ListingRequest): Promise<Listing> {
  const { data } = await httpClient.put<Listing>(`/listings/${id}`, request);
  return data;
}

/** Anunțurile proprietarului conectat. Fără `status` se întorc toate, în afară de cele șterse. */
export async function getMine(
  params: MyListingsParams = {},
): Promise<PageResponse<ListingSummary>> {
  const { data } = await httpClient.get<PageResponse<ListingSummary>>("/listings/mine", { params });
  return data;
}

export async function getMineById(id: number): Promise<Listing> {
  const { data } = await httpClient.get<Listing>(`/listings/mine/${id}`);
  return data;
}

export async function changeStatus(id: number, status: ListingStatusChoice): Promise<Listing> {
  const { data } = await httpClient.patch<Listing>(`/listings/${id}/status`, { status });
  return data;
}

export async function remove(id: number): Promise<void> {
  await httpClient.delete(`/listings/${id}`);
}
