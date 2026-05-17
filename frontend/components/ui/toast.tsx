"use client";

import { useCallback, useEffect, useState } from "react";
import { AlertCircle, CheckCircle2, Info, X } from "lucide-react";
import { cn } from "@/lib/utils";

/* ── types ─────────────────────────────────────────────── */

type ToastType = "success" | "error" | "info";

type ToastItem = {
  id: number;
  type: ToastType;
  message: string;
  exiting?: boolean;
};

type ToastStore = {
  items: ToastItem[];
  listeners: Set<() => void>;
  nextId: number;
};

/* ── global store (singleton, no external deps) ────────── */

const store: ToastStore = { items: [], listeners: new Set(), nextId: 1 };

function notify() {
  for (const fn of store.listeners) fn();
}

function addToast(type: ToastType, message: string, duration = 3500) {
  const id = store.nextId++;
  store.items = [...store.items, { id, type, message }];
  notify();

  setTimeout(() => {
    // 퇴장 애니메이션 시작
    store.items = store.items.map((t) => (t.id === id ? { ...t, exiting: true } : t));
    notify();
    // 애니메이션 후 제거
    setTimeout(() => {
      store.items = store.items.filter((t) => t.id !== id);
      notify();
    }, 300);
  }, duration);
}

function removeToast(id: number) {
  store.items = store.items.map((t) => (t.id === id ? { ...t, exiting: true } : t));
  notify();
  setTimeout(() => {
    store.items = store.items.filter((t) => t.id !== id);
    notify();
  }, 300);
}

/* ── public API ────────────────────────────────────────── */

export const toast = {
  success: (msg: string) => addToast("success", msg),
  error: (msg: string) => addToast("error", msg, 5000),
  info: (msg: string) => addToast("info", msg),
};

/* ── icon / style map ──────────────────────────────────── */

const ICON_MAP: Record<ToastType, typeof AlertCircle> = {
  success: CheckCircle2,
  error: AlertCircle,
  info: Info,
};

const STYLE_MAP: Record<ToastType, string> = {
  success: "border-emerald-200 bg-emerald-50/95 text-emerald-800",
  error: "border-rose-200 bg-rose-50/95 text-rose-800",
  info: "border-[#BCD0F5] bg-blue-50/95 text-[#18397E]",
};

const ICON_STYLE: Record<ToastType, string> = {
  success: "text-emerald-500",
  error: "text-rose-500",
  info: "text-[#4F72C8]",
};

/* ── container component (mount once in layout) ────────── */

export function ToastContainer() {
  const [items, setItems] = useState<ToastItem[]>([]);

  useEffect(() => {
    const fn = () => setItems([...store.items]);
    store.listeners.add(fn);
    return () => { store.listeners.delete(fn); };
  }, []);

  if (items.length === 0) return null;

  return (
    <div className="fixed left-1/2 top-1/4 z-[9999] flex -translate-x-1/2 flex-col items-center gap-2">
      {items.map((t) => {
        const Icon = ICON_MAP[t.type];
        return (
          <div
            key={t.id}
            className={cn(
              "flex items-start gap-2 rounded-lg border px-3 py-2.5 shadow-lg backdrop-blur-sm",
              "min-w-[280px] max-w-[380px]",
              "transition-all duration-300 ease-out",
              t.exiting
                ? "-translate-y-4 opacity-0"
                : "translate-y-0 opacity-100",
              STYLE_MAP[t.type],
            )}
          >
            <Icon className={cn("mt-0.5 h-4 w-4 shrink-0", ICON_STYLE[t.type])} />
            <span className="flex-1 text-[12px] font-medium leading-5">{t.message}</span>
            <button
              type="button"
              onClick={() => removeToast(t.id)}
              className="mt-0.5 shrink-0 rounded p-0.5 opacity-50 hover:opacity-100 transition-opacity"
            >
              <X className="h-3.5 w-3.5" />
            </button>
          </div>
        );
      })}
    </div>
  );
}
