"use client";

import { ChevronDown, ChevronLeft, ChevronRight, Workflow } from "lucide-react";

import type { WorkspaceShellContent } from "@/lib/i18n/workspace-shell-content";
import type { MenuNode } from "@/lib/workspace/platform-shell-data";
import { resolveWorkspaceIcon } from "@/lib/workspace/workspace-icon-map";
import { cn } from "@/lib/utils";

type WorkspaceSidebarProps = {
  activeTabId: string;
  expandedMenuIds: string[];
  label: string;
  menus: MenuNode[];
  onMenuSelect: (menu: MenuNode) => void;
  onToggleCollapsed: () => void;
  sidebarCollapsed: boolean;
  shellContent: WorkspaceShellContent;
};

type WorkspaceSidebarNodeProps = {
  activeTabId: string;
  depth?: number;
  expandedMenuIds: string[];
  menu: MenuNode;
  onMenuSelect: (menu: MenuNode) => void;
  sidebarCollapsed: boolean;
};

function WorkspaceSidebarNode({
  activeTabId,
  depth = 1,
  expandedMenuIds,
  menu,
  onMenuSelect,
  sidebarCollapsed,
}: WorkspaceSidebarNodeProps) {
  const isExpanded = expandedMenuIds.includes(menu.id);
  const hasChildren = Boolean(menu.children?.length);
  const Icon = menu.icon ? resolveWorkspaceIcon(menu.icon) : Workflow;
  const isActiveLeaf = !hasChildren && activeTabId === menu.id;

  return (
    <div id={`workspace-menu-node-${menu.id}`} className="space-y-1">
      <button
        id={`workspace-menu-button-${menu.id}`}
        type="button"
        onClick={() => onMenuSelect(menu)}
        className={cn(
          "flex w-full items-center gap-2 rounded-[3px] border border-transparent px-2 py-1 text-left leading-4 transition-colors",
          depth === 1 ? "font-semibold" : "font-medium",
          isActiveLeaf
            ? "border-[#31588f] bg-[#31588f] text-white"
            : "text-slate-600 hover:border-slate-300 hover:bg-slate-100 hover:text-slate-900",
          depth === 1 ? "text-[11px]" : "",
          depth === 2 ? "py-1 text-[10px] text-slate-500" : "",
          depth >= 3 ? "py-0.5 text-[10px] text-slate-400" : "",
          sidebarCollapsed && depth === 1 ? "justify-center px-1.5" : ""
        )}
      >
        {depth === 1 ? (
          <Icon className="h-3.5 w-3.5 shrink-0" />
        ) : (
          <span className="block h-1 w-1 bg-slate-300" />
        )}

        {!sidebarCollapsed ? (
          <span
            className={cn(
              depth === 1 ? "text-[13px]" : "",
              depth === 2 ? "text-[10px]" : "",
              depth >= 3 ? "text-[10px]" : ""
            )}
          >
            {menu.title}
          </span>
        ) : null}

        {!sidebarCollapsed && hasChildren ? (
          <ChevronDown
            className={cn(
              "ml-auto h-3.5 w-3.5 transition-transform",
              isExpanded ? "rotate-180" : ""
            )}
          />
        ) : null}
      </button>

      {!sidebarCollapsed && hasChildren && isExpanded ? (
        <div
          id={`workspace-menu-children-${menu.id}`}
          className={cn(
            "border-l border-slate-200 pl-2",
            depth === 1 ? "ml-2 space-y-0.5" : "ml-1.5 space-y-0.5"
          )}
        >
          {menu.children?.map((child) => (
            <WorkspaceSidebarNode
              key={child.id}
              activeTabId={activeTabId}
              depth={Math.min(depth + 1, 4)}
              expandedMenuIds={expandedMenuIds}
              menu={child}
              onMenuSelect={onMenuSelect}
              sidebarCollapsed={sidebarCollapsed}
            />
          ))}
        </div>
      ) : null}
    </div>
  );
}

export function WorkspaceSidebar({
  activeTabId,
  expandedMenuIds,
  label,
  menus,
  onMenuSelect,
  onToggleCollapsed,
  sidebarCollapsed,
  shellContent,
}: WorkspaceSidebarProps) {
  return (
    <aside
      id="workspace-sidebar"
      className={cn(
        "workspace-scrollbar self-stretch overflow-y-auto rounded-[4px] border border-slate-300 bg-[#f8f9fb] p-2 shadow-none transition-all",
        sidebarCollapsed ? "w-[78px]" : "w-[238px]"
      )}
    >
      <div id="workspace-sidebar-header" className="mb-3 flex items-center justify-between border-b border-slate-200 pb-2">
        {!sidebarCollapsed ? (
          <div>
            <p
              id="workspace-sidebar-eyebrow"
              className="text-[9px] uppercase tracking-[0.12em] text-slate-400"
            >
              {shellContent.navigation}
            </p>
            <p id="workspace-sidebar-label" className="mt-0.5 text-[11px] font-semibold text-slate-700">
              {label}
            </p>
          </div>
        ) : (
          <div className="text-[9px] uppercase tracking-[0.12em] text-slate-400">
            {shellContent.navShort}
          </div>
        )}

        <button
          id="workspace-sidebar-toggle"
          type="button"
          onClick={onToggleCollapsed}
          className="rounded-[3px] border border-slate-300 p-1 text-slate-500 transition-colors hover:bg-slate-100 hover:text-slate-950"
        >
          {sidebarCollapsed ? (
            <ChevronRight className="h-3.5 w-3.5" />
          ) : (
            <ChevronLeft className="h-3.5 w-3.5" />
          )}
        </button>
      </div>

      <div id="workspace-sidebar-tree" className="space-y-1">
        {menus.map((menu) => (
          <WorkspaceSidebarNode
            key={menu.id}
            activeTabId={activeTabId}
            expandedMenuIds={expandedMenuIds}
            menu={menu}
            onMenuSelect={onMenuSelect}
            sidebarCollapsed={sidebarCollapsed}
          />
        ))}
      </div>
    </aside>
  );
}
