package com.familyhub.digital_family_hub.kitchen;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.familyhub.digital_family_hub.users.AppUser;
import com.familyhub.digital_family_hub.users.AppUserRepository;
import com.familyhub.digital_family_hub.users.UserRole;
import java.security.Principal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

class KitchenServiceTest {

    private final RecipeRepository recipes = mock(RecipeRepository.class);
    private final AppUserRepository users = mock(AppUserRepository.class);
    private final KitchenService service = new KitchenService(recipes, users);

    @Test
    void listRecipesWithoutSearchUsesExistingListBehavior() {
        Recipe recipe = recipe(UUID.randomUUID(), user(UUID.randomUUID(), "member@example.com", UserRole.MEMBER));
        when(recipes.findAll()).thenReturn(List.of(recipe));

        List<RecipeDTO.Response> responses = service.listRecipes(" ");

        assertThat(responses).hasSize(1);
        assertThat(responses.getFirst().id()).isEqualTo(recipe.getId());
        verify(recipes).findAll();
        verify(recipes, never()).searchByText(any());
    }

    @Test
    void listRecipesWithSearchUsesTextSearch() {
        Recipe recipe = recipe(UUID.randomUUID(), user(UUID.randomUUID(), "member@example.com", UserRole.MEMBER));
        when(recipes.searchByText("ginger")).thenReturn(List.of(recipe));

        List<RecipeDTO.Response> responses = service.listRecipes(" ginger ");

        assertThat(responses).hasSize(1);
        assertThat(responses.getFirst().id()).isEqualTo(recipe.getId());
        verify(recipes).searchByText("ginger");
        verify(recipes, never()).findAll();
    }

    @Test
    void updateRecipeAllowsOwner() {
        AppUser owner = user(UUID.randomUUID(), "member@example.com", UserRole.MEMBER);
        UUID recipeId = UUID.randomUUID();
        Recipe recipe = recipe(recipeId, owner);
        when(users.findByEmailIgnoreCase(owner.getEmail())).thenReturn(Optional.of(owner));
        when(recipes.findById(recipeId)).thenReturn(Optional.of(recipe));
        when(recipes.save(any(Recipe.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RecipeDTO.Response response = service.updateRecipe(recipeId, request("Updated recipe"), principal(owner.getEmail()));

        assertThat(response.id()).isEqualTo(recipeId);
        assertThat(response.createdById()).isEqualTo(owner.getId());
        assertThat(response.title()).isEqualTo("Updated recipe");
        verify(recipes).save(recipe);
    }

    @Test
    void updateRecipeHidesRecipesOwnedByAnotherUser() {
        AppUser currentUser = user(UUID.randomUUID(), "member@example.com", UserRole.MEMBER);
        AppUser otherUser = user(UUID.randomUUID(), "other@example.com", UserRole.MEMBER);
        UUID recipeId = UUID.randomUUID();
        when(users.findByEmailIgnoreCase(currentUser.getEmail())).thenReturn(Optional.of(currentUser));
        when(recipes.findById(recipeId)).thenReturn(Optional.of(recipe(recipeId, otherUser)));

        assertThatThrownBy(() -> service.updateRecipe(recipeId, request("Nope"), principal(currentUser.getEmail())))
            .isInstanceOf(ResponseStatusException.class)
            .satisfies(exception ->
                assertThat(((ResponseStatusException) exception).getStatusCode().value()).isEqualTo(404)
            );
        verify(recipes, never()).save(any());
    }

    @Test
    void deleteRecipeAllowsOwner() {
        AppUser owner = user(UUID.randomUUID(), "member@example.com", UserRole.MEMBER);
        UUID recipeId = UUID.randomUUID();
        Recipe recipe = recipe(recipeId, owner);
        when(users.findByEmailIgnoreCase(owner.getEmail())).thenReturn(Optional.of(owner));
        when(recipes.findById(recipeId)).thenReturn(Optional.of(recipe));

        service.deleteRecipe(recipeId, principal(owner.getEmail()));

        verify(recipes).delete(recipe);
    }

    @Test
    void deleteRecipeAllowsAdminForAnyRecipe() {
        AppUser admin = user(UUID.randomUUID(), "admin@example.com", UserRole.ADMIN);
        AppUser owner = user(UUID.randomUUID(), "member@example.com", UserRole.MEMBER);
        UUID recipeId = UUID.randomUUID();
        Recipe recipe = recipe(recipeId, owner);
        when(users.findByEmailIgnoreCase(admin.getEmail())).thenReturn(Optional.of(admin));
        when(recipes.findById(recipeId)).thenReturn(Optional.of(recipe));

        service.deleteRecipe(recipeId, principal(admin.getEmail()));

        verify(recipes).delete(recipe);
    }

    @Test
    void deleteRecipeHidesRecipesOwnedByAnotherUser() {
        AppUser currentUser = user(UUID.randomUUID(), "member@example.com", UserRole.MEMBER);
        AppUser otherUser = user(UUID.randomUUID(), "other@example.com", UserRole.MEMBER);
        UUID recipeId = UUID.randomUUID();
        when(users.findByEmailIgnoreCase(currentUser.getEmail())).thenReturn(Optional.of(currentUser));
        when(recipes.findById(recipeId)).thenReturn(Optional.of(recipe(recipeId, otherUser)));

        assertThatThrownBy(() -> service.deleteRecipe(recipeId, principal(currentUser.getEmail())))
            .isInstanceOf(ResponseStatusException.class)
            .satisfies(exception ->
                assertThat(((ResponseStatusException) exception).getStatusCode().value()).isEqualTo(404)
            );
        verify(recipes, never()).delete(any());
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

    private Recipe recipe(UUID id, AppUser owner) {
        Recipe recipe = new Recipe();
        recipe.setId(id);
        recipe.setCreatedBy(owner);
        recipe.setTitle("Family recipe");
        recipe.setDescription("Description");
        recipe.setIngredients("Ingredients");
        recipe.setInstructions("Instructions");
        return recipe;
    }

    private RecipeDTO.Request request(String title) {
        return new RecipeDTO.Request(
            title,
            "Description",
            "Ingredients",
            "Instructions",
            "https://example.com/video",
            "Notes"
        );
    }
}
