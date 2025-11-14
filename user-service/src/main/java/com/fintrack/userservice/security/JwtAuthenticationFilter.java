/* 
 * A security checkpoint that:
 * 1. Intercepts every incoming request
 * 2. Extracts JWT token from the Authorization header
 * 3. Validates the token
 * 4. If valid -> tells Spring Security "this user is authenticated"
 * 5. If invalid -> request continues but user is NOT authenticated (will be blocked by protected endpoints)
 */

package com.fintrack.userservice.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, CustomUserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        // This method runs for EVERY request

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        // Check if auth header is valid
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        jwt = authHeader.substring(7);
        userEmail = jwtUtil.extractUsername(jwt);

        // if statement check if the user is already authenticated or not
        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // Load user data from database
            UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);

            // Validate JWT token
            if (jwtUtil.validateToken(jwt, userDetails)) {
                // Spring Security's way of representing "this user is authenticated"
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request)); // Add request details
                                                                                                  // (IP address of the
                                                                                                  // requester, session
                                                                                                  // ID, etc)
                SecurityContextHolder.getContext().setAuthentication(authToken); // Set authentication in
                                                                                 // SecurityContext - The user is now
                                                                                 // authenticated
            }
        }
        filterChain.doFilter(request, response);
    }
}
