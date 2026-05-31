package com.judgemesh.gateway.security;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class GatewayRateLimitFilter implements GlobalFilter, Ordered {
    private final Cache<String, WindowCounter> counters;
    private final int submitLimitPerMinute;
    private final Counter blockedCounter;

    public GatewayRateLimitFilter(
            MeterRegistry registry,
            @Value("${judgemesh.gateway.submit-limit-per-minute:30}") int submitLimitPerMinute) {
        this.submitLimitPerMinute = Math.max(1, submitLimitPerMinute);
        this.counters = Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofMinutes(2))
                .maximumSize(10_000)
                .build();
        this.blockedCounter = Counter.builder("oj_gateway_rate_limit_total")
                .tag("route", "submit")
                .tag("result", "blocked")
                .register(registry);
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().pathWithinApplication().value();
        if (!isSubmitPath(path)) {
            return chain.filter(exchange);
        }

        String key = rateLimitKey(request);
        WindowCounter counter = counters.asMap().compute(key, (ignored, existing) -> {
            if (existing == null || existing.expired()) {
                return new WindowCounter();
            }
            existing.increment();
            return existing;
        });
        if (counter.count() > submitLimitPerMinute) {
            blockedCounter.increment();
            return tooManyRequests(exchange);
        }
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return -90;
    }

    private static boolean isSubmitPath(String path) {
        return "/api/submit".equals(path) || "/api/submits".equals(path);
    }

    private static String rateLimitKey(ServerHttpRequest request) {
        String userId = request.getHeaders().getFirst("X-User-Id");
        if (userId != null && !userId.isBlank()) {
            return "user:" + userId;
        }
        String forwardedFor = request.getHeaders().getFirst("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return "ip:" + forwardedFor.split(",")[0].trim();
        }
        return "remote:" + request.getRemoteAddress();
    }

    private static Mono<Void> tooManyRequests(ServerWebExchange exchange) {
        byte[] body = "{\"code\":\"1429\",\"message\":\"submit rate limit exceeded\"}"
                .getBytes(StandardCharsets.UTF_8);
        exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        return exchange.getResponse().writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap(body)));
    }

    private static final class WindowCounter {
        private final Instant windowStart = Instant.now();
        private final AtomicInteger count = new AtomicInteger(1);

        boolean expired() {
            return windowStart.plus(Duration.ofMinutes(1)).isBefore(Instant.now());
        }

        void increment() {
            count.incrementAndGet();
        }

        int count() {
            return count.get();
        }
    }
}
