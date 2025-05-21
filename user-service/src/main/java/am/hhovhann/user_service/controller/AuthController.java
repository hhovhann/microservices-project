package am.hhovhann.user_service.controller;

import am.hhovhann.user_service.dto.AuthenticateRequest;
import am.hhovhann.user_service.dto.RegisterRequest;
import am.hhovhann.user_service.dto.UserDto;
import am.hhovhann.user_service.entity.User;
import am.hhovhann.user_service.service.UserDetailsService;
import am.hhovhann.user_service.util.JwtUtil;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@RestController
@RequestMapping("/v1/api/user")
@CrossOrigin(
        origins = "http://localhost:3000",
        allowCredentials = "true"  // ✅ Important
)
public class AuthController {
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;

    public AuthController(
            JwtUtil jwtUtil,
            AuthenticationManager authenticationManager,
            UserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserDto> register(@RequestBody RegisterRequest userRequest) {
        try {
            log.info("Registering new user: {}", userRequest.username());
            User user =
                    userDetailsService.registerNewUser(
                            userRequest.username(), userRequest.password(), userRequest.email());
            UserDto response = new UserDto(user.getUserId(), user.getUsername(), user.getEmail());
            log.info("User registered successfully: {}", userRequest.username());
            return ResponseEntity.ok(response);
        } catch (IllegalStateException e) {
            log.error("Error registering user: %s", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/authenticate")
    public ResponseEntity<String> authenticate(@RequestBody AuthenticateRequest userRequest, HttpServletResponse response) {
        try {
            log.info("Attempting to authenticate user: {}", userRequest.username());
            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(userRequest.username(), userRequest.password()));
            String jwtToken = jwtUtil.generateToken(userRequest.username());
            log.info("User authenticated successfully: {}", userRequest.username());
            return ResponseEntity.ok(jwtToken);
        } catch (AuthenticationException e) {
            log.warn("Authentication failed for user: {}", userRequest.username());
            return ResponseEntity.status(UNAUTHORIZED).body("Invalid credentials");
        }
    }

    @GetMapping("/me")
    public ResponseEntity<UserDto> getCurrentUser(@RequestHeader(name = "Authorization", required = false) String authHeader) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String token = authHeader.substring(7); // remove "Bearer "
        String username = jwtUtil.extractUsername(token);

        if (!jwtUtil.validateToken(token, username)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        User user = userDetailsService.loadDomainUserByUsername(username);
        UserDto response = new UserDto(user.getUserId(), user.getUsername(), user.getEmail());

        return ResponseEntity.ok(response);
    }
}
