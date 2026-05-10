"use client";

import type { ReactNode } from "react";

import type {
  LeaveFiltStatus,
  LeaveFiltValue,
} from "@/features/requests/leave/leave-mock-data";
import { Button } from "@/components/ui/button";
import { Search } from "lucide-react";

type LeaveFiltBarProps = {
  value: LeaveFiltValue;
  statusOpts: readonly LeaveFiltStatus[];
  onChange: (next: LeaveFiltValue) => void;
  onSearch: () => void;
  onCreate: () => void;
};

const fieldClassName =
  "h-6 w-full rounded-[3px] border border-slate-300 bg-white px-2 text-[10px] leading-4 text-slate-700 outline-none transition focus:border-[#7E9BD8] focus:ring-1 focus:ring-[#BCD0F5]";

function FilterField({
  label,
  children,
}: {
  label: string;
  children: ReactNode;
}) {
  return (
    <label className="space-y-1">
      <span className="text-[8px] leading-3 text-slate-500">{label}</span>
      {children}
    </label>
  );
}

export function LeaveFiltBar({
  value,
  statusOpts,
  onChange,
  onSearch,
  onCreate,
}: LeaveFiltBarProps) {
  return (
    <div className="flex flex-col gap-1.5 xl:flex-row xl:items-end xl:justify-between">
      <Button
        type="button"
        size="sm"
        onClick={onCreate}
        className="h-6 shrink-0 rounded-[3px] bg-[#2f5b96] px-2 text-[9px] font-medium text-white hover:bg-[#274d7e]"
      >
        Create
      </Button>

      <div className="flex flex-col gap-1.5 xl:ml-auto xl:flex-row xl:items-end xl:justify-end">
        <div className="grid gap-2.5 sm:grid-cols-2 xl:grid-cols-[160px_160px_140px]">
          <FilterField label="Start Date From">
            <input
              type="date"
              value={value.startDateFrom}
              onChange={(event) =>
                onChange({ ...value, startDateFrom: event.target.value })
              }
              className={fieldClassName}
            />
          </FilterField>
          <FilterField label="Start Date To">
            <input
              type="date"
              value={value.startDateTo}
              onChange={(event) =>
                onChange({ ...value, startDateTo: event.target.value })
              }
              className={fieldClassName}
            />
          </FilterField>
          <FilterField label="Status">
            <select
              value={value.status}
              onChange={(event) =>
                onChange({
                  ...value,
                  status: event.target.value as LeaveFiltStatus,
                })
              }
              className={fieldClassName}
            >
              {statusOpts.map((status) => (
                <option key={status} value={status}>
                  {status}
                </option>
              ))}
            </select>
          </FilterField>
        </div>

        <Button
          type="button"
          variant="outline"
          size="sm"
          onClick={onSearch}
          className="h-6 shrink-0 rounded-[3px] border-slate-300 bg-white px-2 text-[9px] font-medium text-slate-700 hover:bg-slate-50"
        >
          <Search className="mr-1 h-2.5 w-2.5" />
          Search
        </Button>
      </div>
    </div>
  );
}
