package com.jira.jiraclone.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter
        extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    private final CustomUserDetailsService customUserDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader =
                request.getHeader("Authorization");

        final String jwt;
        final String userEmail;

        // CHECK HEADER
        if (authHeader == null ||
                !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // EXTRACT TOKEN
        jwt = authHeader.substring(7);

        // EXTRACT EMAIL
        userEmail = jwtUtil.extractEmail(jwt);

        // AUTHENTICATE USER
        if (userEmail != null &&
                SecurityContextHolder
                        .getContext()
                        .getAuthentication() == null) {

            UserDetails userDetails;

            // FIX: catch UsernameNotFoundException so it doesn't
            // bubble up and cause a 401 crash in the filter
            try {
                userDetails =
                        customUserDetailsService
                                .loadUserByUsername(userEmail);
            } catch (UsernameNotFoundException e) {
                // User not found — skip auth, continue filter chain
                filterChain.doFilter(request, response);
                return;
            }

            // VALIDATE TOKEN
            if (jwtUtil.validateToken(jwt)) {

                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                authToken.setDetails(
                        new WebAuthenticationDetailsSource()
                                .buildDetails(request)
                );

                SecurityContextHolder
                        .getContext()
                        .setAuthentication(authToken);
            }
        }

        filterChain.doFilter(request, response);
    }
}