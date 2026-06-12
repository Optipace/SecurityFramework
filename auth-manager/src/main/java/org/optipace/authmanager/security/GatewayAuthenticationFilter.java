package org.optipace.authmanager.security;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.optipace.authmanager.DTO.ResponseDTO.AuthPrincipal;
import org.optipace.authmanager.Entity.User;
import org.optipace.authmanager.repository.UserRepository;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class GatewayAuthenticationFilter extends OncePerRequestFilter {

    private final UserRepository userRepository;
    private  final AuthorityCacheService authorityCacheService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String userId = request.getHeader("X-User-Id");

            try {

                User user = userRepository.findById(Long.parseLong(userId)).orElse(null);
         if(userId==null){
             Set<GrantedAuthority> grantedAuthorities = authorityCacheService.getAuthority(user.getId());
             AuthPrincipal principal =
                        new AuthPrincipal(
                             user.getId(),
                             user.getUsername(),
                             user.getRole() != null ?  user.getRole().getName() : null
                     );
             UsernamePasswordAuthenticationToken authentication =
                     new UsernamePasswordAuthenticationToken(
                             principal,
                             null,
                             grantedAuthorities
                     );
             SecurityContextHolder.getContext().setAuthentication(authentication);
         }
            } catch (NumberFormatException numberFormatException) {
                log.warn("Invalid X-User-Id header value: {}", userId);

            }


        filterChain.doFilter(request, response);
    }
}