package com.familyhub.digital_family_hub;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Disabled("Enable when Docker is available in the test environment.")
@Testcontainers
class PostgresContainerIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
        .withDatabaseName("hometree")
        .withUsername("hometree")
        .withPassword("hometree");

    @Test
    void postgresContainerStarts() {
        org.assertj.core.api.Assertions.assertThat(POSTGRES.isRunning()).isTrue();
    }
}
