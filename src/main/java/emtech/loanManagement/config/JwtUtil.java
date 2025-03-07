package emtech.loanManagement.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    // Inject secret key from application.properties (must be at least 32 bytes)
    @Value("${jwt.secret}")
    private String secretKeyString;

    // Use a secure key derived from the string (or fallback to generated key if invalid)
    private final SecretKey secretKey;

    @Value("${jwt.expiration}")
    private Long expiration;

    // Initialize with a secure key
    public JwtUtil() {
        // Validate and convert the injected secretKeyString to a SecretKey
        if (secretKeyString != null && secretKeyString.getBytes().length >= 32) {
            this.secretKey = Keys.hmacShaKeyFor(secretKeyString.getBytes());
            logger.info("Using provided secret key of length: {}", secretKeyString.getBytes().length);
        } else {
            this.secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS256); // Generate a 256-bit key
            logger.warn("Provided secret key is invalid or too short (< 32 bytes). Generated a secure 256-bit key instead.");
        }
    }

    public String generateToken(UserDetails userDetails) {
        logger.info("Generating token for user: {}", userDetails.getUsername());
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, userDetails.getUsername());
    }

    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(secretKey, SignatureAlgorithm.HS256) // Use the secure key
                .compact();
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
        } catch (Exception e) {
            logger.error("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey) // Use the secure key
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Boolean isTokenExpired(String token) {
        return extractAllClaims(token).getExpiration().before(new Date());
    }
}