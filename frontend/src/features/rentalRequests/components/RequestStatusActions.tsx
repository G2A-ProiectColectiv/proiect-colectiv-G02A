"use client";

import { useState } from "react";
import { getApiErrorMessage } from "@/shared/api/apiError";
import { Button } from "@/shared/components/Button";
import { ConfirmDialog } from "@/shared/components/ConfirmDialog";
import { Icon } from "@/shared/components/Icon";
import { LinkButton } from "@/shared/components/LinkButton";
import { toast } from "@/shared/components/Toaster";
import { useChangeRequestStatus } from "../hooks/useChangeRequestStatus";
import { useDeleteRequest } from "../hooks/useDeleteRequest";
import type { RentalRequestSummary, RequestStatus, RequestStatusChoice } from "../types";
import styles from "./RequestStatusActions.module.css";

type PendingAction = { kind: "status"; status: RequestStatusChoice } | { kind: "delete" } | null;

interface StatusAction {
  status: RequestStatusChoice;
  buttonLabel: string;
  title: string;
  message: (title: string) => string;
  confirmLabel: string;
  success: string;
}

const MARK_FOUND: StatusAction = {
  status: "FOUND",
  buttonLabel: "Am găsit o locuință",
  title: "Marchezi cererea ca găsită?",
  message: (title) =>
    `Cererea „${title}” nu va mai fi vizibilă pentru proprietari. O poți reactiva oricând.`,
  confirmLabel: "Da, marchează",
  success: "Cererea a fost marcată ca găsită.",
};

const CLOSE: StatusAction = {
  status: "CLOSED",
  buttonLabel: "Închide cererea",
  title: "Închizi cererea?",
  message: (title) =>
    `Cererea „${title}” nu va mai fi vizibilă pentru proprietari. O poți reactiva oricând.`,
  confirmLabel: "Da, închide",
  success: "Cererea a fost închisă.",
};

const REACTIVATE: StatusAction = {
  status: "ACTIVE",
  buttonLabel: "Reactivează",
  title: "Reactivezi cererea?",
  message: (title) => `Cererea „${title}” va fi din nou vizibilă pentru proprietari.`,
  confirmLabel: "Da, reactivează",
  success: "Cererea a fost reactivată.",
};

/** Tranzițiile permise de backend (CER-06): ACTIVE ↔ FOUND și ACTIVE ↔ CLOSED. */
function actionsFor(status: RequestStatus): StatusAction[] {
  if (status === "ACTIVE") return [MARK_FOUND, CLOSE];
  if (status === "FOUND" || status === "CLOSED") return [REACTIVATE];
  return [];
}

export interface RequestStatusActionsProps {
  request: RentalRequestSummary;
}

/** Editează, schimbă statusul și șterge; schimbarea statusului și ștergerea cer confirmare (CER-07, NF-17). */
export function RequestStatusActions({ request }: RequestStatusActionsProps) {
  const [pending, setPending] = useState<PendingAction>(null);
  const changeStatus = useChangeRequestStatus();
  const deleteRequest = useDeleteRequest();

  const statusActions = actionsFor(request.status);
  const activeStatusAction =
    pending?.kind === "status"
      ? statusActions.find((action) => action.status === pending.status)
      : undefined;
  const isLoading = changeStatus.isPending || deleteRequest.isPending;

  function close() {
    setPending(null);
  }

  function confirmStatus(action: StatusAction) {
    changeStatus.mutate(
      { id: request.id, status: action.status },
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
    deleteRequest.mutate(request.id, {
      onSuccess: () => {
        toast.success("Cererea a fost ștearsă.");
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
      <LinkButton href={`/my-requests/${request.id}/edit`} variant="text" className={styles.edit}>
        <Icon name="edit" size={18} />
        Editează
      </LinkButton>

      {statusActions.map((action) => (
        <Button
          key={action.status}
          variant="secondary"
          size="sm"
          icon={action.status === "FOUND" ? <Icon name="check" size={18} /> : undefined}
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
        Șterge cererea
      </Button>

      <ConfirmDialog
        open={!!activeStatusAction}
        title={activeStatusAction?.title ?? ""}
        message={activeStatusAction?.message(request.title) ?? ""}
        confirmLabel={activeStatusAction?.confirmLabel ?? ""}
        loading={isLoading}
        onConfirm={() => activeStatusAction && confirmStatus(activeStatusAction)}
        onCancel={close}
      />

      <ConfirmDialog
        open={pending?.kind === "delete"}
        title="Ștergi cererea?"
        message={`Cererea „${request.title}” va fi ștearsă definitiv și nu va mai fi afișată.`}
        confirmLabel="Șterge cererea"
        tone="danger"
        loading={isLoading}
        onConfirm={confirmDelete}
        onCancel={close}
      />
    </div>
  );
}
