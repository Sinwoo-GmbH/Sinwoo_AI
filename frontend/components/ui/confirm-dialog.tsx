"use client";

import { useCallback, useEffect, useRef, useState } from "react";

type ConfirmDialogProps = {
  open: boolean;
  title: string;
  message: string;
  confirmLabel?: string;
  cancelLabel?: string;
  variant?: "danger" | "default";
  onConfirm: () => void;
  onCancel: () => void;
};

export function ConfirmDialog({
  open,
  title,
  message,
  confirmLabel = "OK",
  cancelLabel = "Cancel",
  variant = "default",
  onConfirm,
  onCancel,
}: ConfirmDialogProps) {
  const confirmRef = useRef<HTMLButtonElement>(null);
  const [visible, setVisible] = useState(false);
  const [animate, setAnimate] = useState(false);

  useEffect(() => {
    if (open) {
      setVisible(true);
      requestAnimationFrame(() => requestAnimationFrame(() => setAnimate(true)));
      confirmRef.current?.focus();
    } else {
      setAnimate(false);
      const t = setTimeout(() => setVisible(false), 200);
      return () => clearTimeout(t);
    }
  }, [open]);

  const handleKeyDown = useCallback(
    (e: React.KeyboardEvent) => {
      if (e.key === "Escape") onCancel();
    },
    [onCancel],
  );

  if (!visible) return null;

  const isDanger = variant === "danger";

  return (
    <div
      className={`fixed inset-0 z-[9999] flex items-center justify-center transition-all duration-200 ${
        animate ? "bg-black/30 backdrop-blur-[1px]" : "bg-transparent"
      }`}
      onClick={onCancel}
      onKeyDown={handleKeyDown}
    >
      <div
        className={`w-[380px] rounded-lg border border-slate-200/80 bg-white shadow-[0_20px_60px_rgba(0,0,0,0.12),0_1px_3px_rgba(0,0,0,0.06)] transition-all duration-200 ${
          animate
            ? "translate-y-0 scale-100 opacity-100"
            : "translate-y-2 scale-[0.97] opacity-0"
        }`}
        onClick={(e) => e.stopPropagation()}
      >
        {/* Header */}
        <div className="border-b border-slate-100 px-5 py-4">
          <h3 className="text-[14px] font-semibold tracking-[-0.01em] text-slate-900">
            {title}
          </h3>
        </div>

        {/* Body */}
        <div className="px-5 py-4">
          <p className="text-[13px] leading-[1.6] text-slate-600">{message}</p>
        </div>

        {/* Footer */}
        <div className="flex items-center justify-end gap-2 border-t border-slate-100 px-5 py-3">
          <button
            type="button"
            onClick={onCancel}
            className="h-[34px] rounded-md border border-slate-200 bg-white px-4 text-[12.5px] font-medium text-slate-600 transition-colors hover:bg-slate-50 active:bg-slate-100"
          >
            {cancelLabel}
          </button>
          <button
            ref={confirmRef}
            type="button"
            onClick={onConfirm}
            className={`h-[34px] rounded-md px-4 text-[12.5px] font-medium text-white transition-colors ${
              isDanger
                ? "bg-[#C53030] hover:bg-[#9B2C2C] active:bg-[#822727]"
                : "bg-[#23468F] hover:bg-[#1D3975] active:bg-[#172D5E]"
            }`}
          >
            {confirmLabel}
          </button>
        </div>
      </div>
    </div>
  );
}
