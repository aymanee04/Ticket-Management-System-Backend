package ma.bank.ticketmanagementsystembackend.security.filters;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ma.bank.ticketmanagementsystembackend.controllers.AttachmentController;
import ma.bank.ticketmanagementsystembackend.exceptions.ErrorResponse;
import ma.bank.ticketmanagementsystembackend.security.JwtUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class JwtAuthorisationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthorisationFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if (request.getServletPath().equals("/auth/login") ||
                request.getServletPath().equals("/auth/refreshToken")) {
            filterChain.doFilter(request, response);
            return;
        }

        String authorizationHeader = request.getHeader(JwtUtils.AUTH_HEADER);

        if (authorizationHeader != null && authorizationHeader.startsWith(JwtUtils.PREFIX)) {
            try {
                String token = authorizationHeader.substring(JwtUtils.PREFIX.length());
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

                UsernamePasswordAuthenticationToken authenticationToken =
                        new UsernamePasswordAuthenticationToken(username, null, authorities);

                SecurityContextHolder.getContext().setAuthentication(authenticationToken);

            } catch (Exception e) {
                System.err.println("JWT Error: " + e.getMessage());
                e.printStackTrace();
                log.warn("JWT validation failed: {}", e.getMessage());

                try {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.setContentType("application/json");
                    response.setCharacterEncoding("UTF-8");

                    ErrorResponse errorResponse = ErrorResponse.of(
                            403,
                            "Forbidden",
                            "Invalid or expired token",
                            "JWT_VALIDATION_FAILED"
                    );

                    String jsonResponse = new com.fasterxml.jackson.databind.ObjectMapper()
                            .writeValueAsString(errorResponse);

                    response.getWriter().write(jsonResponse);
                } catch (IOException ioException) {
                    log.error("Failed to write error response: {}", ioException.getMessage());
                }
                return;
            }

        }

        filterChain.doFilter(request, response);
    }
}
