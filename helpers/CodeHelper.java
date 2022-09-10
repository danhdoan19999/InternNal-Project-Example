package com.nals.rw360.helpers;

import com.google.common.collect.ImmutableMap;
import com.nals.rw360.errors.ValidatorException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import tech.jhipster.config.JHipsterProperties;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.Year;
import java.util.Date;

import static com.nals.rw360.errors.ErrorCodes.INVALID_KEY;

@Slf4j
@Component
public final class CodeHelper {
    public static final String KEY = "key";
    public static final String NUMBER_OF_SEND_KEY = "numberOfSendKey";

    private final java.security.Key key;
    private final JwtParser jwtParser;

    public CodeHelper(final JHipsterProperties jHipsterProperties) {
        byte[] keyBytes;
        String secret = jHipsterProperties.getSecurity().getAuthentication().getJwt().getBase64Secret();

        if (StringHelper.isNotBlank(secret)) {
            log.debug("Using a Base64-encoded JWT secret key");
            keyBytes = Decoders.BASE64.decode(secret);
        } else {
            log.warn("Warning: the JWT key used is not Base64-encoded");
            secret = jHipsterProperties.getSecurity().getAuthentication().getJwt().getSecret();
            keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        }

        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.jwtParser = Jwts.parserBuilder().setSigningKey(key).build();
    }

    public static String generateCode(final Long id, final String postfix) {
        return StringUtils.leftPad(id.toString(), 2, "0") + "/" + Year.now() + postfix;
    }

    public String generateKey(final Key key) {
        JwtBuilder jwtBuilder = Jwts.builder()
                                    .setSubject(key.getEmail())
                                    .signWith(this.key, SignatureAlgorithm.HS512)
                                    .setIssuedAt(new Date(key.getSendDate().toEpochMilli()))
                                    .setExpiration(new Date(key.getExpiredDate().toEpochMilli()));

        jwtBuilder.addClaims(ImmutableMap.of(KEY, key.getKey(),
                                             NUMBER_OF_SEND_KEY, key.getNumberOfSendKey()));

        return jwtBuilder.compact();
    }

    public Key getKeyInfo(final String key) {
        log.info("Get key info for #{}}", key);
        Claims claims = jwtParser.parseClaimsJws(key).getBody();

        String keyVal = claims.get(KEY, String.class);
        Integer sendNumber = claims.get(NUMBER_OF_SEND_KEY, Integer.class);

        if (keyVal == null || sendNumber == null) {
            throw new ValidatorException("Invalid key", "", INVALID_KEY);
        }

        return Key.builder()
                  .email(claims.getSubject())
                  .key(keyVal)
                  .numberOfSendKey(sendNumber)
                  .sendDate(Instant.ofEpochMilli(claims.getIssuedAt().getTime()))
                  .expiredDate(Instant.ofEpochMilli(claims.getExpiration().getTime()))
                  .build();
    }

    @Getter
    @Setter
    @Builder
    public static class Key {
        private String email;
        private String key;
        private Integer numberOfSendKey;
        private Instant sendDate;
        private Instant expiredDate;
    }
}
