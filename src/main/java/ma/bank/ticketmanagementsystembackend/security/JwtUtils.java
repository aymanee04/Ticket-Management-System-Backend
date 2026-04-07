package ma.bank.ticketmanagementsystembackend.security;

public class JwtUtils {
    public static final String SECRET = "secret04";
    public static final long EXPIRE_ACCESS_TOKEN = 60 * 60 * 1000;
    public static final long EXPIRE_REFRESH_TOKEN = 24 * 60 * 60 * 1000;
    public static final String AUTH_HEADER = "Authorization";
    public static final String PREFIX = "Bearer ";
}
