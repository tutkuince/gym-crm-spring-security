package com.epam.gymcrm.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Objects;

@Component
public class BlacklistFilter extends OncePerRequestFilter {

    private final TokenBlacklist blacklist;
    private final JwtDecoder decoder;

    public BlacklistFilter(TokenBlacklist blacklist, JwtDecoder decoder) {
        this.blacklist = blacklist;
        this.decoder = decoder;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {

        String auth = req.getHeader("Authorization");
        if (Objects.nonNull(auth) && auth.startsWith("Bearer ")) {
            String token = auth.substring(7);
            try {
                Jwt jwt = decoder.decode(token); // imza/süre vs. valid değilse exception atar
                String jti = jwt.getId();        // "jti" claim
                if (Objects.nonNull(jti) && blacklist.isInvalid(jti)) {
                    res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    res.getWriter().write("{\"error\":\"TOKEN_INVALIDATED\"}");
                    return;
                }
            } catch (JwtException e) {
                // Decoder zaten Resource Server filtresi içinde de yakalanır; zincire bırak
            }
        }
        chain.doFilter(req, res);
    }
}
