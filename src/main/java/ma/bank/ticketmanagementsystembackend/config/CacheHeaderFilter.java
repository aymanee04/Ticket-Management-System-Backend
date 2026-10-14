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
@Order(Ordered.HIGHEST_PRECEDENCE)
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

        // Check cache BEFORE processing the request
        String cacheKey = resolveCacheKey(request.getRequestURI());
        boolean isCacheHit = false;
        long ttl = 0;

        if (request.getMethod().equals("GET") && cacheKey != null) {
            try {
                isCacheHit = Boolean.TRUE.equals(redisTemplate.hasKey(cacheKey));
                if (isCacheHit) {
                    Long ttlValue = redisTemplate.getExpire(cacheKey, TimeUnit.SECONDS);
                    ttl = (ttlValue != null && ttlValue > 0) ? ttlValue : 0;
                }
            } catch (Exception e) {
                System.err.println("Cache check error: " + e.getMessage());
            }
        }

        // Add headers BEFORE the response is committed
        if (request.getMethod().equals("GET") && cacheKey != null) {
            response.setHeader("X-Cache", isCacheHit ? "HIT" : "MISS");
            response.setHeader("X-Cache-Source", isCacheHit ? "REDIS" : "DATABASE");
            if (isCacheHit && ttl > 0) {
                response.setHeader("X-Cache-TTL", ttl + "s remaining");
            }
        }

        long start = System.currentTimeMillis();
        chain.doFilter(request, response);
        long elapsed = System.currentTimeMillis() - start;

        response.setHeader("X-Response-Time", elapsed + "ms");
        System.out.println("Cache check for key: " + cacheKey + " → " + (isCacheHit ? "HIT" : "MISS"));
    }

    private String resolveCacheKey(String uri) {
        if (uri.matches("/api/users/\\d+"))
            return "users::" + uri.replaceAll("\\D+", "");
        if (uri.matches("/api/clients/\\d+"))
            return "clients::" + uri.replaceAll("\\D+", "");
        if (uri.matches("/api/tickets/\\d+"))
            return "tickets::" + uri.replaceAll("\\D+", "");
        if (uri.equals("/api/clients"))
            return "clients::all";
        return null;
    }
}