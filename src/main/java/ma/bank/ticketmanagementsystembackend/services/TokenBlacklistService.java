package ma.bank.ticketmanagementsystembackend.services;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private final RedisTemplate<String, String> redisTemplate;

    private static final String PREFIX = "blacklist::";

    // Called on logout — stores the token until it naturally expires
    public void blacklist(String token, long ttlMillis) {
        redisTemplate.opsForValue().set(
                PREFIX + token,
                "revoked",
                ttlMillis,
                TimeUnit.MILLISECONDS
        );
    }

    // Called on every request in JwtAuthorisationFilter
    public boolean isBlacklisted(String token) {
        return Boolean.TRUE.equals(
                redisTemplate.hasKey(PREFIX + token)
        );
    }
}
