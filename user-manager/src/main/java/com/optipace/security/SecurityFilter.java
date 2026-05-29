package com.optipace.security;

import com.optipace.entity.Permission;
import com.optipace.entity.Role;
import com.optipace.entity.User;
import com.optipace.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class SecurityFilter extends OncePerRequestFilter {

    private final UserRepository userRepository;




    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String userId = request.getHeader("X-User-Id");

        if (userId != null) {

            User user = userRepository.findById(Long.parseLong(userId))
                    .orElse(null);

            if (user != null) {

                Set<GrantedAuthority> authorities = new HashSet<>();

                Role role = user.getRole();

                if (role != null) {

                    authorities.add(
                            new SimpleGrantedAuthority("ROLE_" + role.getName())
                    );

                    if (role.getPermissions() != null) {
                        for (Permission permission : role.getPermissions()) {

                            authorities.add(
                                    new SimpleGrantedAuthority(permission.getName())
                            );
                        }
                    }
                }

                AuthPrincipal principal =
                        new AuthPrincipal(
                                user.getId(),
                                user.getUsername(),
                                role != null ? role.getName() : null
                        );

                Authentication authentication =
                        new UsernamePasswordAuthenticationToken(
                                principal,
                                null,
                                authorities
                        );

                SecurityContextHolder.getContext()
                        .setAuthentication(authentication);
            }
        }

        filterChain.doFilter(request, response);
    }


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}