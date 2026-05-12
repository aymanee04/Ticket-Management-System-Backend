package ma.bank.ticketmanagementsystembackend.security.web;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import ma.bank.ticketmanagementsystembackend.entities.AppUser;
import ma.bank.ticketmanagementsystembackend.entities.ClientStatus;
import ma.bank.ticketmanagementsystembackend.security.JwtUtils;
import ma.bank.ticketmanagementsystembackend.services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @GetMapping("/refreshToken")
    public void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String authorisationToken = request.getHeader(JwtUtils.AUTH_HEADER);

        if (authorisationToken != null && authorisationToken.startsWith(JwtUtils.PREFIX)) {
            try {
                String jwt = authorisationToken.substring(JwtUtils.PREFIX.length());
                Algorithm algorithm = Algorithm.HMAC256(JwtUtils.SECRET);
                JWTVerifier jwtVerifier = JWT.require(algorithm).build();
                DecodedJWT decodedJWT = jwtVerifier.verify(jwt);
                String email = decodedJWT.getSubject();

                AppUser appUser = userService.loadUserByEmail(email);
                if (appUser == null) {
                    throw new UsernameNotFoundException("User not found with email: " + email);
                }
                if (appUser.getClient() != null) {
                    if (appUser.getClient().getStatus() == ClientStatus.INACTIVE) {
                        throw new DisabledException(
                                "Your company account is currently inactive. Please contact support."
                        );
                    }
                    if (appUser.getClient().getStatus() == ClientStatus.SUSPENDED) {
                        throw new DisabledException(
                                "Your company account has been suspended. Please contact support."
                        );
                    }
                }


                String jwtAccessToken = JWT.create()
                        .withSubject(appUser.getEmail())
                        .withExpiresAt(new Date(System.currentTimeMillis() + JwtUtils.EXPIRE_ACCESS_TOKEN))
                        .withIssuer(request.getRequestURL().toString())
                        .withClaim("email", appUser.getEmail())
                        .withClaim("userId", appUser.getUserId())
                        .withClaim("name", appUser.getName() != null ? appUser.getName() : "")
                        .withClaim("roles", appUser.getRoles().stream()
                                .map(Enum::name)
                                .collect(Collectors.toList()))
                        .withClaim("clientName", appUser.getClient() != null ? appUser.getClient().getName() : "")
                        .sign(algorithm);

                Map<String, String> tokens = new HashMap<>();
                tokens.put("access-token", jwtAccessToken);
                tokens.put("refresh-token", jwt);

                response.setContentType("application/json");
                new ObjectMapper().writeValue(response.getOutputStream(), tokens);
            } catch (Exception e) {
                response.setHeader("error-message", e.getMessage());
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
            }
        } else {
            throw new RuntimeException("Refresh token is missing");
        }
    }

}