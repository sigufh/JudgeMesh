package com.judgemesh.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Smoke test: boots the full Gateway ApplicationContext on a random port
 * (with Nacos / Sentinel discovery & config disabled) and asserts that
 * /actuator/health responds 200 with status UP.
 */
@SpringBootTest(
        webEnvironment = WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.cloud.nacos.discovery.enabled=false",
                "spring.cloud.nacos.discovery.register-enabled=false",
                "spring.cloud.nacos.config.enabled=false",
                "spring.cloud.nacos.config.import-check.enabled=false",
                "spring.cloud.config.import-check.enabled=false",
                "spring.cloud.service-registry.auto-registration.enabled=false",
                "spring.cloud.discovery.enabled=false",
                "spring.config.import=",
                "spring.cloud.gateway.server.webflux.discovery.locator.enabled=false",
                "eureka.client.enabled=false"
        }
)
@ActiveProfiles("test")
@TestPropertySource(properties = "spring.config.import=")
class ApplicationSmokeTest {

    @Autowired(required = false)
    private WebTestClient webTestClient;

    @LocalServerPort
    private int port;

    @Test
    void healthEndpointReturnsUp() {
        WebTestClient client = webTestClient != null
                ? webTestClient
                : WebTestClient.bindToServer().baseUrl("http://localhost:" + port).build();

        client.get()
                .uri("/actuator/health")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(body -> assertThat(body).contains("\"status\":\"UP\""));
    }
}
