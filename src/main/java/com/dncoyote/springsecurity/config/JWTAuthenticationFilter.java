package com.dncoyote.springsecurity.config;

import java.io.IOException;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.dncoyote.springsecurity.service.UserService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JWTAuthenticationFilter extends OncePerRequestFilter {

    private final JWtService jwtService;
    private final UserService userService;
    private final TokenRepository tokenRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;
        if (StringUtils.isEmpty(authHeader) || !StringUtils.startsWith(authHeader,"Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        jwt = authHeader.substring(7);
        userEmail = jwtService.extractUsername(jwt);
        if (StringUtils.isNotEmpty(userEmail) && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userService.userDetailsService.loadUserByUsername(userEmail);
            // var isTokenValid = tokenRepository.findByToken(jwt)
            //         .map(t -> !t.isExpired() && !t.isRevoked())
            //         .orElse(false);
            // if (jwtService.isTokenValid(jwt, userDetails) && isTokenValid)
            if (jwtService.isTokenValid(jwt, userDetails)) {
                // UsernamePasswordAuthenticationToken authToken = new
                // UsernamePasswordAuthenticationToken(userDetails,
                // null, userDetails.getAuthorities());

                SecurityContext securityContext = SecurityContextHolder.createEmptyContext();

                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities());
                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request));
                        securityContext.setAuthentication(authToken);
                        SecurityContextHolder.setContext(securityContext);
            }
        }
        filterChain.doFilter(request, response);
    }

}
