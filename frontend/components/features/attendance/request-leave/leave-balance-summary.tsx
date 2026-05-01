"use client";

type LeaveBalanceSummaryProps = {
  availableDays: number;
  afterRequestDays: number;
};

function SummaryPanel({
  label,
  value,
}: {
  label: string;
  value: number;
}) {
  return (
    <div className="rounded-[3px] border border-slate-300 bg-[#f8f9fb] px-2 py-1">
      <p className="text-[8px] leading-3 text-slate-500">{label}</p>
      <p className="mt-0.5 text-[11px] font-semibold leading-4 text-slate-900">
        {value.toFixed(1)}
      </p>
    </div>
  );
}

export function LeaveBalanceSummary({
  availableDays,
  afterRequestDays,
}: LeaveBalanceSummaryProps) {
  return (
    <div className="grid w-full max-w-[190px] gap-1 sm:grid-cols-2">
      <SummaryPanel label="Available" value={availableDays} />
      <SummaryPanel label="After Request" value={afterRequestDays} />
    </div>
  );
}
