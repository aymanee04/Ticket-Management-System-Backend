package ma.bank.ticketmanagementsystembackend.security.filters;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import ma.bank.ticketmanagementsystembackend.controllers.AttachmentController;
import ma.bank.ticketmanagementsystembackend.exceptions.ErrorResponse;
import ma.bank.ticketmanagementsystembackend.security.JwtUtils;
import ma.bank.ticketmanagementsystembackend.services.TokenBlacklistService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class JwtAuthorisationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthorisationFilter.class);
    private final ApplicationContext applicationContext;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // Skip entirely for auth endpoints
        if (request.getServletPath().equals("/auth/login") ||
                request.getServletPath().equals("/auth/refreshToken") ||
                request.getServletPath().equals("/auth/logout")) {
            filterChain.doFilter(request, response);
            return;
        }

        String authorizationHeader = request.getHeader(JwtUtils.AUTH_HEADER);

        // No token at all — just continue, Spring Security handles the 401
        if (authorizationHeader == null || !authorizationHeader.startsWith(JwtUtils.PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String token = authorizationHeader.substring(JwtUtils.PREFIX.length());

            // ── Blacklist check FIRST — before decoding ──────────────────
            TokenBlacklistService blacklist =
                    applicationContext.getBean(TokenBlacklistService.class);
            if (blacklist.isBlacklisted(token)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write("""
                {"status":401,"error":"Unauthorized",
                 "message":"Token has been revoked. Please login again.",
                 "type":"TOKEN_REVOKED"}
            """);
                return;
            }
            // ─────────────────────────────────────────────────────────────

            Algorithm algorithm = Algorithm.HMAC256(JwtUtils.SECRET);
            JWTVerifier verifier = JWT.require(algorithm).build();
            DecodedJWT decodedJWT = verifier.verify(token);

            String username = decodedJWT.getSubject();
            List<String> roles = decodedJWT.getClaim("roles").asList(String.class);

            Collection<GrantedAuthority> authorities = new ArrayList<>();
            if (roles != null) {
                for (String role : roles) {
                    authorities.add(new SimpleGrantedAuthority(role));
                }
            }

            SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken(username, null, authorities));

        } catch (Exception e) {
            log.warn("JWT validation failed: {}", e.getMessage());

            try {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");

                // ── Second bug fix — use a plain Map instead of ErrorResponse ──
                // ErrorResponse uses LocalDateTime which Jackson can't serialize
                // without the JSR310 module, causing the secondary error you saw
                Map<String, Object> errorBody = new java.util.HashMap<>();
                errorBody.put("status", 403);
                errorBody.put("error", "Forbidden");
                errorBody.put("message", "Invalid or expired token");
                errorBody.put("type", "JWT_VALIDATION_FAILED");
                errorBody.put("timestamp", System.currentTimeMillis());

                new com.fasterxml.jackson.databind.ObjectMapper()
                        .writeValue(response.getWriter(), errorBody);
            } catch (IOException ioException) {
                log.error("Failed to write error response: {}", ioException.getMessage());
            }
            return;
        }

        filterChain.doFilter(request, response);
    }
}
