package com.clickshop.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.clickshop.entity.User;
import com.clickshop.entity.User.Role;
import com.clickshop.repository.UserRepository;
import com.clickshop.security.CustomUserDetailsService;
import com.clickshop.security.JwtUtil;
import com.clickshop.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    // Add a new user
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody Map<String, Object> registrationData) {
        // Create new user from the registration data
        User user = new User();
        user.setName((String) registrationData.get("name"));
        user.setEmail((String) registrationData.get("email"));
        user.setUname((String) registrationData.get("username"));
        try {
            if (userRepository.existsByEmail(user.getEmail()) ||
                    userRepository.existsByUname(user.getUname())) {
                return ResponseEntity
                        .status(HttpStatus.CONFLICT)
                        .body("User with same email or username already exists");
            }

            // Encode the password before saving
            String rawPassword = (String) registrationData.get("password");
            user.setPassword(passwordEncoder.encode(rawPassword));

            user.setContact((String) registrationData.get("contactNumber"));

            // Handle address (which is a nested object in the frontend)
            if (registrationData.get("address") instanceof Map) {
                Map<String, Object> addressMap = (Map<String, Object>) registrationData.get("address");
                StringBuilder address = new StringBuilder();
                address.append(addressMap.get("addressLine1")).append("\n");

                if (addressMap.get("addressLine2") != null && !((String) addressMap.get("addressLine2")).isEmpty()) {
                    address.append(addressMap.get("addressLine2")).append("\n");
                }

                address.append(addressMap.get("city")).append(", ")
                        .append(addressMap.get("state")).append(" ")
                        .append(addressMap.get("zipCode"));

                user.setAddress(address.toString());
            }

            user.setRole(Role.USER); // Or however you define the default role
            user.setStatus(User.Status.ACTIVE);

            System.out.println("Registering user: " + user);

            boolean isAdded = userService.addUserService(user);

            if (isAdded) {
                // Generate JWT token for the newly registered user
                final UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
                final String jwt = jwtUtil.generateToken(userDetails);

                Map<String, Object> response = new HashMap<>();
                response.put("message", "User registered successfully");
                response.put("status", "success");
                response.put("token", jwt);
                response.put("user", Map.of(
                        "id", user.getId(),
                        "name", user.getName(),
                        "email", user.getEmail(),
                        "role", user.getRole(),
                        "isAdmin", user.isAdmin()));
                return ResponseEntity.ok(response);
            } else {
                Map<String, String> response = new HashMap<>();
                response.put("message", "User cannot be registered. Email or username may already exist.");
                response.put("status", "error");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> response = new HashMap<>();
            response.put("message", "Registration failed: " + e.getMessage());
            response.put("status", "error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/test")
    public ResponseEntity<String> testBackend() {
        return ResponseEntity.ok("✅ Backend is working!");
    }

    @GetMapping("/validate-token")
    public ResponseEntity<?> validateToken(HttpServletRequest request) {
        final String authorizationHeader = request.getHeader("Authorization");

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String jwt = authorizationHeader.substring(7);
            try {
                String username = jwtUtil.extractUsername(jwt);

                if (username != null) {
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                    if (jwtUtil.validateToken(jwt, userDetails)) {
                        // Token is valid
                        User user = userRepository.findByEmail(username);
                        if (user == null) {
                            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                                    "valid", false,
                                    "error", "User not found"
                            ));
                        }
                    
                        return ResponseEntity.ok(Map.of(
                                "valid", true,
                                "userId", user.getId(),
                                "username", username,
                                "role", user.getRole(),
                                "isAdmin", user.isAdmin()
                        ));
                    }
                }
            } catch (Exception e) {
                // Token parsing failed
                System.err.println("Error validating token "+ e);
            }
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                "valid", false,
                "message", "Invalid or expired token"));
    }

    // User login
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> loginUser(@RequestBody Map<String, String> loginData) {
        String username = loginData.get("username");
        String password = loginData.get("password");

        System.out.println(username + " " + password);

        try {
            // First use your existing service to validate credentials
            User user = userService.loginUser(username, password);
            System.out.println(user);
            if (user != null) {
                // Create authentication token
                try {
                    authenticationManager.authenticate(
                            new UsernamePasswordAuthenticationToken(user.getEmail(), password));
                } catch (BadCredentialsException e) {
                    // This shouldn't happen since userService already validated the credentials
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                            "message", "Invalid username or password"));
                }

                // Generate JWT Token
                final UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
                final String jwt = jwtUtil.generateToken(userDetails);

                // Still maintain session for backward compatibility
                // session.setAttribute("loggedInUser", user);
                // session.setAttribute("userId", user.getId());
                // session.setAttribute("role", user.getRole());
                // session.setAttribute("isAdmin", user.isAdmin());

                Map<String, Object> response = new HashMap<>();
                response.put("message", "Login successful");
                response.put("token", jwt);
                response.put("userId", user.getId());
                response.put("role", user.getRole());
                response.put("isAdmin", user.isAdmin());

                return ResponseEntity.ok().body(response);
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                        "message", "Invalid username or password"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "message", "Login failed: " + e.getMessage()));
        }
    }

    @GetMapping("/logout")
    public ResponseEntity<String> logout(HttpSession session) {
        session.invalidate(); // Destroy session
        return ResponseEntity.ok("Logged out successfully");
    }

}
