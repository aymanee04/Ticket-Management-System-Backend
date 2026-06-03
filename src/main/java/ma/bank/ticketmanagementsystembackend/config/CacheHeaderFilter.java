package ma.bank.ticketmanagementsystembackend.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class CacheHeaderFilter extends OncePerRequestFilter {

    private final RedisTemplate<String, String> redisTemplate;

    public CacheHeaderFilter(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        long start = System.currentTimeMillis();
        chain.doFilter(request, response);
        long elapsed = System.currentTimeMillis() - start;

        // Only tag GET requests — writes are never cached
        if (!request.getMethod().equals("GET")) return;

        String cacheKey = resolveCacheKey(request.getRequestURI());
        if (cacheKey == null) return;

        try {
            Boolean exists = redisTemplate.hasKey(cacheKey);
            boolean hit = Boolean.TRUE.equals(exists);

            response.setHeader("X-Cache", hit ? "HIT" : "MISS");
            response.setHeader("X-Cache-Source", hit ? "REDIS" : "DATABASE");
            response.setHeader("X-Response-Time", elapsed + "ms");

            if (hit) {
                Long ttl = redisTemplate.getExpire(cacheKey, TimeUnit.SECONDS);
                if (ttl != null && ttl > 0) {
                    response.setHeader("X-Cache-TTL", ttl + "s remaining");
                }
            }
        } catch (Exception e) {
            // Redis down — just skip the header, don't break the response
            response.setHeader("X-Cache", "UNAVAILABLE");
        }
    }

    // Maps a URL path to the Redis key Spring Cache would use
    private String resolveCacheKey(String uri) {
        // /api/clients      → clients::all
        if (uri.equals("/api/clients"))             return "clients::all";

        // /api/clients/1  → clients::1
        if (uri.matches("/api/clients/\\d+"))
            return "clients::" + uri.replaceAll("\\D+", "");

        // /api/users/3      → users::3
        if (uri.matches("/api/users/\\d+"))
            return "users::" + uri.replaceAll("\\D+", "");

        // /api/tickets/12   → tickets::12
        if (uri.matches("/api/tickets/\\d+"))
            return "tickets::" + uri.replaceAll("\\D+", "");

        return null; // endpoint not cached — skip
    }
}