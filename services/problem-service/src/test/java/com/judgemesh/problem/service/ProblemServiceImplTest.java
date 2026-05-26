package com.judgemesh.problem.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.judgemesh.api.dto.ProblemDTO;
import com.judgemesh.problem.mapper.ProblemMapper;
import com.judgemesh.problem.mapper.ProblemTagMapper;
import com.judgemesh.problem.service.impl.ProblemServiceImpl;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProblemServiceImplTest {

    @Mock private StringRedisTemplate stringRedisTemplate;
    @Mock private ValueOperations<String, String> valueOperations;
    @Mock private ProblemMapper problemMapper;
    @Mock private ProblemTagMapper problemTagMapper;
    @Mock private ObjectMapper objectMapper;
    @Mock private RedissonClient redissonClient;
    @Mock private MeterRegistry meterRegistry;
    @Mock private Counter mockCounter;

    @InjectMocks
    private ProblemServiceImpl problemService;

    @BeforeEach
    void setUp() {
        // Mock 掉 Micrometer 埋点，防止报空指针
        when(meterRegistry.counter(anyString())).thenReturn(mockCounter);
        when(meterRegistry.counter(anyString(), anyString(), anyString())).thenReturn(mockCounter);
        problemService.initMetrics();
    }

    @Test
    void getProblemDetail_CacheHit_ReturnsDto() throws Exception {
        // 1. 模拟 Redis 缓存命中
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("cache:problem:1")).thenReturn("{\"id\":1, \"title\":\"Test\"}");
        when(objectMapper.readValue(anyString(), eq(ProblemDTO.class))).thenReturn(new ProblemDTO());

        // 2. 执行查询
        ProblemDTO result = problemService.getProblemDetail(1L);

        // 3. 验证：结果不为空，并且绝对没有去查 MySQL
        assertNotNull(result);
        verify(problemMapper, never()).selectById(anyLong());
        verify(mockCounter, atLeastOnce()).increment(); // 验证缓存命中计数器 +1
    }

    @Test
    void getProblemDetail_PenetrationDefense_ReturnsNull() {
        // 1. 模拟 Redis 查到 "{}"，即防穿透标记
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("cache:problem:999")).thenReturn("{}");

        // 2. 执行查询
        ProblemDTO result = problemService.getProblemDetail(999L);

        // 3. 验证：直接返回 null，绝对没有去查 MySQL
        assertNull(result);
        verify(problemMapper, never()).selectById(anyLong());
    }
}
