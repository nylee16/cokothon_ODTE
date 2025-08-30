// file: src/main/java/com/odte/topicurator/auth/Infrastructure/TokenBlacklist.java
package com.odte.topicurator.auth.Infrastructure;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class TokenBlacklist {
    private final Cache<String, Boolean> cache = Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofDays(14)) // 최대 TTL
            .maximumSize(200_000)
            .build();

    public void blacklist(String idOrToken, Duration ttl){
        cache.put(idOrToken, Boolean.TRUE);
    }

    public boolean isBlacklisted(String idOrToken){
        return cache.getIfPresent(idOrToken) != null;
    }
}
