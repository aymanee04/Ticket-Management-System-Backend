package ma.bank.ticketmanagementsystembackend.security.filters;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ma.bank.ticketmanagementsystembackend.entities.AppUser;
import ma.bank.ticketmanagementsystembackend.repositories.UserRepository;
import ma.bank.ticketmanagementsystembackend.security.JwtUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private AuthenticationManager authenticationManager;
    private UserRepository userRepository;

    public JwtAuthenticationFilter(AuthenticationManager authenticationManager, UserRepository userRepository) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {
        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, String> credentials = mapper.readValue(request.getInputStream(), Map.class);

            String email = credentials.get("email");
            String password = credentials.get("password");

            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(email, password);

            return authenticationManager.authenticate(authToken);

        } catch (IOException e) {
            throw new RuntimeException("Failed to parse authentication request", e);
        }
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                            FilterChain chain, Authentication authResult)
            throws IOException, ServletException {
        User springUser = (User) authResult.getPrincipal();
        Algorithm algorithm = Algorithm.HMAC256(JwtUtils.SECRET);

        AppUser appUser = userRepository.findByEmailWithClient(springUser.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Long clientId = null;
        String clientName = null;

        if (appUser.getClient() != null) {
            clientId = appUser.getClient().getClientId();
            clientName = appUser.getClient().getName();
        }

        String jwtAccessToken = JWT.create()
                .withSubject(appUser.getEmail())
                .withExpiresAt(new Date(System.currentTimeMillis() + JwtUtils.EXPIRE_ACCESS_TOKEN))
                .withIssuer(request.getRequestURL().toString())
                .withClaim("userId", appUser.getUserId())
                .withClaim("name", appUser.getName())
                .withClaim("email", appUser.getEmail())
                .withClaim("jobTitle", appUser.getJobTitle() != null ? appUser.getJobTitle() : "")
                .withClaim("roles", appUser.getRoles().stream()
                        .map(role -> role.name())
                        .collect(Collectors.toList()))
                .withClaim("clientId", clientId)
                .withClaim("clientName", clientName)
                .sign(algorithm);

        String jwtRefreshToken = JWT.create()
                .withSubject(appUser.getEmail())
                .withExpiresAt(new Date(System.currentTimeMillis() + JwtUtils.EXPIRE_REFRESH_TOKEN))
                .withIssuer(request.getRequestURL().toString())
                .sign(algorithm);

        Map<String, Object> tokens = new HashMap<>();
        tokens.put("access-token", jwtAccessToken);
        tokens.put("refresh-token", jwtRefreshToken);

        response.setContentType("application/json");
        new ObjectMapper().writeValue(response.getOutputStream(), tokens);
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                              AuthenticationException failed) throws IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", System.currentTimeMillis());

        // Check the exact exception type and set appropriate response
        if (failed.getCause() instanceof DisabledException || failed instanceof DisabledException) {
            // Account disabled (inactive/suspended client)
            response.setStatus(HttpServletResponse.SC_FORBIDDEN); // 403
            errorResponse.put("status", 403);
            errorResponse.put("error", "Forbidden");
            errorResponse.put("message", failed.getMessage()); // This contains the custom message
            errorResponse.put("type", "ACCOUNT_DISABLED");

        } else if (failed instanceof BadCredentialsException) {
            // Wrong password or email
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401
            errorResponse.put("status", 401);
            errorResponse.put("error", "Unauthorized");
            errorResponse.put("message", "Invalid email or password");
            errorResponse.put("type", "BAD_CREDENTIALS");

        } else if (failed instanceof UsernameNotFoundException) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401
            errorResponse.put("status", 401);
            errorResponse.put("error", "Unauthorized");
            errorResponse.put("message", "Invalid email or password");
            errorResponse.put("type", "BAD_CREDENTIALS");

        } else {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401
            errorResponse.put("status", 401);
            errorResponse.put("error", "Unauthorized");
            errorResponse.put("message", "Authentication failed: " + failed.getMessage());
            errorResponse.put("type", "AUTH_FAILED");
        }

        new ObjectMapper().writeValue(response.getOutputStream(), errorResponse);
    }
}