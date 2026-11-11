import { httpClient } from "@/shared/api/httpClient";
import type { PageResponse } from "@/shared/types/api";
import type {
  MyRequestsParams,
  RentalRequest,
  RentalRequestPayload,
  RentalRequestSummary,
  RequestStatusChoice,
} from "../types";

export async function createRequest(payload: RentalRequestPayload): Promise<RentalRequest> {
  const { data } = await httpClient.post<RentalRequest>("/requests", payload);
  return data;
}

export async function updateRequest(
  id: number,
  payload: RentalRequestPayload,
): Promise<RentalRequest> {
  const { data } = await httpClient.put<RentalRequest>(`/requests/${id}`, payload);
  return data;
}

/** Cererile chiriașului conectat. Fără `status` se întorc toate, în afară de cele șterse. */
export async function getMine(
  params: MyRequestsParams = {},
): Promise<PageResponse<RentalRequestSummary>> {
  const { data } = await httpClient.get<PageResponse<RentalRequestSummary>>("/requests/mine", {
    params,
  });
  return data;
}

export async function getMineById(id: number): Promise<RentalRequest> {
  const { data } = await httpClient.get<RentalRequest>(`/requests/mine/${id}`);
  return data;
}

export async function changeStatus(
  id: number,
  status: RequestStatusChoice,
): Promise<RentalRequest> {
  const { data } = await httpClient.patch<RentalRequest>(`/requests/${id}/status`, { status });
  return data;
}

export async function remove(id: number): Promise<void> {
  await httpClient.delete(`/requests/${id}`);
}
