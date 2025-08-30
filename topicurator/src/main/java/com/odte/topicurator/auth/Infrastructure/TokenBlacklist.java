package com.odte.topicurator.auth.Infrastructure;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class TokenBlacklist {
    private final Cache<String, Boolean> cache = Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofDays(14))
            .maximumSize(200_000)
            .build();

    public void blacklist(String idOrToken, Duration tt1){
        cache.put(idOrToken, Boolean.TRUE);
    }

    public boolean isBlacklisted(String idOrToken){
        return cache.getIfPresent(idOrToken) != null;
    }
}
