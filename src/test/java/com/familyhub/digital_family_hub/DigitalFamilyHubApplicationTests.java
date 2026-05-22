package com.familyhub.digital_family_hub;

import com.familyhub.digital_family_hub.albums.AlbumRepository;
import com.familyhub.digital_family_hub.chat.ChatMessageRepository;
import com.familyhub.digital_family_hub.chat.ChatRoomRepository;
import com.familyhub.digital_family_hub.family.FamilyMemberRepository;
import com.familyhub.digital_family_hub.family.FamilyRelationshipRepository;
import com.familyhub.digital_family_hub.kitchen.RecipeRepository;
import com.familyhub.digital_family_hub.media.MediaAssetRepository;
import com.familyhub.digital_family_hub.memorials.MemorialTributeRepository;
import com.familyhub.digital_family_hub.notifications.InAppNotificationRepository;
import com.familyhub.digital_family_hub.posts.MemoryPostRepository;
import com.familyhub.digital_family_hub.users.AppUserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(properties = {
	"spring.autoconfigure.exclude="
		+ "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,"
		+ "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration,"
		+ "org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration"
})
class DigitalFamilyHubApplicationTests {

	@MockitoBean
	AppUserRepository appUserRepository;

	@MockitoBean
	FamilyMemberRepository familyMemberRepository;

	@MockitoBean
	FamilyRelationshipRepository familyRelationshipRepository;

	@MockitoBean
	MemoryPostRepository memoryPostRepository;

	@MockitoBean
	AlbumRepository albumRepository;

	@MockitoBean
	MediaAssetRepository mediaAssetRepository;

	@MockitoBean
	ChatRoomRepository chatRoomRepository;

	@MockitoBean
	ChatMessageRepository chatMessageRepository;

	@MockitoBean
	InAppNotificationRepository inAppNotificationRepository;

	@MockitoBean
	RecipeRepository recipeRepository;

	@MockitoBean
	MemorialTributeRepository memorialTributeRepository;

	@Test
	void contextLoads() {
	}

}
