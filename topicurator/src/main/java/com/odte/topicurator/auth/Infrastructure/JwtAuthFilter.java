package com.odte.topicurator.auth.Infrastructure;

import com.odte.topicurator.auth.Domain.CustomUserDetails;
import com.odte.topicurator.entity.User;
import com.odte.topicurator.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Objects;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final UserAccountRepository users;
    private final TokenBlacklist blacklist;
    private final UserRepository userRepository;

    public JwtAuthFilter(JwtProvider jwtProvider, UserAccountRepository users, TokenBlacklist blacklist, UserRepository userRepository) {
        this.jwtProvider = jwtProvider;
        this.users = users;
        this.blacklist = blacklist;
        this.userRepository = userRepository;
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
                    User user = userRepository.findById(u.getId()).orElseThrow();
                    CustomUserDetails customUserDetails = new CustomUserDetails(u, user);
                    var auth = new UsernamePasswordAuthenticationToken(
                            customUserDetails, null, customUserDetails.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(auth);
                });
            } catch (JwtException ignored) {}
        }
        chain.doFilter(req, res);
    }
}
