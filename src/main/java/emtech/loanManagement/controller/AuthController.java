package emtech.loanManagement.controller;

import emtech.loanManagement.config.JwtUtil;
import emtech.loanManagement.dto.AuthResponse;
import emtech.loanManagement.dto.LoginRequest;
import emtech.loanManagement.dto.RegisterRequest;
import emtech.loanManagement.entity.User;
import emtech.loanManagement.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        logger.info("Registering user: {}", request.getUsername());
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            logger.warn("Username {} already exists", request.getUsername());
            return ResponseEntity.badRequest().body("Username already exists");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        logger.info("Saving user: {}", user.getUsername());
        userRepository.save(user);
        logger.info("User saved successfully");

        return ResponseEntity.ok("User registered successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        logger.info("Attempting to login user: {}", request.getUsername());
        try {
            logger.debug("Authenticating with username: {} and password: {}", request.getUsername(), request.getPassword());
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
            logger.debug("Authentication successful for user: {}", request.getUsername());
        } catch (Exception e) {
            logger.error("Authentication failed for user {}: {}", request.getUsername(), e.getMessage());
            return ResponseEntity.badRequest().body("Invalid username or password");
        }

        final UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
        final String jwt = jwtUtil.generateToken(userDetails);
        logger.info("Generated JWT token for user: {}", request.getUsername());

        return ResponseEntity.ok(new AuthResponse(jwt));
    }
}