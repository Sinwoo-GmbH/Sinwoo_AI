"use client";

import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";

type LeaveConfirmDialogProps = {
  open: boolean;
  title: string;
  description: string;
  confirmLabel?: string;
  cancelLabel?: string;
  onConfirm: () => void;
  onClose: () => void;
};

export function LeaveConfirmDialog({
  open,
  title,
  description,
  confirmLabel = "Confirm",
  cancelLabel = "Close",
  onConfirm,
  onClose,
}: LeaveConfirmDialogProps) {
  if (!open) {
    return null;
  }

  return (
    <div className="fixed inset-0 z-[80] flex items-center justify-center bg-slate-950/30 p-4">
      <div className="absolute inset-0" aria-hidden="true" onClick={onClose} />

      <Card className="relative z-[81] w-full max-w-[360px] rounded-[4px] border-slate-300 bg-[#f7f8fa] shadow-[0_8px_18px_rgba(15,23,42,0.12)]">
        <CardHeader className="border-b border-slate-300 px-3 py-2">
          <CardTitle className="text-[11px] font-semibold leading-4 text-slate-900">
            {title}
          </CardTitle>
        </CardHeader>
        <CardContent className="px-3 py-2.5">
          <p className="text-[10px] leading-4 text-slate-600">{description}</p>
        </CardContent>
        <div className="flex items-center justify-end gap-1.5 border-t border-slate-300 px-3 py-2">
          <Button
            type="button"
            variant="outline"
            size="sm"
            onClick={onClose}
            className="h-6 rounded-[3px] border-slate-300 px-2 text-[9px] font-medium text-slate-700"
          >
            {cancelLabel}
          </Button>
          <Button
            type="button"
            size="sm"
            onClick={onConfirm}
            className="h-6 rounded-[3px] bg-[#8b5a1f] px-2 text-[9px] font-medium text-white hover:bg-[#734918]"
          >
            {confirmLabel}
          </Button>
        </div>
      </Card>
    </div>
  );
}
