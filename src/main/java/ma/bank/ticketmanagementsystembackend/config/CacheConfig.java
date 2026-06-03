package ma.bank.ticketmanagementsystembackend.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.cache.autoconfigure.RedisCacheManagerBuilderCustomizer;
import org.springframework.cache.Cache;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import jakarta.annotation.Nullable;

import java.time.Duration;

@Configuration
@EnableCaching
public class CacheConfig implements CachingConfigurer {
    private static final Logger log = LoggerFactory.getLogger(CacheConfig.class);

    @Bean
    public RedisCacheManagerBuilderCustomizer cacheCustomizer() {
        return builder -> builder
                .withCacheConfiguration("clients",
                        RedisCacheConfiguration.defaultCacheConfig()
                                .entryTtl(Duration.ofMinutes(30))
                                .disableCachingNullValues())
                .withCacheConfiguration("users",
                        RedisCacheConfiguration.defaultCacheConfig()
                                .entryTtl(Duration.ofMinutes(15))
                                .disableCachingNullValues())
                .withCacheConfiguration("tickets",
                        RedisCacheConfiguration.defaultCacheConfig()
                                .entryTtl(Duration.ofMinutes(5))
                                .disableCachingNullValues());
    }

    @Override
    public CacheErrorHandler errorHandler() {
        return new CacheErrorHandler() {
            @Override public void handleCacheGetError(RuntimeException e, @Nullable Cache c, @Nullable Object k) {
                log.warn("Redis GET failed for cache: {} key: {}, falling back to DB: {}",
                        c != null ? c.getName() : "unknown", k, e.getMessage());
            }
            @Override public void handleCachePutError(RuntimeException e, @Nullable Cache c, @Nullable Object k, @Nullable Object v) {
                log.warn("Redis PUT failed for cache: {} key: {}: {}",
                        c != null ? c.getName() : "unknown", k, e.getMessage());
            }
            @Override public void handleCacheEvictError(RuntimeException e, @Nullable Cache c, @Nullable Object k) {
                log.warn("Redis EVICT failed for cache: {} key: {}: {}",
                        c != null ? c.getName() : "unknown", k, e.getMessage());
            }
            @Override public void handleCacheClearError(RuntimeException e, @Nullable Cache c) {
                log.warn("Redis CLEAR failed for cache: {}: {}",
                        c != null ? c.getName() : "unknown", e.getMessage());
            }
        };
    }
}