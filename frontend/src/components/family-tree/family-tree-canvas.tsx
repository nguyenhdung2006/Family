"use client";

import { FormEvent, useEffect, useMemo, useState } from "react";
import { useQueries } from "@tanstack/react-query";
import { Background, Controls, MiniMap, ReactFlow, useEdgesState, useNodesState } from "@xyflow/react";
import { Link2, Plus, Search } from "lucide-react";
import { FamilyNode } from "@/components/family-tree/family-node";
import { MemberDrawer } from "@/components/family-tree/member-drawer";
import { Card, CardContent } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input, Textarea } from "@/components/ui/input";
import { Badge } from "@/components/ui/badge";
import { useCreateFamilyMember, useCreateFamilyRelationship, useFamilyMembers } from "@/features/family/hooks";
import { buildFamilyGraph } from "@/features/family/graph";
import { listRelationships } from "@/features/family/api";
import type { FamilyBranch, FamilyRelationship, RelationshipType } from "@/features/family/types";
import { queryKeys } from "@/lib/api/queryKeys";
import { useTreeStore } from "@/stores/tree-store";

const nodeTypes = { familyMember: FamilyNode };

export function FamilyTreeCanvas() {
  const [search, setSearch] = useState("");
  const [memberForm, setMemberForm] = useState({
    fullName: "",
    birthDate: "",
    deathDate: "",
    roleInFamily: "",
    branch: "PATERNAL" as FamilyBranch,
    generationLevel: "0",
    avatarUrl: "",
    biography: ""
  });
  const [relationshipForm, setRelationshipForm] = useState({
    sourceMemberId: "",
    targetMemberId: "",
    type: "PARENT_CHILD" as RelationshipType,
    notes: ""
  });
  const { selectedMemberId, setSelectedMemberId } = useTreeStore();
  const { data: members = [], isLoading, error } = useFamilyMembers({ page: 0, size: 250 });
  const createMember = useCreateFamilyMember();
  const createRelationship = useCreateFamilyRelationship();

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

  async function submitMember(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!memberForm.fullName.trim()) {
      return;
    }
    await createMember.mutateAsync({
      fullName: memberForm.fullName.trim(),
      birthDate: memberForm.birthDate || null,
      deathDate: memberForm.deathDate || null,
      roleInFamily: memberForm.roleInFamily.trim() || null,
      branch: memberForm.branch,
      avatarUrl: memberForm.avatarUrl.trim() || null,
      biography: memberForm.biography.trim() || null,
      generationLevel: Number(memberForm.generationLevel || 0)
    });
    setMemberForm({
      fullName: "",
      birthDate: "",
      deathDate: "",
      roleInFamily: "",
      branch: "PATERNAL",
      generationLevel: "0",
      avatarUrl: "",
      biography: ""
    });
  }

  async function submitRelationship(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!relationshipForm.sourceMemberId || !relationshipForm.targetMemberId || relationshipForm.sourceMemberId === relationshipForm.targetMemberId) {
      return;
    }
    await createRelationship.mutateAsync({
      sourceMemberId: relationshipForm.sourceMemberId,
      targetMemberId: relationshipForm.targetMemberId,
      type: relationshipForm.type,
      notes: relationshipForm.notes.trim() || null
    });
    setRelationshipForm((current) => ({ ...current, notes: "" }));
  }

  if (isLoading) {
    return <Card><CardContent><p className="text-lg font-bold text-muted">Loading the family tree...</p></CardContent></Card>;
  }

  return (
    <div className="grid gap-5">
      <div className="grid gap-4 xl:grid-cols-[minmax(0,1fr)_minmax(0,1fr)]">
        <Card>
          <CardContent>
            <form className="grid gap-3" onSubmit={submitMember}>
              <div className="flex items-center justify-between gap-3">
                <h2 className="text-xl font-black text-ink">Add family member</h2>
                <Badge tone="sage">Profile</Badge>
              </div>
              <div className="grid gap-3 md:grid-cols-2">
                <Input value={memberForm.fullName} onChange={(event) => setMemberForm((form) => ({ ...form, fullName: event.target.value }))} placeholder="Full name" required />
                <Input value={memberForm.roleInFamily} onChange={(event) => setMemberForm((form) => ({ ...form, roleInFamily: event.target.value }))} placeholder="Role in family" />
                <Input type="date" value={memberForm.birthDate} onChange={(event) => setMemberForm((form) => ({ ...form, birthDate: event.target.value }))} aria-label="Birth date" />
                <Input type="date" value={memberForm.deathDate} onChange={(event) => setMemberForm((form) => ({ ...form, deathDate: event.target.value }))} aria-label="Death date" />
                <select className="min-h-12 rounded-lg border border-border-warm bg-white px-3 text-base font-bold text-ink" value={memberForm.branch} onChange={(event) => setMemberForm((form) => ({ ...form, branch: event.target.value as FamilyBranch }))}>
                  <option value="PATERNAL">Paternal</option>
                  <option value="MATERNAL">Maternal</option>
                </select>
                <Input type="number" min={0} value={memberForm.generationLevel} onChange={(event) => setMemberForm((form) => ({ ...form, generationLevel: event.target.value }))} placeholder="Generation" />
              </div>
              <Input value={memberForm.avatarUrl} onChange={(event) => setMemberForm((form) => ({ ...form, avatarUrl: event.target.value }))} placeholder="Avatar URL" />
              <Textarea value={memberForm.biography} onChange={(event) => setMemberForm((form) => ({ ...form, biography: event.target.value }))} placeholder="Biography" />
              {createMember.error ? <p className="font-bold text-[#C15A4A]">{createMember.error.message}</p> : null}
              <Button type="submit" disabled={createMember.isPending}>
                <Plus className="h-5 w-5" /> {createMember.isPending ? "Adding..." : "Add member"}
              </Button>
            </form>
          </CardContent>
        </Card>

        <Card>
          <CardContent>
            <form className="grid gap-3" onSubmit={submitRelationship}>
              <div className="flex items-center justify-between gap-3">
                <h2 className="text-xl font-black text-ink">Link relationship</h2>
                <Badge tone="blue">{members.length} members</Badge>
              </div>
              <div className="grid gap-3 md:grid-cols-2">
                <select className="min-h-12 rounded-lg border border-border-warm bg-white px-3 text-base font-bold text-ink" value={relationshipForm.sourceMemberId} onChange={(event) => setRelationshipForm((form) => ({ ...form, sourceMemberId: event.target.value }))} required>
                  <option value="">Source member</option>
                  {members.map((member) => <option key={member.id} value={member.id}>{member.fullName}</option>)}
                </select>
                <select className="min-h-12 rounded-lg border border-border-warm bg-white px-3 text-base font-bold text-ink" value={relationshipForm.targetMemberId} onChange={(event) => setRelationshipForm((form) => ({ ...form, targetMemberId: event.target.value }))} required>
                  <option value="">Target member</option>
                  {members.map((member) => <option key={member.id} value={member.id}>{member.fullName}</option>)}
                </select>
                <select className="min-h-12 rounded-lg border border-border-warm bg-white px-3 text-base font-bold text-ink" value={relationshipForm.type} onChange={(event) => setRelationshipForm((form) => ({ ...form, type: event.target.value as RelationshipType }))}>
                  <option value="PARENT_CHILD">Parent child</option>
                  <option value="SPOUSE">Spouse</option>
                  <option value="SIBLING">Sibling</option>
                </select>
                <Input value={relationshipForm.notes} onChange={(event) => setRelationshipForm((form) => ({ ...form, notes: event.target.value }))} placeholder="Notes" />
              </div>
              {relationshipForm.sourceMemberId && relationshipForm.sourceMemberId === relationshipForm.targetMemberId ? <p className="font-bold text-[#C15A4A]">Choose two different members.</p> : null}
              {createRelationship.error ? <p className="font-bold text-[#C15A4A]">{createRelationship.error.message}</p> : null}
              <Button type="submit" disabled={createRelationship.isPending || members.length < 2}>
                <Link2 className="h-5 w-5" /> {createRelationship.isPending ? "Linking..." : "Link members"}
              </Button>
            </form>
          </CardContent>
        </Card>
      </div>

      {error ? <Card><CardContent><p className="font-bold text-[#C15A4A]">{error.message}</p></CardContent></Card> : null}

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
    </div>
  );
}
