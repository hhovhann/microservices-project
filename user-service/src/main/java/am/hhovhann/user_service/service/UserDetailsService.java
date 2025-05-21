package am.hhovhann.user_service.service;

import am.hhovhann.user_service.entity.User;
import am.hhovhann.user_service.producer.KafkaProducer;
import am.hhovhann.user_service.repository.UserRepository;
import am.hhovhann.user_service.util.JwtUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsService
        implements org.springframework.security.core.userdetails.UserDetailsService {
    private static final Logger log = LoggerFactory.getLogger(UserDetailsService.class);

    private final String USER_AUTHORITIES = "USER";
    private final ObjectMapper objectMapper;
    private final KafkaProducer kafkaProducer;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserDetailsService(
            ObjectMapper objectMapper,
            KafkaProducer kafkaProducer,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {
        this.objectMapper = objectMapper;
        this.kafkaProducer = kafkaProducer;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("Attempting to load user by username: {}", username);

        User user =
                userRepository
                        .findByUsername(username)
                        .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        log.info("Successfully loaded user: {}", username);
        return org.springframework.security.core.userdetails.User.withUsername(user.getUsername())
                .password(user.getPassword())
                .authorities(USER_AUTHORITIES)
                .build();
    }

    public User registerNewUser(String username, String password, String email) {
        log.info("Attempting to register new user with username: {}", username);
        if (userRepository.findByUsername(username).isPresent()) {
            log.error("Username already taken: {}", username);
            throw new IllegalStateException("Username already taken");
        }
        String encodedPassword = passwordEncoder.encode(password);
        User newUser = new User(username, encodedPassword, email);
        User savedUser = userRepository.save(newUser);
        log.info("Successfully registered new user: {}", username);

        try {
            kafkaProducer.sendUserRegisteredEvent(
                    savedUser.getUserId(), objectMapper.writeValueAsString(savedUser));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return savedUser;
    }

    public User findById(String userId) {
        return userRepository
                .findByUserId(userId)
                .orElseThrow(
                        () -> new EntityNotFoundException("User with id %s isn't not found".formatted(userId)));
    }

    public User loadDomainUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }
}
