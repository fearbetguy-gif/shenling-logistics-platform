package com.sl.express.gateway.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sl.express.common.result.Result;
import com.sl.express.common.result.ResultCode;
import com.sl.express.gateway.config.JwtProperties;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import javax.crypto.SecretKey;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/** Gateway 统一校验 JWT，避免每个业务服务重复实现认证逻辑。 */
@Component
public class JwtGlobalFilter implements GlobalFilter, Ordered {

    private static final String USER_ID_HEADER = "X-User-Id";
    private final JwtProperties properties;
    private final ObjectMapper objectMapper;

    public JwtGlobalFilter(JwtProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        if (isWhiteListed(path)) {
            return chain.filter(exchange);
        }

        String authorization = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return unauthorized(exchange, "请先登录");
        }

        try {
            String userId = Jwts.parser()
                    .verifyWith(signingKey())
                    .build()
                    .parseSignedClaims(authorization.substring(7))
                    .getPayload()
                    .getSubject();
            ServerHttpRequest request = exchange.getRequest().mutate()
                    .headers(headers -> headers.set(USER_ID_HEADER, userId))
                    .build();
            return chain.filter(exchange.mutate().request(request).build());
        } catch (JwtException | IllegalArgumentException exception) {
            return unauthorized(exchange, "登录凭证无效或已过期");
        }
    }

    private boolean isWhiteListed(String path) {
        return path.equals("/api/user/register")
                || path.equals("/api/user/login")
                || path.endsWith("/internal/ping")
                || path.equals("/actuator/health");
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        byte[] body;
        try {
            body = objectMapper.writeValueAsBytes(Result.failure(ResultCode.UNAUTHORIZED.getCode(), message));
        } catch (JsonProcessingException exception) {
            body = "{\"code\":401,\"message\":\"未登录或登录已过期\",\"data\":null}".getBytes(StandardCharsets.UTF_8);
        }
        return exchange.getResponse().writeWith(
                Mono.just(exchange.getResponse().bufferFactory().wrap(body)));
    }

    private SecretKey signingKey() {
        return Keys.hmacShaKeyFor(properties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public int getOrder() {
        return -100;
    }
}
