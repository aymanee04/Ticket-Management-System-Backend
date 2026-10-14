package ma.bank.ticketmanagementsystembackend.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import jakarta.annotation.Nullable;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.time.Duration;

@Configuration
@EnableCaching
public class CacheConfig implements CachingConfigurer {

    private static final Logger log = LoggerFactory.getLogger(CacheConfig.class);

    @Bean
    public RedisSerializer<Object> jsonSerializer() {
        ObjectMapper mapper = JsonMapper.builder()
                .addModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .activateDefaultTyping(
                        LaissezFaireSubTypeValidator.instance,
                        ObjectMapper.DefaultTyping.NON_FINAL,
                        JsonTypeInfo.As.PROPERTY
                )
                .build();
        return new GenericJackson2JsonRedisSerializer(mapper);
    }

    @Bean
    public RedisCacheManagerBuilderCustomizer cacheCustomizer() {
        RedisCacheConfiguration base = RedisCacheConfiguration.defaultCacheConfig()
                .disableCachingNullValues()
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(jsonSerializer())
                );

        return builder -> builder
                .withCacheConfiguration("clients",
                        base.entryTtl(Duration.ofMinutes(30)))
                .withCacheConfiguration("users",
                        base.entryTtl(Duration.ofMinutes(15)))
                .withCacheConfiguration("tickets",
                        base.entryTtl(Duration.ofMinutes(5)));
    }

    @Override
    public CacheErrorHandler errorHandler() {
        return new CacheErrorHandler() {
            @Override
            public void handleCacheGetError(RuntimeException e, @Nullable Cache c, @Nullable Object k) {
                log.warn("Redis GET failed for cache: {} key: {}, falling back to DB: {}",
                        c != null ? c.getName() : "unknown", k, e.getMessage());
            }

            @Override
            public void handleCachePutError(RuntimeException e, @Nullable Cache c, @Nullable Object k, @Nullable Object v) {
                log.warn("Redis PUT failed for cache: {} key: {}: {}",
                        c != null ? c.getName() : "unknown", k, e.getMessage());
            }

            @Override
            public void handleCacheEvictError(RuntimeException e, @Nullable Cache c, @Nullable Object k) {
                log.warn("Redis EVICT failed for cache: {} key: {}: {}",
                        c != null ? c.getName() : "unknown", k, e.getMessage());
            }

            @Override
            public void handleCacheClearError(RuntimeException e, @Nullable Cache c) {
                log.warn("Redis CLEAR failed for cache: {}: {}",
                        c != null ? c.getName() : "unknown", e.getMessage());
            }
        };
    }
}