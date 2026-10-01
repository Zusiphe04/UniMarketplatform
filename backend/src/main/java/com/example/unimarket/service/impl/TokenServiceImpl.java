package com.example.unimarket.service.impl;

import com.example.unimarket.domain.UserAccount;
import com.example.unimarket.domain.enums.Role;
import com.example.unimarket.response.AccessTokenResponse;
import com.example.unimarket.service.ITokenService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** Mints HS256 JWT access tokens for authenticated accounts. */
@Service
public class TokenServiceImpl implements ITokenService {

    private final JwtEncoder jwtEncoder;
    private final String issuer;
    private final String audience;
    private final Duration accessTokenTtl;

    public TokenServiceImpl(
            JwtEncoder jwtEncoder,
            @Value("${security.jwt.issuer}") String issuer,
            @Value("${security.jwt.audience}") String audience,
            @Value("${unimarket.security.access-token-ttl}") Duration accessTokenTtl) {
        this.jwtEncoder = jwtEncoder;
        this.issuer = issuer;
        this.audience = audience;
        this.accessTokenTtl = accessTokenTtl;
    }

    @Override
    public AccessTokenResponse issueAccessToken(UserAccount account, Set<Role> roles, String tokenFamilyId) {
        Instant now = Instant.now();
        long lifetimeSeconds = accessTokenTtl.getSeconds();
        List<String> roleNames = roles == null
                ? List.of()
                : roles.stream().map(Enum::name).toList();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .audience(List.of(audience))
                .subject(account.getId().toString())
                .id(UUID.randomUUID().toString())
                .issuedAt(now)
                .notBefore(now)
                .expiresAt(now.plusSeconds(lifetimeSeconds))
                .claim("sid", tokenFamilyId)
                .claim("roles", roleNames)
                .claim("email_verified", account.isEmailVerified())
                .build();

        JwsHeader header = JwsHeader.with(
                org.springframework.security.oauth2.jose.jws.MacAlgorithm.HS256).build();
        String token = jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
        return new AccessTokenResponse(token, lifetimeSeconds);
    }
}
