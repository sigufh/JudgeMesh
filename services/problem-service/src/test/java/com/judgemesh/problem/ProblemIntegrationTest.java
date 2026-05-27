package com.judgemesh.problem;

import com.judgemesh.problem.service.ProblemService;
import com.judgemesh.problem.vo.ProblemCreateReq;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest
class ProblemIntegrationTest {

    // 1. 启动临时的 MySQL 8.0 容器
    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.4")
        .withDatabaseName("problems_db")
        .withUsername("judgemesh")
        .withPassword("judgemesh");

    // 2. 启动临时的 Redis 7 容器
    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7.4-alpine")
        .withExposedPorts(6379);

    // 3. 动态把容器的随机端口注入到 Spring Boot 配置中
    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", redis::getFirstMappedPort);
        registry.add("spring.flyway.enabled", () -> "true"); // 开启 Flyway 自动建表
        // 排除 Nacos 和 MinIO 干扰
        registry.add("spring.cloud.nacos.discovery.enabled", () -> "false");
        registry.add("spring.cloud.nacos.config.enabled", () -> "false");
    }

    @Autowired
    private ProblemService problemService;

    @Test
    void testCreateAndGetProblemFullFlow() {
        // 测试真实的 DB 插入与 Redis 缓存双写流程
        ProblemCreateReq req = new ProblemCreateReq();
        req.setTitle("Test Integration");
        req.setDescription("Desc");
        req.setTimeLimitMs(1000);
        req.setMemoryLimitMb(256);
        req.setDifficulty("EASY");
        req.setTags(List.of("TestTag"));

        Long pid = problemService.createProblem(req, 1L);
        assertThat(pid).isNotNull();

        // 第一次查：穿透到 DB，回写 Redis
        var detail1 = problemService.getProblemDetail(pid);
        assertThat(detail1.getTitle()).isEqualTo("Test Integration");

        // 第二次查：命中 Redis
        var detail2 = problemService.getProblemDetail(pid);
        assertThat(detail2.getTitle()).isEqualTo("Test Integration");
    }
}
