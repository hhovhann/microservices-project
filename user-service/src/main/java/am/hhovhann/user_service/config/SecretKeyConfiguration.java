package am.hhovhann.user_service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SecretKeyConfiguration {

    @Value("${app.token.secret.key}")
    private String secretKey;

    @Value("${app.token.secret.expiration.time}")
    private Long secretExpirationTime;

    public String getSecretKey() {
        return secretKey;
    }

    public Long getSecretKeyExpirationTime() {
        return secretExpirationTime;
    }
}
