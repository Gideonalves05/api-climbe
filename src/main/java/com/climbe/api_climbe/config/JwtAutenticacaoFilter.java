package com.climbe.api_climbe.config;

import com.climbe.api_climbe.service.TokenJwtService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAutenticacaoFilter extends OncePerRequestFilter {

    private final TokenJwtService tokenJwtService;

    public JwtAutenticacaoFilter(TokenJwtService tokenJwtService) {
        this.tokenJwtService = tokenJwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(7);

        if (!tokenJwtService.tokenValido(token) || SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        Claims claims = tokenJwtService.extrairClaims(token);
        String subject = claims.getSubject();

        Object authoritiesObj = claims.get("authorities");
        List<SimpleGrantedAuthority> authorities = Collections.emptyList();
        if (authoritiesObj instanceof Collection<?>) {
            Collection<?> collection = (Collection<?>) authoritiesObj;
            authorities = collection.stream()
                    .map(String::valueOf)
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());
        }

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(subject, null, authorities);
        auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(auth);

        filterChain.doFilter(request, response);
    }
}
