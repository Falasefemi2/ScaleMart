package com.femi.orderservice.auth;



import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        // No token, just continue
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader.substring(7); // Remove "Bearer "
        Claims claims;

        try {
            claims = jwtService.extractAllClaims(jwt);
        } catch (Exception e) {
            // Invalid token — skip authentication
            filterChain.doFilter(request, response);
            return;
        }

        // Extract user identity and role from the token
        String userId = claims.getSubject(); // usually the user's ID
        String role = claims.get("role", String.class); // e.g., "SELLER", "ADMIN"

        // Convert role to Spring Security format
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + role.toUpperCase());

        // Create Spring Security Authentication token
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(userId, null, Collections.singletonList(authority));

        // Set the authentication into context
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);

        // Continue the filter chain
        filterChain.doFilter(request, response);
    }
}
