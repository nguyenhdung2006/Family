"use client";

import { useEffect, useMemo, useState } from "react";
import { useQueries } from "@tanstack/react-query";
import { Background, Controls, MiniMap, ReactFlow, useEdgesState, useNodesState } from "@xyflow/react";
import { Search } from "lucide-react";
import { FamilyNode } from "@/components/family-tree/family-node";
import { MemberDrawer } from "@/components/family-tree/member-drawer";
import { Card, CardContent } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Badge } from "@/components/ui/badge";
import { useFamilyMembers } from "@/features/family/hooks";
import { buildFamilyGraph } from "@/features/family/graph";
import { listRelationships } from "@/features/family/api";
import type { FamilyRelationship } from "@/features/family/types";
import { queryKeys } from "@/lib/api/queryKeys";
import { useTreeStore } from "@/stores/tree-store";

const nodeTypes = { familyMember: FamilyNode };

export function FamilyTreeCanvas() {
  const [search, setSearch] = useState("");
  const { selectedMemberId, setSelectedMemberId } = useTreeStore();
  const { data: members = [], isLoading } = useFamilyMembers({ page: 0, size: 250 });

  const relationshipQueries = useQueries({
    queries: members.map((member) => ({
      queryKey: queryKeys.familyRelationships(member.id),
      queryFn: () => listRelationships(member.id),
      staleTime: 5 * 60_000
    }))
  });

  const relationships = useMemo(() => {
    const map = new Map<string, FamilyRelationship>();
    relationshipQueries.forEach((query) => {
      query.data?.forEach((relationship) => map.set(relationship.id, relationship));
    });
    return Array.from(map.values());
  }, [relationshipQueries]);

  const filteredMembers = useMemo(() => {
    const normalized = search.trim().toLowerCase();
    if (!normalized) {
      return members;
    }
    return members.filter((member) => `${member.fullName} ${member.roleInFamily ?? ""}`.toLowerCase().includes(normalized));
  }, [members, search]);

  const graph = useMemo(() => buildFamilyGraph(filteredMembers, relationships), [filteredMembers, relationships]);
  const [nodes, setNodes, onNodesChange] = useNodesState(graph.nodes);
  const [edges, setEdges, onEdgesChange] = useEdgesState(graph.edges);

  useEffect(() => {
    setNodes(graph.nodes);
    setEdges(graph.edges);
  }, [graph.edges, graph.nodes, setEdges, setNodes]);

  const selectedMember = members.find((member) => member.id === selectedMemberId);

  if (isLoading) {
    return <Card><CardContent><p className="text-lg font-bold text-muted">Loading the family tree...</p></CardContent></Card>;
  }

  return (
    <div className="relative overflow-hidden rounded-lg border border-border-warm bg-[#FFFDF7] shadow-[0_18px_45px_rgba(80,55,33,0.08)]">
      <div className="absolute left-4 top-4 z-10 flex w-[calc(100%-2rem)] flex-col gap-3 sm:w-auto sm:min-w-80">
        <div className="relative">
          <Search className="pointer-events-none absolute left-3 top-1/2 h-5 w-5 -translate-y-1/2 text-muted" />
          <Input value={search} onChange={(event) => setSearch(event.target.value)} placeholder="Search family members" className="pl-10" />
        </div>
        <div className="flex flex-wrap gap-2">
          <Badge tone="sage">{members.length} members</Badge>
          <Badge tone="blue">{relationships.length} relationships</Badge>
        </div>
      </div>

      <div className="h-[calc(100vh-11rem)] min-h-[35rem]">
        <ReactFlow
          nodes={nodes}
          edges={edges}
          nodeTypes={nodeTypes}
          onNodesChange={onNodesChange}
          onEdgesChange={onEdgesChange}
          onNodeClick={(_, node) => setSelectedMemberId(node.id)}
          fitView
          fitViewOptions={{ padding: 0.25 }}
          minZoom={0.25}
          maxZoom={1.6}
          onlyRenderVisibleElements
        >
          <Background color="#E7D8C3" gap={28} />
          <Controls position="bottom-left" />
          <MiniMap pannable zoomable nodeColor="#F4B942" maskColor="rgba(255,248,234,0.72)" />
        </ReactFlow>
      </div>

      {!members.length ? (
        <div className="absolute inset-0 grid place-items-center bg-background/70 p-6 text-center">
          <Card className="max-w-lg">
            <CardContent>
              <p className="text-2xl font-black text-ink">No family members yet</p>
              <p className="mt-2 text-base font-semibold leading-7 text-muted">Add members from the backend/API and the graph will render them here.</p>
            </CardContent>
          </Card>
        </div>
      ) : null}

      <MemberDrawer member={selectedMember} onClose={() => setSelectedMemberId(null)} />
    </div>
  );
}
