export const queryKeys = {
  authMe: ["auth", "me"] as const,
  familyMembers: (filters?: unknown) => ["family", "members", filters ?? {}] as const,
  familyRelationships: (memberId: string) => ["family", "relationships", memberId] as const,
  timelinePosts: (filters?: unknown) => ["timeline", "posts", filters ?? {}] as const,
  albums: (filters?: unknown) => ["albums", filters ?? {}] as const,
  albumMedia: (albumId: string) => ["albums", albumId, "media"] as const,
  chatRooms: ["chat", "rooms"] as const,
  chatMessages: (roomId: string) => ["chat", "rooms", roomId, "messages"] as const,
  notifications: ["notifications"] as const,
  memorials: ["memorials"] as const,
  memorialTributes: (memberId: string) => ["memorials", memberId, "tributes"] as const,
  recipes: ["kitchen", "recipes"] as const
};
