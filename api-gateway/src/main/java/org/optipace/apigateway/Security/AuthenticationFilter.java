package org.optipace.apigateway.Security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import lombok.RequiredArgsConstructor;
import org.optipace.apigateway.Security.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.security.SignatureException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class AuthenticationFilter implements GlobalFilter, Ordered {

	private final JwtUtil jwtUtil;
	private static final Logger log =LoggerFactory.getLogger(AuthenticationFilter.class);

	@Value("${gateway.open-endpoints}")
	private List<String> openEndpoints;

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

		String path = exchange.getRequest().getURI().getPath();
		System.out.println(" check path is open" + path);
		if (exchange.getRequest().getMethod() == HttpMethod.OPTIONS) {
		    return chain.filter(exchange);
		}
		if (isPublicEndpoint(path)) {
			return chain.filter(exchange);
		}

		String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

		if (authHeader == null || !authHeader.startsWith("Bearer ")) {
			exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
			return unauthorized(exchange,"Missing or invalid Authorization header");
			//return exchange.getResponse().setComplete();
		}

		String token = authHeader.substring(7);

		try {

			Claims claims = jwtUtil.getClaims(token);

			ServerHttpRequest request = exchange.getRequest().mutate()
					.headers(headers -> {

			            // Remove spoofable external headers
			            headers.remove(HttpHeaders.AUTHORIZATION);

			            headers.remove("X-User-Id");
			            headers.remove("X-User-Roles");
			            headers.remove("X-Authenticated-User");
			            headers.remove("X-Internal-Token");
			        })					
					.header("X-User-Id", claims.getSubject())
					.header("X-User-Roles", claims.get("role", String.class)).build();

			return chain.filter(exchange.mutate().request(request).build());

		} catch (ExpiredJwtException e) {
			log.warn("JWT token expired");
			return unauthorized(exchange, "JWT token expired");

		} catch (MalformedJwtException e) {
			log.warn("Malformed JWT token");
			return unauthorized(exchange, "Malformed JWT token");

		} catch (Exception e) {
			log.error("Authentication failed", e);
			return unauthorized(exchange, "Authentication failed");
		}
	}

	private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {

		exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
		exchange.getResponse().getHeaders().add(HttpHeaders.CONTENT_TYPE, "application/json");

		String body = """
				{
				"status": 401,
				"message": "%s"
				}
				""".formatted(message);

		DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));

		return exchange.getResponse().writeWith(Mono.just(buffer));
	}

	private final AntPathMatcher matcher = new AntPathMatcher();

	private boolean isPublicEndpoint(String path) {
		return openEndpoints.stream().anyMatch(pattern -> matcher.match(pattern, path));
	}

	@Override
	public int getOrder() {
		return -1;
	}
}