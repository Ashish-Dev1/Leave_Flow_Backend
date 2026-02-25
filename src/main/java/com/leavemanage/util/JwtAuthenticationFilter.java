package com.leavemanage.util;

import com.leavemanage.service.PasswordService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Date;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;
    private final PasswordService passwordService;

    public JwtAuthenticationFilter(
            JwtUtil jwtUtil,
            CustomUserDetailsService userDetailsService,
            @Lazy PasswordService passwordService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
        this.passwordService = passwordService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        // 1️⃣ Read Authorization header
        String authHeader = request.getHeader("Authorization");

        String token = null;
        String username = null;
        Date tokenIssuedAt = null;

        // 2️⃣ Check Bearer token
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7); // remove "Bearer "
            try {
                username = jwtUtil.extractUsername(token);
                tokenIssuedAt = jwtUtil.extractIssuedAt(token);
            } catch (Exception e) {
                // Invalid token → continue without authentication
                filterChain.doFilter(request, response);
                return;
            }
        }

        // 3️⃣ Authenticate user if not already authenticated
        if (username != null &&
                SecurityContextHolder.getContext().getAuthentication() == null) {

            UserDetails userDetails =
                    userDetailsService.loadUserByUsername(username);

            // 4️⃣ Validate token and check if it was issued after last password change
            if (jwtUtil.isTokenValid(token, userDetails) &&
                    passwordService.isTokenValidAfterPasswordChange(tokenIssuedAt, username)) {

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

                // 5️⃣ Set authentication
                SecurityContextHolder.getContext()
                        .setAuthentication(authToken);
            }
        }

        // 6️⃣ Continue filter chain
        filterChain.doFilter(request, response);
    }
}
