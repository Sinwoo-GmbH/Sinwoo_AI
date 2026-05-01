"use client";

import { useEffect, useMemo, useState } from "react";

import type {
  LeaveOrganizationNode,
  LeaveParticipant,
} from "@/components/features/attendance/request-leave/leave-mock-data";
import { SelectedUserChip } from "@/components/features/attendance/request-leave/selected-user-chip";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { cn } from "@/lib/utils";
import { Check, Search, X } from "lucide-react";

type EmployeePickerModalProps = {
  open: boolean;
  mode: "approver" | "cc";
  title?: string;
  organizations: LeaveOrganizationNode[];
  employees: LeaveParticipant[];
  selectedUsers: LeaveParticipant[];
  onClose: () => void;
  onApply: (users: LeaveParticipant[]) => void;
};

type OrganizationTreeNodeProps = {
  depth?: number;
  node: LeaveOrganizationNode;
  selectedOrgId: string;
  onSelect: (orgId: string) => void;
};

const inputClassName =
  "h-7 w-full rounded-[3px] border border-slate-300 bg-white px-2 text-[10px] leading-4 text-slate-700 outline-none transition focus:border-[#7E9BD8] focus:ring-1 focus:ring-[#BCD0F5]";

function collectOrganizationIds(node: LeaveOrganizationNode): string[] {
  return [
    node.id,
    ...(node.children?.flatMap((child) => collectOrganizationIds(child)) ?? []),
  ];
}

function resolveSelectedOrganizationIds(
  organizations: LeaveOrganizationNode[],
  selectedOrgId: string
) {
  if (!selectedOrgId) {
    return null;
  }

  const walk = (nodes: LeaveOrganizationNode[]): string[] | null => {
    for (const node of nodes) {
      if (node.id === selectedOrgId) {
        return collectOrganizationIds(node);
      }

      if (node.children?.length) {
        const hit = walk(node.children);
        if (hit) {
          return hit;
        }
      }
    }

    return null;
  };

  return walk(organizations);
}

function OrganizationTreeNode({
  depth = 0,
  node,
  selectedOrgId,
  onSelect,
}: OrganizationTreeNodeProps) {
  const isSelected = selectedOrgId === node.id;

  return (
    <div className="space-y-1">
      <button
        type="button"
        onClick={() => onSelect(node.id)}
        className={cn(
          "flex w-full items-center rounded-[3px] px-2 py-1 text-left text-[10px] leading-4 transition-colors",
          isSelected
            ? "bg-[#31588f] text-white"
            : "text-slate-600 hover:bg-slate-100 hover:text-slate-900"
        )}
        style={{ paddingLeft: `${depth * 12 + 8}px` }}
      >
        {node.label}
      </button>

      {node.children?.length ? (
        <div className="space-y-1">
          {node.children.map((child) => (
            <OrganizationTreeNode
              key={child.id}
              depth={depth + 1}
              node={child}
              selectedOrgId={selectedOrgId}
              onSelect={onSelect}
            />
          ))}
        </div>
      ) : null}
    </div>
  );
}

