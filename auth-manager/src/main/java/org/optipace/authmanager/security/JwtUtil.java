package org.optipace.authmanager.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.optipace.authmanager.Entity.RefreshToken;
import org.optipace.authmanager.Entity.User;
import org.optipace.authmanager.repository.RefreshTokenRepository;
import org.optipace.authmanager.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.time.LocalDateTime;
import java.time.chrono.ChronoLocalDateTime;
import java.util.*;


@Component
@RequiredArgsConstructor
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    @Value("${jwt.refresh.expiration}")
    private long refreshExpiration;

    private final UserRepository userRepository;

private final RefreshTokenRepository refreshTokenRepository;

    public String generateToken(Long userId, String username, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("username", username);
        claims.put("role", role);
        String jwtId = UUID.randomUUID().toString();
        return Jwts.builder()
                .setClaims(claims)
                .setId(jwtId)
                .setSubject(userId.toString())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSignKey(), SignatureAlgorithm.HS256)
                .compact();
    }



    public  String generateRefreshToken(Long userId){
       User user = userRepository.findById(userId).orElseThrow(()->new RuntimeException("User not found"));


        RefreshToken token = new RefreshToken();
        token.setRefreshToken(UUID.randomUUID().toString());
        token.setCreatedDatetime(LocalDateTime.now());
        token.setUser(user);
        token.setExpireDatetime(LocalDateTime.now().plusSeconds(refreshExpiration));
        refreshTokenRepository.save(token);
        return token.getRefreshToken();

    }
    public Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
