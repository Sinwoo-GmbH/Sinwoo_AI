"use client";

import { ChevronDown } from "lucide-react";
import { useEffect, useLayoutEffect, useRef, useState } from "react";
import { createPortal } from "react-dom";

import {
  LOGIN_LOCALES,
  type LoginLocale,
} from "@/lib/i18n/login-cnt";
import { cn } from "@/lib/utils";

type Props = {
  idPrefix: string;
  value: LoginLocale;
  localeLabel: string;
  localeNames: Record<LoginLocale, string>;
  onChange: (locale: LoginLocale) => void;
  className?: string;
  buttonClassName?: string;
  mnuClassName?: string;
  align?: "start" | "center" | "end";
  mnuStrategy?: "absolute" | "fixed";
};

export function LocaleCombobox({
  idPrefix,
  value,
  localeLabel,
  localeNames,
  onChange,
  className,
  buttonClassName,
  mnuClassName,
  align = "end",
  mnuStrategy = "absolute",
}: Props) {
  const [open, setOpen] = useState(false);
  const rootRef = useRef<HTMLDivElement | null>(null);
  const buttonRef = useRef<HTMLButtonElement | null>(null);
  const mnuRef = useRef<HTMLDivElement | null>(null);
  const [fixedStyle, setFixedStyle] = useState<React.CSSProperties>({});
  const [positionReady, setPositionReady] = useState(false);

  useLayoutEffect(() => {
    if (!open || mnuStrategy !== "fixed") {
      setPositionReady(false);
      return;
    }

    function updateMnuPosition() {
      const button = buttonRef.current;
      const mnu = mnuRef.current;

      if (!button || !mnu) {
        return;
      }

      const rect = button.getBoundingClientRect();
      const viewportPadding = 12;
      const nextTop = Math.round(rect.bottom + 6);
      const minWidth = Math.max(Math.round(rect.width), 124);

      if (align === "end") {
        const nextRight = Math.max(
          viewportPadding,
          Math.round(window.innerWidth - rect.right)
        );

        setFixedStyle({
          top: `${nextTop}px`,
          left: "auto",
          right: `${nextRight}px`,
          minWidth: `${minWidth}px`,
          maxWidth: `calc(100vw - ${viewportPadding * 2}px)`,
        });
        setPositionReady(true);
        return;
      }

      const unclampedLeft =
        align === "center"
          ? rect.left + rect.width / 2 - minWidth / 2
          : rect.left;
      const nextLeft = Math.max(
        viewportPadding,
        Math.round(
          Math.min(unclampedLeft, window.innerWidth - minWidth - viewportPadding)
        )
      );

      setFixedStyle({
        top: `${nextTop}px`,
        left: `${nextLeft}px`,
        right: "auto",
        minWidth: `${minWidth}px`,
        maxWidth: `calc(100vw - ${viewportPadding * 2}px)`,
      });
      setPositionReady(true);
    }

    updateMnuPosition();
    window.addEventListener("resize", updateMnuPosition);
    window.addEventListener("scroll", updateMnuPosition, true);

    return () => {
      window.removeEventListener("resize", updateMnuPosition);
      window.removeEventListener("scroll", updateMnuPosition, true);
      setPositionReady(false);
    };
  }, [align, mnuStrategy, open]);

  useEffect(() => {
    function handlePointerDown(event: MouseEvent) {
      const target = event.target as Node;
      if (!rootRef.current?.contains(target) && !mnuRef.current?.contains(target)) {
        setOpen(false);
      }
    }

    document.addEventListener("mousedown", handlePointerDown);
    return () => document.removeEventListener("mousedown", handlePointerDown);
  }, []);

  const mnuCnt = open ? (
    <div
      id={`${idPrefix}-locale-mnu`}
      ref={mnuRef}
      className={cn(
        mnuStrategy === "fixed"
          ? "fixed z-[9999] rounded-[4px] border border-slate-300 bg-white p-1 shadow-[0_6px_14px_rgba(15,23,42,0.10)]"
          : cn(
              "absolute z-30 mt-1 min-w-[180px] rounded-[4px] border border-slate-300 bg-white p-1 shadow-[0_6px_14px_rgba(15,23,42,0.08)]",
              align === "start"
                ? "left-0"
                : align === "center"
                  ? "left-1/2 -translate-x-1/2"
                  : "right-0"
            ),
        mnuClassName
      )}
      style={
        mnuStrategy === "fixed"
          ? {
              ...fixedStyle,
              visibility: positionReady ? "visible" : "hidden",
            }
          : undefined
      }
    >
      <div
        id={`${idPrefix}-locale-mnu-label`}
        className="px-2 pb-0.5 pt-0.5 text-[8px] font-semibold uppercase tracking-[0.12em] text-slate-400"
      >
        {localeLabel}
      </div>
      {LOGIN_LOCALES.map((item) => (
        <button
          id={`${idPrefix}-locale-option-${item}`}
          key={item}
          type="button"
          onClick={() => {
            onChange(item);
            setOpen(false);
          }}
          className={cn(
            "flex w-full items-center rounded-[3px] px-2 py-1 text-left text-[11px] transition",
            item === value
              ? "bg-[#31588f] text-white"
              : "text-slate-700 hover:bg-slate-100"
          )}
        >
          <span>{localeNames[item]}</span>
        </button>
      ))}
    </div>
  ) : null;

  return (
    <div
      id={`${idPrefix}-locale-switcher`}
      ref={rootRef}
      className={cn("relative isolate shrink-0", className)}
    >
      <button
        id={`${idPrefix}-locale-button`}
        ref={buttonRef}
        type="button"
        onClick={() => setOpen((current) => !current)}
        className={cn(
          "inline-flex items-center gap-1.5 rounded-[4px] border border-slate-300 bg-white px-2.5 py-1 text-[11px] font-medium text-slate-700 transition hover:border-slate-400 hover:bg-slate-50",
          buttonClassName
        )}
      >
        <span id={`${idPrefix}-locale-name`} className="whitespace-nowrap">
          {localeNames[value]}
        </span>
        <ChevronDown
          className={cn("h-3.5 w-3.5 text-slate-400 transition-transform", open ? "rotate-180" : "")}
        />
      </button>
      {mnuCnt
        ? mnuStrategy === "fixed"
          ? createPortal(mnuCnt, document.body)
          : mnuCnt
        : null}
    </div>
  );
}
