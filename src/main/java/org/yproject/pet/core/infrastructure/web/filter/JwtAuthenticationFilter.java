package org.yproject.pet.core.infrastructure.web.filter;

import io.micrometer.common.util.StringUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.yproject.pet.core.application.security.UserInfoService;
import org.yproject.pet.core.infrastructure.web.jwt.JwtService;

import java.io.IOException;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final UserInfoService userInfoService;

    private Optional<String> extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            final var token = bearerToken.substring(7);
            return Optional.of(token);
        }
        return Optional.empty();
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {
        final var tokenOptional = extractTokenFromRequest(request);
        if (tokenOptional.isPresent()) {
            final var jwtToken = tokenOptional.get();
            final var email = jwtService.extractEmail(jwtToken);
            if (StringUtils.isNotEmpty(email)
                    && SecurityContextHolder.getContext().getAuthentication() == null) {
                final var userInfo = userInfoService.loadUserByEmail(email);
                if (jwtService.isTokenValid(jwtToken, userInfo)) {
                    final var context = SecurityContextHolder.createEmptyContext();
                    final var authenticationToken = new UsernamePasswordAuthenticationToken(
                            userInfo,
                            null,
                            userInfo.getAuthorities());
                    authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    context.setAuthentication(authenticationToken);
                    SecurityContextHolder.setContext(context);
                }
            }
        }
        filterChain.doFilter(request, response);
    }
}
