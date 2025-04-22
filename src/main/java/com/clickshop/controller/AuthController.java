package com.clickshop.controller;

import java.io.IOException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.clickshop.entity.User;
import com.clickshop.entity.User.Role;
import com.clickshop.repository.UserRepository;
import com.clickshop.security.CustomUserDetailsService;
import com.clickshop.security.JwtUtil;
import com.clickshop.service.UserService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.mailtrap.client.MailtrapClient;
import io.mailtrap.config.MailtrapConfig;
import io.mailtrap.factory.MailtrapClientFactory;
import io.mailtrap.model.request.emails.MailtrapMail;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final EmailController emailController;

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

    @Autowired
    private RestTemplate restTemplate;

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String clientSecret;

    @Value("${spring.security.oauth2.client.registration.google.redirect-uri}")
    private String redirectUri;

    private final Map<String, Map<String, Object>> otpStorage = new ConcurrentHashMap<>();

    private static final int OTP_VALID_DURATION = 2;

    AuthController(EmailController emailController) {
        this.emailController = emailController;
    }

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

    @GetMapping("/check-email")
    public ResponseEntity<?> checkEmailExists(@RequestParam String email) {
        if (email == null || email.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Email is required",
                    "exists", false));
        }

        boolean exists = userService.existsByEmail(email);
        return ResponseEntity.ok().body(Map.of(
                "exists", exists,
                "message", exists ? "Email already registered" : "Email available"));
    }

    @GetMapping("/check-username")
    public ResponseEntity<?> checkUsernameExists(@RequestParam String username) {
        if (username == null || username.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Username is required",
                    "exists", false));
        }

        boolean exists = userService.existsByUsername(username);
        return ResponseEntity.ok().body(Map.of(
                "exists", exists,
                "message", exists ? "Username already taken" : "Username available"));
    }


    @GetMapping("/oauth2/callback/google")
    public ResponseEntity<?> googleCallback(@RequestParam("code") String code, HttpServletResponse response) {
        try {
            Map<String, String> tokens = exchangeCodeForTokens(code);
            String accessToken = tokens.get("access_token");

            Map<String, String> userInfo = getUserInfoFromGoogle(accessToken);

            User user = findOrCreateGoogleUser(userInfo);

            // Generate JWT token
            final UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
            final String jwt = jwtUtil.generateToken(userDetails);

            String redirectUrl = "http://localhost:4200/intercept" +
                    "?token=" + jwt +
                    "&userId=" + user.getId() +
                    "&role=" + user.getRole() +
                    "&isAdmin=" + user.isAdmin();

            response.sendRedirect(redirectUrl);
            return null;

        } catch (Exception e) {
            e.printStackTrace();
            try {
                response.sendRedirect("http://localhost:4200/login?error=auth_failed");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            return null;
        }
    }

    private Map<String, String> exchangeCodeForTokens(String code) {
        String tokenUrl = "https://oauth2.googleapis.com/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("code", code);
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);
        body.add("redirect_uri", redirectUri);
        body.add("grant_type", "authorization_code");

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                tokenUrl,
                HttpMethod.POST,
                requestEntity,
                String.class);

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.getBody());

            Map<String, String> tokens = new HashMap<>();
            tokens.put("access_token", root.path("access_token").asText());
            tokens.put("id_token", root.path("id_token").asText());
            tokens.put("refresh_token", root.path("refresh_token").asText());

            return tokens;
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse token response", e);
        }
    }

    private Map<String, String> getUserInfoFromGoogle(String accessToken) {
        String userInfoUrl = "https://www.googleapis.com/oauth2/v3/userinfo";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<String> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                userInfoUrl,
                HttpMethod.GET,
                requestEntity,
                String.class);

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.getBody());

            Map<String, String> userInfo = new HashMap<>();
            userInfo.put("email", root.path("email").asText());
            userInfo.put("name", root.path("name").asText());
            userInfo.put("picture", root.path("picture").asText());
            userInfo.put("sub", root.path("sub").asText());

            return userInfo;
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse user info response", e);
        }
    }

    private User findOrCreateGoogleUser(Map<String, String> googleUserInfo) {
        String email = googleUserInfo.get("email");
        User user = userRepository.findByEmail(email);

        if (user == null) {
            // Create new user
            user = new User();
            user.setEmail(email);
            user.setName(googleUserInfo.get("name"));
            user.setUname(email.split("@")[0]);

            String randomPassword = generateRandomPassword();
            user.setPassword(passwordEncoder.encode(randomPassword));

            user.setRole(Role.USER);
            user.setStatus(User.Status.ACTIVE);
            user.setAuthProvider("GOOGLE");
            user.setProviderId(googleUserInfo.get("sub"));

            userRepository.save(user);
        } else {
            if (user.getAuthProvider() == null) {
                user.setAuthProvider("GOOGLE");
                user.setProviderId(googleUserInfo.get("sub"));
                userRepository.save(user);
            }
        }

        return user;
    }

    private String generateRandomPassword() {
        return java.util.UUID.randomUUID().toString();
    }

    @GetMapping("/oauth2/authorization/google")
    public ResponseEntity<?> initiateGoogleOAuth() {
        String authUrl = "https://accounts.google.com/o/oauth2/v2/auth" +
                "?client_id=" + clientId +
                "&redirect_uri=" + redirectUri +
                "&response_type=code" +
                "&scope=email%20profile" +
                "&access_type=offline";

        return ResponseEntity.ok(Map.of("authUrl", authUrl));
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
                                    "error", "User not found"));
                        }

                        return ResponseEntity.ok(Map.of(
                                "valid", true,
                                "userId", user.getId(),
                                "username", username,
                                "role", user.getRole(),
                                "isAdmin", user.isAdmin()));
                    }
                }
            } catch (Exception e) {
                // Token parsing failed
                System.err.println("Error validating token " + e);
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
                response.put("status", user.getStatus());

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

    @PostMapping("/send-otp")
    public ResponseEntity<?> sendOtp(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String name = request.get("name");

        if (email == null || email.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Email is required"));
        }

        try {
            // Generate a 6-digit OTP
            String otp = generateOTP(6);

            LocalDateTime expiryTime = LocalDateTime.now().plusMinutes(OTP_VALID_DURATION);
            Map<String, Object> otpData = new HashMap<>();
            otpData.put("otp", otp);
            otpData.put("expiry", expiryTime);
            otpStorage.put(email, otpData);

            Map<String, Object> emailData = new HashMap<>();
            emailData.put("email", email);
            emailData.put("user_name", name != null ? name : "User");
            emailData.put("otp_code", otp);
            emailData.put("expiry_minutes", OTP_VALID_DURATION);

            return emailController.sendOtpEmail(emailData);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "Failed to send OTP",
                    "error", e.getMessage()));
        }
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String otp = request.get("otp");
    
        if (email == null || email.trim().isEmpty() || otp == null || otp.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Email and OTP are required"));
        }
    
        Map<String, Object> otpData = otpStorage.get(email);
    
        if (otpData == null) {
            // Check if the OTP might have expired and been removed by the cleanup process
            // You could implement a separate expired OTP tracking mechanism here
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "success", false,
                    "message", "No active OTP found for this email. Please request a new one."));
        }
    
        String storedOtp = (String) otpData.get("otp");
        LocalDateTime expiryTime = (LocalDateTime) otpData.get("expiry");
    
        if (LocalDateTime.now().isAfter(expiryTime)) {
            // OTP has expired
            otpStorage.remove(email);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "success", false,
                    "message", "Your OTP has expired. Please request a new one.",
                    "errorCode", "OTP_EXPIRED"
            ));
        }
    
        if (!otp.equals(storedOtp)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "success", false,
                    "message", "Invalid OTP. Please try again.",
                    "errorCode", "INVALID_OTP"
            ));
        }
    
        // OTP is valid
        otpStorage.remove(email);
    
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Email verified successfully"));
    }
    

    private String generateOTP(int length) {
        String digits = "0123456789";

        SecureRandom random = new SecureRandom();
        StringBuilder otp = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            otp.append(digits.charAt(random.nextInt(digits.length())));
        }

        return otp.toString();
    }

    @GetMapping("/logout")
    public ResponseEntity<String> logout(HttpSession session) {
        session.invalidate(); // Destroy session
        return ResponseEntity.ok("Logged out successfully");
    }

}
