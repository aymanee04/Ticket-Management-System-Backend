package ma.bank.ticketmanagementsystembackend.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtUtils {

    public static final String PREFIX = "Bearer ";
    public static String SECRET;
    public static long EXPIRE_ACCESS_TOKEN;
    public static long EXPIRE_REFRESH_TOKEN;
    public static String AUTH_HEADER;

    @Value("${jwt.secret}")
    public void setSecret(String secret) {
        JwtUtils.SECRET = secret;
    }

    @Value("${jwt.expire-access-token}")
    public void setExpireAccessToken(long value) {
        JwtUtils.EXPIRE_ACCESS_TOKEN = value;
    }

    @Value("${jwt.expire-refresh-token}")
    public void setExpireRefreshToken(long value) {
        JwtUtils.EXPIRE_REFRESH_TOKEN = value;
    }

    @Value("${jwt.auth-header}")
    public void setAuthHeader(String value) {
        JwtUtils.AUTH_HEADER = value;
    }
}
