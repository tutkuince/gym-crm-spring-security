package com.epam.gymcrm.infrastructure.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BlacklistFilterTest {

    @Mock
    private TokenBlacklist blacklist;
    @Mock
    private JwtDecoder jwtDecoder;
    @Mock
    private FilterChain chain;

    private BlacklistFilter filter;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        filter = new BlacklistFilter(blacklist, jwtDecoder, objectMapper);
    }

    private Jwt jwt(String token, String jti) {
        Instant now = Instant.now();

        Map<String, Object> headers = Map.of("alg", "HS256");

        Map<String, Object> claims = new java.util.HashMap<>();
        claims.put("sub", "test-user");
        if (Objects.nonNull(jti)) {
            claims.put("jti", jti);
        }

        return new Jwt(token, now, now.plusSeconds(600), headers, claims);
    }

    @Test
    void doFilter_shouldPassThrough_whenNoAuthorizationHeader() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        MockHttpServletResponse res = new MockHttpServletResponse();

        filter.doFilterInternal(req, res, chain);

        verifyNoInteractions(jwtDecoder, blacklist);
        verify(chain).doFilter(req, res);
        assertThat(res.getStatus()).isEqualTo(200); // default
    }

    @Test
    void doFilter_shouldPassThrough_whenAuthorizationIsNotBearer() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader("Authorization", "Basic abc");
        MockHttpServletResponse res = new MockHttpServletResponse();

        filter.doFilterInternal(req, res, chain);

        verifyNoInteractions(jwtDecoder, blacklist);
        verify(chain).doFilter(req, res);
    }

    @Test
    void doFilter_shouldPassThrough_whenTokenDecodedAndJtiNotBlacklisted() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader("Authorization", "Bearer TOK123");
        MockHttpServletResponse res = new MockHttpServletResponse();

        when(jwtDecoder.decode("TOK123")).thenReturn(jwt("TOK123", "jti-1"));
        when(blacklist.isInvalid("jti-1")).thenReturn(false);

        filter.doFilterInternal(req, res, chain);

        verify(jwtDecoder).decode("TOK123");
        verify(blacklist).isInvalid("jti-1");
        verify(chain).doFilter(req, res);
        assertThat(res.getStatus()).isEqualTo(200);
    }

    @Test
    void doFilter_shouldReturn401Json_whenTokenInvalidated() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader("Authorization", "Bearer TOK999");
        MockHttpServletResponse res = new MockHttpServletResponse();

        when(jwtDecoder.decode("TOK999")).thenReturn(jwt("TOK999", "dead-jti"));
        when(blacklist.isInvalid("dead-jti")).thenReturn(true);

        filter.doFilterInternal(req, res, chain);

        verify(jwtDecoder).decode("TOK999");
        verify(blacklist).isInvalid("dead-jti");
        verify(chain, never()).doFilter(any(), any());

        assertThat(res.getStatus()).isEqualTo(401);
        assertThat(res.getContentType()).isEqualTo(MediaType.APPLICATION_JSON_VALUE);

        String body = res.getContentAsString();
        assertThat(body).contains("\"status\":401");
        assertThat(body).contains("\"error\":\"Unauthorized\"");
        assertThat(body).contains("\"message\":\"JWT token has been invalidated\"");
        assertThat(body).contains("jti=dead-jti");
    }


    @Test
    void doFilter_shouldPassThrough_whenDecoderThrowsJwtException() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader("Authorization", "Bearer BROKEN");
        MockHttpServletResponse res = new MockHttpServletResponse();

        when(jwtDecoder.decode("BROKEN")).thenThrow(new JwtException("bad token"));

        filter.doFilterInternal(req, res, chain);

        verify(jwtDecoder).decode("BROKEN");
        verifyNoInteractions(blacklist);
        verify(chain).doFilter(req, res);
        assertThat(res.getStatus()).isEqualTo(200);
    }

    @Test
    void doFilter_shouldPassThrough_whenJtiIsNull() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader("Authorization", "Bearer TOK_NO_JTI");
        MockHttpServletResponse res = new MockHttpServletResponse();

        when(jwtDecoder.decode("TOK_NO_JTI")).thenReturn(jwt("TOK_NO_JTI", null));

        filter.doFilterInternal(req, res, chain);

        verify(jwtDecoder).decode("TOK_NO_JTI");
        verifyNoInteractions(blacklist);
        verify(chain).doFilter(req, res);
    }

}