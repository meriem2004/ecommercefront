package com.example.controller;

import com.example.config.JwtTokenUtil;
import com.example.dto.JwtResponse;
import com.example.dto.LoginRequest;
import com.example.dto.RefreshTokenRequest;
import com.example.dto.UserDTO;
import com.example.security.JwtUserDetailsService;
import com.example.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private JwtUserDetailsService userDetailsService;

    @Autowired
    private UserService userService;
    
    @Value("${jwt.expiration}")
    private long jwtExpiration;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            logger.info("Login attempt for email: {}", loginRequest.getEmail());
            
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
            );

            final UserDetails userDetails = userDetailsService.loadUserByUsername(loginRequest.getEmail());
            
            // Get user details to include in response
            UserDTO userDTO = userService.getUserByEmail(loginRequest.getEmail())
                    .orElseThrow(() -> new Exception("User not found"));

            // Extract roles from UserDetails
            List<String> roles = userDetails.getAuthorities().stream()
                    .map(authority -> authority.getAuthority())
                    .collect(Collectors.toList());

            logger.info("Generating token for user: {} with ID: {} and roles: {}", 
                       loginRequest.getEmail(), userDTO.getId(), roles);

            // Generate tokens with user ID and roles
            final String token = jwtTokenUtil.generateToken(userDetails, userDTO.getId(), roles);
            final String refreshToken = jwtTokenUtil.generateRefreshToken(userDetails);
            
            logger.info("Login successful for user: {} with ID: {}", loginRequest.getEmail(), userDTO.getId());

            return ResponseEntity.ok(new JwtResponse(token, refreshToken, userDTO));
        } catch (DisabledException e) {
            logger.warn("Login failed - account disabled for: {}", loginRequest.getEmail());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Account is disabled"));
        } catch (BadCredentialsException e) {
            logger.warn("Login failed - bad credentials for: {}", loginRequest.getEmail());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid email or password"));
        } catch (Exception e) {
            logger.error("Login failed for: {} - Error: {}", loginRequest.getEmail(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Authentication failed: " + e.getMessage()));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody UserDTO userDTO) {
        try {
            logger.info("Registration attempt for email: {}", userDTO.getEmail());
            
            // Create the user
            UserDTO registeredUser = userService.createUser(userDTO);
            
            // Generate tokens just like in login
            final UserDetails userDetails = userDetailsService.loadUserByUsername(registeredUser.getEmail());
            
            // Extract roles from UserDetails
            List<String> roles = userDetails.getAuthorities().stream()
                    .map(authority -> authority.getAuthority())
                    .collect(Collectors.toList());

            logger.info("Generating token for new user: {} with ID: {} and roles: {}", 
                       registeredUser.getEmail(), registeredUser.getId(), roles);

            // Generate tokens with user ID and roles
            final String token = jwtTokenUtil.generateToken(userDetails, registeredUser.getId(), roles);
            final String refreshToken = jwtTokenUtil.generateRefreshToken(userDetails);
            
            logger.info("Registration successful for user: {} with ID: {}", registeredUser.getEmail(), registeredUser.getId());
            
            return ResponseEntity.ok(new JwtResponse(token, refreshToken, registeredUser));
        } catch (Exception e) {
            logger.error("Registration failed for: {} - Error: {}", userDTO.getEmail(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Registration failed: " + e.getMessage()));
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        try {
            // Extract username from token
            final String username = jwtTokenUtil.extractUsername(request.getRefreshToken());

            if (username != null) {
                final UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                
                // Get user details
                UserDTO userDTO = userService.getUserByEmail(username)
                        .orElseThrow(() -> new Exception("User not found"));

                // Validate refresh token
                if (jwtTokenUtil.validateToken(request.getRefreshToken(), userDetails)) {
                    
                    // Extract roles from UserDetails
                    List<String> roles = userDetails.getAuthorities().stream()
                            .map(authority -> authority.getAuthority())
                            .collect(Collectors.toList());

                    logger.info("Refreshing token for user: {} with ID: {} and roles: {}", 
                               username, userDTO.getId(), roles);

                    // Generate new token with user ID and roles
                    final String newToken = jwtTokenUtil.generateToken(userDetails, userDTO.getId(), roles);
                    
                    return ResponseEntity.ok(new JwtResponse(newToken, request.getRefreshToken(), userDTO));
                }
            }

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Invalid refresh token"));
        } catch (Exception e) {
            logger.error("Token refresh failed - Error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Token refresh failed: " + e.getMessage()));
        }
    }

    @GetMapping("/verify")
    public ResponseEntity<?> verifyToken() {
        // This endpoint will be protected by JWT filter
        return ResponseEntity.ok(Map.of("valid", true));
    }

    @GetMapping("/test")
    public ResponseEntity<?> testEndpoint() {
        return ResponseEntity.ok(Map.of("message", "Auth API is working"));
    }
}