package org.optipace.authmanager.security;



import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.optipace.authmanager.DTO.ResponseDTO.AuthPrincipal;
import org.optipace.authmanager.Entity.Permission;
import org.optipace.authmanager.Entity.Role;
import org.optipace.authmanager.Entity.User;
import org.optipace.authmanager.repository.UserRepository;
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

        if (userId != null &&
                SecurityContextHolder.getContext().getAuthentication() == null) {

            try {

                User user = userRepository
                        .findById(Long.parseLong(userId))
                        .orElse(null);

                if (user != null) {

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

                    AuthPrincipal principal =
                            new AuthPrincipal(
                                    user.getId(),
                                    user.getUsername(),
                                    role != null ? role.getName() : null
                            );

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    principal,
                                    null,
                                    authorities
                            );

                    SecurityContextHolder.getContext()
                            .setAuthentication(authentication);
                }

            } catch (NumberFormatException ignored) {
            }
        }

        filterChain.doFilter(request, response);
    }
}