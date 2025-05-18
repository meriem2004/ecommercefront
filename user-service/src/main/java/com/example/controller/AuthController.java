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

import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/api/auth")
// REMOVE @CrossOrigin annotation - API Gateway handles CORS
public class AuthController {

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
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
            );

            final UserDetails userDetails = userDetailsService.loadUserByUsername(loginRequest.getEmail());
            final String token = jwtTokenUtil.generateToken(userDetails);
            final String refreshToken = jwtTokenUtil.generateRefreshToken(userDetails);
            
            // Get user details to include in response
            UserDTO userDTO = userService.getUserByEmail(loginRequest.getEmail())
                    .orElseThrow(() -> new Exception("User not found"));

            return ResponseEntity.ok(new JwtResponse(token, refreshToken, userDTO));
        } catch (DisabledException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Account is disabled"));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid email or password"));
        } catch (Exception e) {
            // Log the exception here
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Authentication failed: " + e.getMessage()));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody UserDTO userDTO) {
        try {
            // First, create the user
            UserDTO registeredUser = userService.createUser(userDTO);
            
            // Then, generate tokens just like in the login endpoint
            final UserDetails userDetails = userDetailsService.loadUserByUsername(registeredUser.getEmail());
            final String token = jwtTokenUtil.generateToken(userDetails);
            final String refreshToken = jwtTokenUtil.generateRefreshToken(userDetails);
            
            // Return response with tokens and user info
            return ResponseEntity.ok(new JwtResponse(token, refreshToken, registeredUser));
        } catch (Exception e) {
            // Log the exception
            e.printStackTrace();
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
                
                // Get user details to include in response
                UserDTO userDTO = userService.getUserByEmail(username)
                        .orElseThrow(() -> new Exception("User not found"));

                // Validate refresh token
                if (jwtTokenUtil.validateToken(request.getRefreshToken(), userDetails)) {
                    final String newToken = jwtTokenUtil.generateToken(userDetails);
                    return ResponseEntity.ok(new JwtResponse(newToken, request.getRefreshToken(), userDTO));
                }
            }

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Invalid refresh token"));
        } catch (Exception e) {
            // Log the exception
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Token refresh failed: " + e.getMessage()));
        }
    }

    @GetMapping("/register")
    public ResponseEntity<?> getRegistrationInfo() {
        return ResponseEntity.ok(Map.of(
            "message", "Registration endpoint is available", 
            "method", "POST",
            "requiredFields", List.of("firstName", "lastName", "email", "password")
        ));
    }

    @GetMapping("/test")
    public ResponseEntity<?> testEndpoint() {
        return ResponseEntity.ok(Map.of("message", "Auth API is working"));
    }
}