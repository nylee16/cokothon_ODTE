package com.odte.topicurator.auth.Infrastructure;

import com.odte.topicurator.auth.Domain.UserAccount;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.sql.Date;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Component
public class JwtProvider {

    @Value("${jwt.secret}") private String secret;
    @Value("${jwt.access-ttl-min:15}")  private long accessTtlMin;
    @Value("${jwt.refresh-ttl-days:14}") private long refreshTtlDays;

    private Key key(){
        byte[] keyBytes;
        try{
            keyBytes = Decoders.BASE64.decode(secret);
        } catch (IllegalArgumentException e){
            keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String createAccess(UserAccount u){
        Instant now = Instant.now();
        return Jwts.builder()
                .setSubject(u.getId().toString())
                .claim("username", u.getUsername())
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plus(accessTtlMin, ChronoUnit.MINUTES)))
                .signWith(key(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String createRefresh(UserAccount u){
        Instant now = Instant.now();
        return Jwts.builder()
                .setSubject(u.getId().toString())
                .claim("type", "refresh")
                .setId(UUID.randomUUID().toString())
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plus(refreshTtlDays, ChronoUnit.DAYS)))
                .signWith(key(), SignatureAlgorithm.HS256)
                .compact();
    }

    public Jws<Claims> parse(String token){
        return Jwts.parserBuilder().setSigningKey(key()).build().parseClaimsJws(token);
    }
}
