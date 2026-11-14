import { httpClient } from "@/shared/api/httpClient";
import type { PageResponse } from "@/shared/types/api";
import type {
  RentalRequestDetails,
  RentalRequestSearchParams,
  RentalRequestSummary,
} from "../types";

/** Căutarea cererilor de către proprietari (doar LANDLORD). */
export async function searchRequests(
  params: RentalRequestSearchParams,
): Promise<PageResponse<RentalRequestSummary>> {
  const { data } = await httpClient.get<PageResponse<RentalRequestSummary>>("/requests", {
    params,
  });
  return data;
}

/** Detaliile unei cereri active, cu chiriașul și zona (doar LANDLORD). */
export async function getRequestDetails(id: number): Promise<RentalRequestDetails> {
  const { data } = await httpClient.get<RentalRequestDetails>(`/requests/${id}`);
  return data;
}
