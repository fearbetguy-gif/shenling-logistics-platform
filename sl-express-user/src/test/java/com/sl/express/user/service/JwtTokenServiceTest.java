package com.sl.express.user.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.sl.express.user.config.JwtProperties;
import org.junit.jupiter.api.Test;

class JwtTokenServiceTest {

    @Test
    void shouldCreateAndParseToken() {
        JwtProperties properties = new JwtProperties();
        properties.setSecret("test-secret-key-with-at-least-32-characters");
        properties.setExpireSeconds(3600);

        JwtTokenService service = new JwtTokenService(properties);
        String token = service.createToken(1001L);

        assertEquals(1001L, service.parseUserId(token));
    }
}
