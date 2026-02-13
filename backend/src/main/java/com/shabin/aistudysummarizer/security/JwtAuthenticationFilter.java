package com.shabin.aistudysummarizer.security;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

   @Override
protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain)
        throws ServletException, IOException {

    final String authHeader = request.getHeader("Authorization");
    
    log.debug("Processing request: {} {}", request.getMethod(), request.getRequestURI());
    log.debug("Authorization header: {}", authHeader != null ? "Present" : "Missing");

    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
        log.debug("No valid Authorization header, continuing filter chain");
        filterChain.doFilter(request, response);
        return;
    }

    try {

        String jwt = authHeader.substring(7);
        String email = jwtService.extractEmail(jwt);
        
        log.debug("Extracted email from JWT: {}", email);

        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            UserDetails userDetails = userDetailsService.loadUserByUsername(email);
            log.debug("Loaded user details for: {}", email);

            if (jwtService.isTokenValid(jwt, userDetails.getUsername())) {
                log.debug("JWT token is valid for user: {}", email);

                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities());

                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                SecurityContextHolder.getContext().setAuthentication(authToken);
                log.debug("Authentication set in SecurityContext for user: {}", email);
            } else {
                log.warn("JWT token is invalid for user: {}", email);
            }
        }

    } catch (Exception e) {
        log.error("Error processing JWT token: {}", e.getMessage(), e);
    }

    filterChain.doFilter(request, response);
}
}
