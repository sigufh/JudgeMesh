package com.judgemesh.gateway.security;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Set;

@Component
public class JwtGatewayFilter implements GlobalFilter, Ordered {
    private static final Set<String> PUBLIC_POSTS = Set.of(
            "/api/auth/login",
            "/api/auth/register");

    private final JwtVerifier jwtVerifier;

    public JwtGatewayFilter(JwtVerifier jwtVerifier) {
        this.jwtVerifier = jwtVerifier;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().pathWithinApplication().value();
        if (isPublic(request.getMethod(), path)) {
            return chain.filter(exchange);
        }

        String authorization = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return unauthorized(exchange, "missing bearer token");
        }
        try {
            JwtVerifier.Claims claims = jwtVerifier.verify(authorization.substring("Bearer ".length()));
            ServerHttpRequest mutated = request.mutate()
                    .header("X-User-Id", claims.userId().toString())
                    .header("X-User-Name", claims.username())
                    .header("X-User-Roles", String.join(",", claims.roles()))
                    .build();
            return chain.filter(exchange.mutate().request(mutated).build());
        } catch (IllegalArgumentException ex) {
            return unauthorized(exchange, ex.getMessage());
        }
    }

    @Override
    public int getOrder() {
        return -100;
    }

    private static boolean isPublic(HttpMethod method, String path) {
        if (path.startsWith("/actuator/") || path.equals("/actuator")) {
            return true;
        }
        if (method == HttpMethod.POST && PUBLIC_POSTS.contains(path)) {
            return true;
        }
        return method == HttpMethod.GET && (path.equals("/api/problem/list")
                || path.equals("/api/problems")
                || path.matches("/api/problem/\\d+")
                || path.matches("/api/problems/\\d+"));
    }

    private static Mono<Void> unauthorized(ServerWebExchange exchange, String detail) {
        byte[] body = ("{\"code\":\"1401\",\"message\":\"" + detail + "\"}")
                .getBytes(StandardCharsets.UTF_8);
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        return exchange.getResponse().writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap(body)));
    }
}