export function EmployeePickerModal({
  open,
  mode,
  title,
  organizations,
  employees,
  selectedUsers,
  onClose,
  onApply,
}: EmployeePickerModalProps) {
  const [search, setSearch] = useState("");
  const [selectedOrgId, setSelectedOrgId] = useState("");
  const [localSelectedUsers, setLocalSelectedUsers] =
    useState<LeaveParticipant[]>(selectedUsers);

  useEffect(() => {
    if (!open) {
      return;
    }

    setSearch("");
    setSelectedOrgId("");
    setLocalSelectedUsers(selectedUsers);
  }, [open, selectedUsers]);

  useEffect(() => {
    if (!open) {
      return;
    }

    const handleEscape = (event: KeyboardEvent) => {
      if (event.key === "Escape") {
        onClose();
      }
    };

    window.addEventListener("keydown", handleEscape);
    return () => window.removeEventListener("keydown", handleEscape);
  }, [onClose, open]);

  const selectedOrgIds = useMemo(
    () => resolveSelectedOrganizationIds(organizations, selectedOrgId),
    [organizations, selectedOrgId]
  );

  const visibleEmployees = useMemo(() => {
    const normalizedSearch = search.trim().toLowerCase();
    return employees.filter((employee) => {
      const matchesOrganization =
        !selectedOrgIds || selectedOrgIds.includes(employee.orgId);
      const matchesSearch =
        !normalizedSearch ||
        employee.name.toLowerCase().includes(normalizedSearch);
      return matchesOrganization && matchesSearch;
    });
  }, [employees, search, selectedOrgIds]);

  const isUserSelected = (userId: string) =>
    localSelectedUsers.some((user) => user.id === userId);

  const toggleUser = (user: LeaveParticipant) => {
    setLocalSelectedUsers((current) =>
      current.some((entry) => entry.id === user.id)
        ? current.filter((entry) => entry.id !== user.id)
        : [...current, user]
    );
  };

  if (!open) {
    return null;
  }

  return (
    <div className="fixed inset-0 z-[90] flex items-center justify-center bg-slate-950/35 p-4">
      <div className="absolute inset-0" aria-hidden="true" onClick={onClose} />

      <Card className="relative z-[91] flex h-[min(74vh,680px)] w-full max-w-[1120px] flex-col overflow-hidden rounded-[4px] border-slate-300 bg-[#f7f8fa] shadow-[0_8px_18px_rgba(15,23,42,0.12)]">
        <CardHeader className="flex flex-row items-center justify-between border-b border-slate-300 px-3 py-2">
          <CardTitle className="text-[12px] font-semibold leading-4 text-slate-900">
            {title ?? (mode === "approver" ? "Select Approvers" : "Select C.C.")}
          </CardTitle>
          <Button
            type="button"
            variant="ghost"
            size="sm"
            onClick={onClose}
            className="h-6 w-6 rounded-[3px] p-0 text-slate-500 hover:bg-slate-200"
          >
            <X className="h-3 w-3" />
          </Button>
        </CardHeader>

        <CardContent className="min-h-0 flex-1 px-3 py-3">
          <div className="grid h-full min-h-0 gap-2 xl:grid-cols-[0.82fr_1.08fr_0.9fr]">
            <div className="flex min-h-0 flex-col gap-1.5">
              <div className="relative">
                <Search className="pointer-events-none absolute left-2 top-1/2 h-3 w-3 -translate-y-1/2 text-slate-400" />
                <input
                  type="search"
                  value={search}
                  onChange={(event) => setSearch(event.target.value)}
                  placeholder="Search employees"
                  className={cn(inputClassName, "pl-7")}
                />
              </div>

              <div className="min-h-0 flex-1 overflow-y-auto rounded-[3px] border border-slate-300 bg-white p-1.5">
                <button
                  type="button"
                  onClick={() => setSelectedOrgId("")}
                  className={cn(
                    "mb-1 flex w-full items-center rounded-[3px] px-2 py-1 text-left text-[10px] leading-4 transition-colors",
                    !selectedOrgId
                      ? "bg-[#31588f] text-white"
                      : "text-slate-600 hover:bg-slate-100 hover:text-slate-900"
                  )}
                >
                  All Organizations
                </button>

                <div className="space-y-1">
                  {organizations.map((node) => (
                    <OrganizationTreeNode
                      key={node.id}
                      node={node}
                      selectedOrgId={selectedOrgId}
                      onSelect={setSelectedOrgId}
                    />
                  ))}
                </div>
              </div>
            </div>

            <div className="flex min-h-0 flex-col">
              <div className="mb-1 flex items-center justify-between">
                <h4 className="text-[10px] font-semibold leading-4 text-slate-700">Employee List</h4>
                <p className="text-[9px] text-slate-500">{visibleEmployees.length} results</p>
              </div>

              <div className="min-h-0 flex-1 overflow-y-auto rounded-[3px] border border-slate-300 bg-white">
                <div className="divide-y divide-slate-200">
                  {visibleEmployees.length ? (
                    visibleEmployees.map((employee) => {
                      const selected = isUserSelected(employee.id);
                      return (
                        <label
                          key={employee.id}
                          className={cn(
                            "flex cursor-pointer items-start gap-2 px-2.5 py-1.5 transition-colors hover:bg-slate-50",
                            selected ? "bg-[#eef3fb]" : ""
                          )}
                        >
                          <input
                            type="checkbox"
                            checked={selected}
                            onChange={() => toggleUser(employee)}
                            className="mt-0.5 h-3 w-3 rounded-[2px] border-slate-300 text-[#31588f] focus:ring-[#BCD0F5]"
                          />
                          <div className="min-w-0">
                            <div className="flex items-center gap-1">
                              <p className="text-[10px] font-semibold leading-4 text-slate-800">
                                {employee.name}
                              </p>
                              {selected ? (
                                <span className="inline-flex h-[14px] w-[14px] items-center justify-center rounded-[2px] bg-[#31588f] text-white">
                                  <Check className="h-2 w-2" />
                                </span>
                              ) : null}
                            </div>
                            <p className="text-[9px] leading-3 text-slate-500">
                              {employee.department} / {employee.position}
                            </p>
                          </div>
                        </label>
                      );
                    })
                  ) : (
                    <div className="px-4 py-8 text-center text-[10px] text-slate-500">
                      No employees match the current filters.
                    </div>
                  )}
                </div>
              </div>
            </div>

            <div className="flex min-h-0 flex-col">
              <div className="mb-1 flex items-center justify-between">
                <h4 className="text-[10px] font-semibold leading-4 text-slate-700">Selected</h4>
                <p className="text-[9px] text-slate-500">{localSelectedUsers.length}</p>
              </div>
              <div className="min-h-0 flex-1 overflow-y-auto rounded-[3px] border border-slate-300 bg-white p-1.5">
                <div className="space-y-1">
                  {localSelectedUsers.length ? (
                    localSelectedUsers.map((user) => (
                      <SelectedUserChip
                        key={user.id}
                        user={user}
                        tone={mode === "approver" ? "approver" : "cc"}
                        onRemove={() => toggleUser(user)}
                      />
                    ))
                  ) : (
                    <div className="rounded-[3px] border border-dashed border-slate-300 bg-[#fafafa] px-2 py-2 text-[10px] text-slate-500">
                      No users selected.
                    </div>
                  )}
                </div>
              </div>
            </div>
          </div>
        </CardContent>

        <div className="flex items-center justify-end gap-2 border-t border-slate-300 px-3 py-2">
          <Button
            type="button"
            variant="outline"
            size="sm"
            onClick={onClose}
            className="h-7 rounded-[3px] border-slate-300 px-2.5 text-[10px] font-medium"
          >
            Cancel
          </Button>
          <Button
            type="button"
            size="sm"
            onClick={() => onApply(localSelectedUsers)}
            className="h-7 rounded-[3px] bg-[#2f5b96] px-2.5 text-[10px] font-medium text-white hover:bg-[#274d7e]"
          >
            Apply
          </Button>
        </div>
      </Card>
    </div>
  );
}
