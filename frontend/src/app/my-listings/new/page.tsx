import type { Metadata } from "next";
import { CreateListingView } from "@/features/listings/views/CreateListingView";

export const metadata: Metadata = { title: "Anunț nou" };

export default function NewListingPage() {
  return <CreateListingView />;
}
