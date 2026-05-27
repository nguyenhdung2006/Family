package com.familyhub.digital_family_hub.memorials;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.familyhub.digital_family_hub.family.FamilyBranch;
import com.familyhub.digital_family_hub.family.FamilyMember;
import com.familyhub.digital_family_hub.family.FamilyMemberRepository;
import com.familyhub.digital_family_hub.users.AppUser;
import com.familyhub.digital_family_hub.users.AppUserRepository;
import com.familyhub.digital_family_hub.users.UserRole;
import java.security.Principal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

class MemorialServiceTest {

    private final FamilyMemberRepository members = mock(FamilyMemberRepository.class);
    private final MemorialTributeRepository tributes = mock(MemorialTributeRepository.class);
    private final AppUserRepository users = mock(AppUserRepository.class);
    private final MemorialService service = new MemorialService(members, tributes, users);

    @Test
    void updateTributeAllowsAuthor() {
        AppUser author = user(UUID.randomUUID(), "member@example.com", UserRole.MEMBER);
        FamilyMember member = member(UUID.randomUUID());
        UUID tributeId = UUID.randomUUID();
        MemorialTribute tribute = tribute(tributeId, member, author);
        when(users.findByEmailIgnoreCase(author.getEmail())).thenReturn(Optional.of(author));
        when(tributes.findById(tributeId)).thenReturn(Optional.of(tribute));
        when(tributes.save(any(MemorialTribute.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MemorialDTO.TributeResponse response = service.updateTribute(
            member.getId(),
            tributeId,
            request("Updated tribute"),
            principal(author.getEmail())
        );

        assertThat(response.id()).isEqualTo(tributeId);
        assertThat(response.authorId()).isEqualTo(author.getId());
        assertThat(response.title()).isEqualTo("Updated tribute");
        verify(tributes).save(tribute);
    }

    @Test
    void updateTributeHidesTributesOwnedByAnotherUser() {
        AppUser currentUser = user(UUID.randomUUID(), "member@example.com", UserRole.MEMBER);
        AppUser otherUser = user(UUID.randomUUID(), "other@example.com", UserRole.MEMBER);
        FamilyMember member = member(UUID.randomUUID());
        UUID tributeId = UUID.randomUUID();
        when(users.findByEmailIgnoreCase(currentUser.getEmail())).thenReturn(Optional.of(currentUser));
        when(tributes.findById(tributeId)).thenReturn(Optional.of(tribute(tributeId, member, otherUser)));

        assertThatThrownBy(() -> service.updateTribute(member.getId(), tributeId, request("Nope"), principal(currentUser.getEmail())))
            .isInstanceOf(ResponseStatusException.class)
            .satisfies(exception ->
                assertThat(((ResponseStatusException) exception).getStatusCode().value()).isEqualTo(404)
            );
        verify(tributes, never()).save(any());
    }

    @Test
    void deleteTributeAllowsAuthor() {
        AppUser author = user(UUID.randomUUID(), "member@example.com", UserRole.MEMBER);
        FamilyMember member = member(UUID.randomUUID());
        UUID tributeId = UUID.randomUUID();
        MemorialTribute tribute = tribute(tributeId, member, author);
        when(users.findByEmailIgnoreCase(author.getEmail())).thenReturn(Optional.of(author));
        when(tributes.findById(tributeId)).thenReturn(Optional.of(tribute));

        service.deleteTribute(member.getId(), tributeId, principal(author.getEmail()));

        verify(tributes).delete(tribute);
    }

    @Test
    void deleteTributeAllowsAdminForAnyTribute() {
        AppUser admin = user(UUID.randomUUID(), "admin@example.com", UserRole.ADMIN);
        AppUser author = user(UUID.randomUUID(), "member@example.com", UserRole.MEMBER);
        FamilyMember member = member(UUID.randomUUID());
        UUID tributeId = UUID.randomUUID();
        MemorialTribute tribute = tribute(tributeId, member, author);
        when(users.findByEmailIgnoreCase(admin.getEmail())).thenReturn(Optional.of(admin));
        when(tributes.findById(tributeId)).thenReturn(Optional.of(tribute));

        service.deleteTribute(member.getId(), tributeId, principal(admin.getEmail()));

        verify(tributes).delete(tribute);
    }

    @Test
    void deleteTributeRejectsWrongMemberPath() {
        AppUser author = user(UUID.randomUUID(), "member@example.com", UserRole.MEMBER);
        UUID tributeId = UUID.randomUUID();
        MemorialTribute tribute = tribute(tributeId, member(UUID.randomUUID()), author);
        when(users.findByEmailIgnoreCase(author.getEmail())).thenReturn(Optional.of(author));
        when(tributes.findById(tributeId)).thenReturn(Optional.of(tribute));

        assertThatThrownBy(() -> service.deleteTribute(UUID.randomUUID(), tributeId, principal(author.getEmail())))
            .isInstanceOf(ResponseStatusException.class)
            .satisfies(exception ->
                assertThat(((ResponseStatusException) exception).getStatusCode().value()).isEqualTo(404)
            );
        verify(tributes, never()).delete(any());
    }

    private Principal principal(String email) {
        return () -> email;
    }

    private AppUser user(UUID id, String email, UserRole role) {
        AppUser user = new AppUser();
        user.setId(id);
        user.setEmail(email);
        user.setName("Family Member");
        user.setRole(role);
        return user;
    }

    private FamilyMember member(UUID id) {
        FamilyMember member = new FamilyMember();
        member.setId(id);
        member.setFullName("Beloved Member");
        member.setBranch(FamilyBranch.PATERNAL);
        member.setGenerationLevel(1);
        member.setDeathDate(LocalDate.parse("2020-01-01"));
        return member;
    }

    private MemorialTribute tribute(UUID id, FamilyMember member, AppUser author) {
        MemorialTribute tribute = new MemorialTribute();
        tribute.setId(id);
        tribute.setMember(member);
        tribute.setAuthor(author);
        tribute.setTitle("Original tribute");
        tribute.setStory("Story");
        return tribute;
    }

    private MemorialDTO.TributeRequest request(String title) {
        return new MemorialDTO.TributeRequest(title, "Remembering a beautiful life");
    }
}
