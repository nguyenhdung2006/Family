import type { Edge, Node } from "@xyflow/react";
import type { FamilyMember, FamilyRelationship } from "@/features/family/types";

export type FamilyNodeData = FamilyMember & {
  relationshipCount: number;
};

export function buildFamilyGraph(
  members: FamilyMember[],
  relationships: FamilyRelationship[]
): { nodes: Node<FamilyNodeData>[]; edges: Edge[] } {
  const generationGroups = members.reduce<Record<number, FamilyMember[]>>((groups, member) => {
    groups[member.generationLevel] = [...(groups[member.generationLevel] ?? []), member];
    return groups;
  }, {});

  const relationshipCounts = relationships.reduce<Record<string, number>>((counts, relationship) => {
    counts[relationship.sourceMemberId] = (counts[relationship.sourceMemberId] ?? 0) + 1;
    counts[relationship.targetMemberId] = (counts[relationship.targetMemberId] ?? 0) + 1;
    return counts;
  }, {});

  const nodes: Node<FamilyNodeData>[] = Object.entries(generationGroups).flatMap(([generation, group]) => {
    const y = Number(generation) * 190;
    const rowWidth = (group.length - 1) * 280;
    return group.map((member, index) => ({
      id: member.id,
      type: "familyMember",
      position: { x: index * 280 - rowWidth / 2, y },
      data: { ...member, relationshipCount: relationshipCounts[member.id] ?? 0 }
    }));
  });

  const edges: Edge[] = relationships.map((relationship) => ({
    id: relationship.id,
    source: relationship.sourceMemberId,
    target: relationship.targetMemberId,
    type: relationship.type === "SPOUSE" ? "smoothstep" : "default",
    animated: relationship.type === "PARENT_CHILD",
    label: relationship.type.replace("_", " ").toLowerCase(),
    data: relationship,
    style: {
      stroke: relationship.type === "SPOUSE" ? "#8B5E3C" : relationship.type === "SIBLING" ? "#6F8FAF" : "#7A9E7E",
      strokeWidth: 2.5
    }
  }));

  return { nodes, edges };
}
