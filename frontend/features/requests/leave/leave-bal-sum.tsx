"use client";

import type { LoginLocale } from "@/lib/i18n/login-cnt";
import { getLeavePageMsgs } from "@/lib/i18n/leave-cnt";

type LeaveBalSumProps = {
  availableDays: number;
  afterRequestDays: number;
  /** 전년도 이월일수 (만료 차감 후). 값이 > 0이면 괄호 안에 표시. */
  previousYearDays?: number;
  locale: LoginLocale;
};

function SummaryPanel({
  label,
  value,
  carryover,
}: {
  label: string;
  value: number;
  carryover?: number;
}) {
  return (
    <div className="rounded-[3px] border border-slate-300 bg-[#f8f9fb] px-2 py-1">
      <p className="text-[8px] leading-3 text-slate-500">{label}</p>
      <p className="mt-0.5 text-[11px] font-semibold leading-4 text-slate-900">
        {value.toFixed(1)}
        {carryover && carryover > 0 ? (
          <span className="ml-0.5 text-[9px] font-normal text-slate-500">
            ({carryover.toFixed(1)})
          </span>
        ) : null}
      </p>
    </div>
  );
}

export function LeaveBalSum({
  availableDays,
  afterRequestDays,
  previousYearDays = 0,
  locale,
}: LeaveBalSumProps) {
  const L = getLeavePageMsgs(locale);

  return (
    <div className="grid w-full max-w-[190px] gap-1 sm:grid-cols-2">
      <SummaryPanel label={L.available} value={availableDays} carryover={previousYearDays} />
      <SummaryPanel label={L.afterRequest} value={afterRequestDays} carryover={previousYearDays} />
    </div>
  );
}
