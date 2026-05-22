package com.familyhub.digital_family_hub.chat;

import com.familyhub.digital_family_hub.family.FamilyBranch;
import com.familyhub.digital_family_hub.shared.domain.AuditableEntity;
import com.familyhub.digital_family_hub.users.AppUser;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(
    name = "chat_rooms",
    indexes = {
        @Index(name = "idx_chat_rooms_type", columnList = "type"),
        @Index(name = "idx_chat_rooms_branch", columnList = "branch")
    }
)
public class ChatRoom extends AuditableEntity {

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChatRoomType type;

    @Enumerated(EnumType.STRING)
    private FamilyBranch branch;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "chat_room_members",
        joinColumns = @JoinColumn(name = "room_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<AppUser> participants = new LinkedHashSet<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ChatRoomType getType() {
        return type;
    }

    public void setType(ChatRoomType type) {
        this.type = type;
    }

    public FamilyBranch getBranch() {
        return branch;
    }

    public void setBranch(FamilyBranch branch) {
        this.branch = branch;
    }

    public Set<AppUser> getParticipants() {
        return participants;
    }
}
