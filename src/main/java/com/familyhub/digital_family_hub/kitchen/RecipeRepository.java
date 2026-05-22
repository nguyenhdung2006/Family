package com.familyhub.digital_family_hub.kitchen;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecipeRepository extends JpaRepository<Recipe, UUID> {
}
