package com.familyhub.digital_family_hub.kitchen;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RecipeRepository extends JpaRepository<Recipe, UUID> {
    @Query("""
        select recipe from Recipe recipe
        where lower(recipe.title) like lower(concat('%', :search, '%'))
           or lower(coalesce(recipe.description, '')) like lower(concat('%', :search, '%'))
           or lower(recipe.ingredients) like lower(concat('%', :search, '%'))
        """)
    List<Recipe> searchByText(@Param("search") String search);
}
