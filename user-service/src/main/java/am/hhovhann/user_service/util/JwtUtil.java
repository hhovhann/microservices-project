package am.hhovhann.user_service.util;

import am.hhovhann.user_service.config.SecretKeyConfiguration;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class JwtUtil {
	private static final Logger log = LoggerFactory.getLogger(JwtUtil.class);

    private final Key signingKey;
    private final SecretKeyConfiguration secretKeyConfiguration;

    public JwtUtil(SecretKeyConfiguration secretKeyConfiguration) {
        this.secretKeyConfiguration = secretKeyConfiguration;
        // Generate a secure key from the base64-encoded secret key
        // Decode the base64-encoded secret key
        this.signingKey = Keys.hmacShaKeyFor(Base64.getDecoder().decode(secretKeyConfiguration.getSecretKey()));
        log.debug("JWT signing key initialized.");
    }

    public String extractUsername(String token) {
        log.debug("Extracting username from token.");
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        log.debug("Extracting claims from token.");
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public Claims extractAllClaims(String token) {
        log.debug("Extracting all claims from token.");
        return Jwts.parser()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String generateToken(String username) {
        log.debug("Generating token for username: {}", username);
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, username);
    }

    private String createToken(Map<String, Object> claims, String subject) {
        log.debug("Creating token with subject: {}", subject);
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + secretKeyConfiguration.getSecretKeyExpirationTime()))
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validateToken(String token, String username) {
        log.debug("Validating token for username: {}", username);
        final String extractedUsername = extractUsername(token);
        boolean isValid = (extractedUsername.equals(username) && !isTokenExpired(token));
        log.debug("Token validation result: {}", isValid);
        return isValid;
    }

    private boolean isTokenExpired(String token) {
        log.debug("Checking if token is expired.");
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        log.debug("Extracting expiration date from token.");
        return extractClaim(token, Claims::getExpiration);
    }
}
