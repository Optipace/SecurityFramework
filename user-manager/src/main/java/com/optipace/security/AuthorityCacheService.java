package com.optipace.security;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.optipace.entity.Permission;
import com.optipace.entity.Role;
import com.optipace.entity.User;
import com.optipace.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AuthorityCacheService {


    private final UserRepository userRepository;

    private final Cache<Long, Set<GrantedAuthority>> authorityCache =
            Caffeine.newBuilder()
                    .expireAfterWrite(30, TimeUnit.MINUTES)
                    .maximumSize(10000)
                    .build();




    public Set<GrantedAuthority> getAuthorities(Long userId) {
        return authorityCache.get(userId, this::loadAuthorities);
    }

    private Set<GrantedAuthority> loadAuthorities(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        Set<GrantedAuthority> authorities = new HashSet<>();

        Role role = user.getRole();

        if (role != null) {

            authorities.add(
                    new SimpleGrantedAuthority(
                            "ROLE_" + role.getName()
                    )
            );

            if (role.getPermissions() != null) {

                for (Permission permission : role.getPermissions()) {

                    authorities.add(
                            new SimpleGrantedAuthority(
                                    permission.getName()
                            )
                    );
                }
            }
        }

        return authorities;
    }



}
