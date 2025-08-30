package com.odte.topicurator.auth.application;

import com.odte.topicurator.auth.controller.dto.*;
import com.odte.topicurator.auth.Domain.UserAccount;
import com.odte.topicurator.auth.exception.exceptionhandler.ApiException;
import com.odte.topicurator.auth.exception.AuthErrorCode;
import com.odte.topicurator.auth.Infrastructure.JwtProvider;
import com.odte.topicurator.auth.Infrastructure.TokenBlacklist;
import com.odte.topicurator.auth.Infrastructure.UserAccountRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Service
public class AuthService {

    private final UserAccountRepository users;
    private final PasswordEncoder encoder;
    private final JwtProvider jwt;
    private final TokenBlacklist blacklist;

    public AuthService(UserAccountRepository users, PasswordEncoder encoder,
                       JwtProvider jwt, TokenBlacklist blacklist) {
        this.users = users; this.encoder = encoder; this.jwt = jwt; this.blacklist = blacklist;
    }

    @Transactional
    public Long register(RegisterReq r) {
        users.findByEmail(r.email()).ifPresent(u -> { throw new ApiException(AuthErrorCode.EMAIL_DUPLICATE); });
        users.findByUsername(r.username()).ifPresent(u -> { throw new ApiException(AuthErrorCode.USERNAME_DUPLICATE); });

        UserAccount u = new UserAccount();
        u.setEmail(r.email());
        u.setUsername(r.username());
        u.setPassword(encoder.encode(r.password()));
        u.setSex(r.sex());
        u.setBirthYear(r.birthYear());
        u.setJob(r.job());
        return users.save(u).getId();
    }

    public TokenRes login(LoginReq r) {
        var u = users.findByEmail(r.emailOrUsername())
                .or(() -> users.findByUsername(r.emailOrUsername()))
                .orElseThrow(() -> new BadCredentialsException("NOT_FOUND"));
        if (!encoder.matches(r.password(), u.getPassword()))
            throw new BadCredentialsException("BAD_CREDENTIALS");
        return new TokenRes(jwt.createAccess(u), jwt.createRefresh(u));
    }

    public TokenRes refresh(String refreshToken) {
        var jws = jwt.parse(refreshToken);
        if (!"refresh".equals(String.valueOf(jws.getBody().get("type"))))
            throw new ApiException(AuthErrorCode.INVALID_REFRESH_TOKEN);
        Long uid = Long.valueOf(jws.getBody().getSubject());
        var u = users.findById(uid).orElseThrow(() -> new ApiException(AuthErrorCode.USER_NOT_FOUND));
        return new TokenRes(jwt.createAccess(u), jwt.createRefresh(u)); // 회전
    }

    public void logout(String accessToken, String refreshToken) {
        try {
            var a = jwt.parse(accessToken);
            var ttl = Duration.between(Instant.now(), a.getBody().getExpiration().toInstant());
            blacklist.blacklist(String.valueOf(a.getBody().getId() == null ? accessToken : a.getBody().getId()), ttl);
        } catch (Exception ignored) {}
        if (refreshToken != null) {
            try {
                var r = jwt.parse(refreshToken);
                var ttl = Duration.between(Instant.now(), r.getBody().getExpiration().toInstant());
                blacklist.blacklist(String.valueOf(r.getBody().getId() == null ? refreshToken : r.getBody().getId()), ttl);
            } catch (Exception ignored) {}
        }
    }

    public MeRes me(Long uid) {
        var u = users.findById(uid).orElseThrow(() -> new ApiException(AuthErrorCode.USER_NOT_FOUND));
        return new MeRes(u.getId(), u.getEmail(), u.getUsername(),
                u.getSex(), u.getBirthYear(), u.getJob(), u.getCreatedAt());
    }

    @Transactional
    public void updateMe(Long uid, UpdateMeReq r) {
        var u = users.findById(uid).orElseThrow(() -> new ApiException(AuthErrorCode.USER_NOT_FOUND));
        if (r.username()!=null) u.setUsername(r.username());
        if (r.sex()!=null) u.setSex(r.sex());
        if (r.birthYear()!=null) u.setBirthYear(r.birthYear());
        if (r.job()!=null) u.setJob(r.job());
    }
}
