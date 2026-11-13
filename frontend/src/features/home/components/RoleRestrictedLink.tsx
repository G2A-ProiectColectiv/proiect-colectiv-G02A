"use client";

import { useState, type MouseEvent } from "react";
import { useAuth } from "@/features/auth/context/AuthContext";
import { ConfirmDialog } from "@/shared/components/ConfirmDialog";
import { LinkButton, type LinkButtonProps } from "@/shared/components/LinkButton";

interface RoleRestrictedLinkProps extends LinkButtonProps {
  /** Rolul căruia nu îi este permisă acțiunea; pentru el se deschide avertizarea în loc de navigare. */
  blockedRole: "TENANT" | "LANDLORD";
  warningTitle: string;
  warningMessage: string;
}

/** Link-buton care, pentru rolul nepermis, deschide un modal de avertizare în loc să navigheze. */
export function RoleRestrictedLink({
  blockedRole,
  warningTitle,
  warningMessage,
  onClick,
  ...linkProps
}: RoleRestrictedLinkProps) {
  const { user } = useAuth();
  const [warningOpen, setWarningOpen] = useState(false);

  function handleClick(event: MouseEvent<HTMLAnchorElement>) {
    onClick?.(event);
    if (user?.role === blockedRole) {
      event.preventDefault();
      setWarningOpen(true);
    }
  }

  return (
    <>
      <LinkButton {...linkProps} onClick={handleClick} />
      <ConfirmDialog
        open={warningOpen}
        title={warningTitle}
        message={warningMessage}
        confirmLabel="Am înțeles"
        hideCancel
        onConfirm={() => setWarningOpen(false)}
        onCancel={() => setWarningOpen(false)}
      />
    </>
  );
}
