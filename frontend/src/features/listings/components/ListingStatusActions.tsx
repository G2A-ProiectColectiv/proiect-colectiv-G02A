"use client";

import { useState } from "react";
import { getApiErrorMessage } from "@/shared/api/apiError";
import { Button } from "@/shared/components/Button";
import { ConfirmDialog } from "@/shared/components/ConfirmDialog";
import { Icon } from "@/shared/components/Icon";
import { LinkButton } from "@/shared/components/LinkButton";
import { toast } from "@/shared/components/Toaster";
import { useChangeListingStatus } from "../hooks/useChangeListingStatus";
import { useDeleteListing } from "../hooks/useDeleteListing";
import type { ListingStatusChoice, ListingSummary } from "../types";
import styles from "./ListingStatusActions.module.css";

type PendingAction = { kind: "status"; status: ListingStatusChoice } | { kind: "delete" } | null;

interface StatusAction {
  status: ListingStatusChoice;
  buttonLabel: string;
  title: string;
  message: (title: string) => string;
  confirmLabel: string;
  success: string;
}

const MARK_RENTED: StatusAction = {
  status: "RENTED",
  buttonLabel: "Marchează ca închiriat",
  title: "Marchezi anunțul ca închiriat?",
  message: (title) => `Anunțul „${title}” nu va mai apărea în căutări. Îl poți reactiva oricând.`,
  confirmLabel: "Da, marchează",
  success: "Anunțul a fost marcat ca închiriat.",
};

const MARK_UNAVAILABLE: StatusAction = {
  status: "UNAVAILABLE",
  buttonLabel: "Marchează indisponibil",
  title: "Marchezi anunțul ca indisponibil?",
  message: (title) => `Anunțul „${title}” nu va mai apărea în căutări. Îl poți reactiva oricând.`,
  confirmLabel: "Da, marchează",
  success: "Anunțul a fost marcat ca indisponibil.",
};

const REACTIVATE: StatusAction = {
  status: "ACTIVE",
  buttonLabel: "Reactivează",
  title: "Reactivezi anunțul?",
  message: (title) => `Anunțul „${title}” va apărea din nou în căutări.`,
  confirmLabel: "Da, reactivează",
  success: "Anunțul a fost reactivat.",
};

/** Tranzițiile permise de backend (GEST-03): ACTIVE ↔ RENTED și ACTIVE ↔ UNAVAILABLE. */
function actionsFor(status: ListingSummary["status"]): StatusAction[] {
  if (status === "ACTIVE") return [MARK_RENTED, MARK_UNAVAILABLE];
  if (status === "RENTED" || status === "UNAVAILABLE") return [REACTIVATE];
  return [];
}

export interface ListingStatusActionsProps {
  listing: ListingSummary;
}

/** Editează, schimbă status și șterge (GEST-05); schimbarea statusului și ștergerea cer confirmare (GEST-04). */
export function ListingStatusActions({ listing }: ListingStatusActionsProps) {
  const [pending, setPending] = useState<PendingAction>(null);
  const changeStatus = useChangeListingStatus();
  const deleteListing = useDeleteListing();

  const statusActions = actionsFor(listing.status);
  const activeStatusAction =
    pending?.kind === "status"
      ? statusActions.find((action) => action.status === pending.status)
      : undefined;
  const isLoading = changeStatus.isPending || deleteListing.isPending;

  function close() {
    setPending(null);
  }

  function confirmStatus(action: StatusAction) {
    changeStatus.mutate(
      { id: listing.id, status: action.status },
      {
        onSuccess: () => {
          toast.success(action.success);
          close();
        },
        onError: (error) => {
          toast.error(getApiErrorMessage(error));
          close();
        },
      },
    );
  }

  function confirmDelete() {
    deleteListing.mutate(listing.id, {
      onSuccess: () => {
        toast.success("Anunțul a fost șters.");
        close();
      },
      onError: (error) => {
        toast.error(getApiErrorMessage(error));
        close();
      },
    });
  }

  return (
    <div className={styles.actions}>
      <LinkButton href={`/my-listings/${listing.id}/edit`} variant="text" className={styles.edit}>
        <Icon name="edit" size={18} />
        Editează
      </LinkButton>

      {statusActions.map((action) => (
        <Button
          key={action.status}
          variant="secondary"
          size="sm"
          onClick={() => setPending({ kind: "status", status: action.status })}
        >
          {action.buttonLabel}
        </Button>
      ))}

      <Button
        variant="dangerOutline"
        size="sm"
        icon={<Icon name="trash" size={18} />}
        onClick={() => setPending({ kind: "delete" })}
      >
        Șterge anunțul
      </Button>

      <ConfirmDialog
        open={!!activeStatusAction}
        title={activeStatusAction?.title ?? ""}
        message={activeStatusAction?.message(listing.title) ?? ""}
        confirmLabel={activeStatusAction?.confirmLabel ?? ""}
        loading={isLoading}
        onConfirm={() => activeStatusAction && confirmStatus(activeStatusAction)}
        onCancel={close}
      />

      <ConfirmDialog
        open={pending?.kind === "delete"}
        title="Ștergi anunțul?"
        message={`Anunțul „${listing.title}” va fi șters definitiv, împreună cu pozele lui.`}
        confirmLabel="Șterge anunțul"
        tone="danger"
        loading={isLoading}
        onConfirm={confirmDelete}
        onCancel={close}
      />
    </div>
  );
}
