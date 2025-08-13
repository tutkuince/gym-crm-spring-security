package com.epam.gymcrm.infrastructure.security;

import com.epam.gymcrm.domain.exception.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Component
public class BlacklistFilter extends OncePerRequestFilter {

    private final TokenBlacklist blacklist;
    private final JwtDecoder decoder;
    private final ObjectMapper objectMapper;

    private final Logger logger = LoggerFactory.getLogger(BlacklistFilter.class);

    public BlacklistFilter(TokenBlacklist blacklist, JwtDecoder decoder, ObjectMapper objectMapper) {
        this.blacklist = blacklist;
        this.decoder = decoder;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req,
                                    HttpServletResponse res,
                                    FilterChain chain)
            throws ServletException, IOException {

        String auth = req.getHeader("Authorization");
        if (Objects.nonNull(auth) && auth.startsWith("Bearer ")) {
            String token = auth.substring(7);
            try {
                Jwt jwt = decoder.decode(token);
                String jti = jwt.getId();
                if (Objects.nonNull(jti) && blacklist.isInvalid(jti)) {
                    logger.warn("Invalidated token attempt detected: jti={}", jti);

                    res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    res.setContentType(MediaType.APPLICATION_JSON_VALUE);

                    ErrorResponse errorResponse = new ErrorResponse(
                            HttpServletResponse.SC_UNAUTHORIZED,
                            "Unauthorized",
                            "JWT token has been invalidated",
                            LocalDateTime.now()
                    );
                    errorResponse.setDetails(List.of("jti=" + jti));

                    res.getWriter().write(objectMapper.writeValueAsString(errorResponse));
                    return;
                }
            } catch (JwtException e) {
                logger.warn("JWT decoding failed: {}", e.getMessage());
            }
        }
        chain.doFilter(req, res);
    }
}
