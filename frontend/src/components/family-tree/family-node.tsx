"use client";

import { Handle, Position, type NodeProps } from "@xyflow/react";
import { ChevronDown, ChevronRight } from "lucide-react";
import { Avatar } from "@/components/ui/avatar";
import { Badge } from "@/components/ui/badge";
import type { FamilyNodeData } from "@/features/family/graph";
import { useTreeStore } from "@/stores/tree-store";
import { cn } from "@/lib/utils/cn";

export function FamilyNode({ data, selected }: NodeProps) {
  const node = data as FamilyNodeData;
  const { expandedMemberIds, toggleExpanded } = useTreeStore();
  const expanded = expandedMemberIds.includes(node.id);

  return (
    <div
      className={cn(
        "w-60 rounded-lg border bg-white p-3 shadow-[0_16px_35px_rgba(80,55,33,0.12)] transition",
        selected ? "border-warm-yellow ring-4 ring-warm-yellow/25" : "border-border-warm"
      )}
    >
      <Handle type="target" position={Position.Top} className="!bg-sage" />
      <div className="flex items-center gap-3">
        <Avatar name={node.fullName} src={node.avatarUrl} className="h-14 w-14" />
        <div className="min-w-0">
          <p className="truncate text-lg font-black text-ink">{node.fullName}</p>
          <p className="truncate text-sm font-bold text-muted">{node.roleInFamily ?? "Family member"}</p>
        </div>
      </div>
      <div className="mt-3 flex items-center justify-between gap-2">
        <Badge tone={node.branch === "PATERNAL" ? "sage" : "blue"}>Gen {node.generationLevel}</Badge>
        <button
          className="inline-flex min-h-9 items-center gap-1 rounded-lg px-2 text-sm font-black text-wood hover:bg-surface-soft"
          onClick={(event) => {
            event.stopPropagation();
            toggleExpanded(node.id);
          }}
        >
          {expanded ? <ChevronDown className="h-4 w-4" /> : <ChevronRight className="h-4 w-4" />}
          {node.relationshipCount}
        </button>
      </div>
      <Handle type="source" position={Position.Bottom} className="!bg-sage" />
    </div>
  );
}
