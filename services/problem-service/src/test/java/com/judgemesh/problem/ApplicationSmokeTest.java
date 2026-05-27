package com.judgemesh.problem;

import com.judgemesh.problem.mapper.ProblemMapper;
import com.judgemesh.problem.mapper.ProblemTagMapper;
import com.judgemesh.problem.mapper.TestcaseManifestMapper;
import io.minio.MinioClient;
import org.junit.jupiter.api.Test;
import org.redisson.api.RedissonClient;
import org.redisson.spring.starter.RedissonAutoConfigurationV2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
// 修改：使用 Spring Boot 3.4+ 全新的 MockitoBean
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.assertj.core.api.Assertions.assertThat;

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
        "spring.flyway.enabled=false",
        "eureka.client.enabled=false"
    }
)
@EnableAutoConfiguration(exclude = {
    DataSourceAutoConfiguration.class,
    DataSourceTransactionManagerAutoConfiguration.class,
    HibernateJpaAutoConfiguration.class,
    FlywayAutoConfiguration.class,
    RedisAutoConfiguration.class,
    RedisRepositoriesAutoConfiguration.class,
    RedissonAutoConfigurationV2.class
})
@ActiveProfiles("test")
@TestPropertySource(properties = "spring.config.import=")
class ApplicationSmokeTest {

    @Autowired
    private TestRestTemplate restTemplate;

    // @MockitoBean
    @MockitoBean private ProblemMapper problemMapper;
    @MockitoBean private ProblemTagMapper problemTagMapper;
    @MockitoBean private TestcaseManifestMapper testcaseManifestMapper;
    @MockitoBean private StringRedisTemplate stringRedisTemplate;
    @MockitoBean private RedissonClient redissonClient;
    @MockitoBean private MinioClient minioClient;

    @Test
    void healthEndpointReturnsUp() {
        ResponseEntity<String> response = restTemplate.getForEntity("/actuator/health", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("\"status\":\"UP\"");
    }
}
