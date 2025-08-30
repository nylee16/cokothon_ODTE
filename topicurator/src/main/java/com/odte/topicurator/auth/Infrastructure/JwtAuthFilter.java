package com.odte.topicurator.auth.Infrastructure;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final UserAccountRepository users;
    private final TokenBlacklist blacklist;

    public JwtAuthFilter(JwtProvider jwtProvider, UserAccountRepository users, TokenBlacklist blacklist) {
        this.jwtProvider = jwtProvider;
        this.users = users;
        this.blacklist = blacklist;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {

        String h = req.getHeader("Authorization");
        if (h != null && h.startsWith("Bearer ")) {
            String token = h.substring(7);
            try {
                Jws<Claims> jws = jwtProvider.parse(token);
                String idOrToken = Objects.toString(jws.getBody().getId(), token); // access는 토큰 문자열 fallback
                if (blacklist.isBlacklisted(idOrToken)) { res.setStatus(401); return; }

                Long uid = Long.valueOf(jws.getBody().getSubject());
                users.findById(uid).ifPresent(u -> {
                    var auth = new UsernamePasswordAuthenticationToken(
                            u.getId(), null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
                    SecurityContextHolder.getContext().setAuthentication(auth);
                });
            } catch (JwtException ignored) {}
        }
        chain.doFilter(req, res);
    }
}
