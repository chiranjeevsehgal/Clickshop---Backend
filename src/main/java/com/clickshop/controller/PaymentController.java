package com.clickshop.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.clickshop.entity.User;
import com.clickshop.security.SecurityUtils;
import com.clickshop.service.PaymentService;
import com.clickshop.service.UserService;

@RestController
@RequestMapping("/payments")
public class PaymentController {
    
    @Autowired
    private PaymentService paymentService;

    @Autowired
    private SecurityUtils securityUtils;
    
    @Autowired
    private UserService userService;

    @Value("${razorpay.key.id}")
    private String razorpayKeyId;
    
    @Value("${razorpay.key.secret}")
    private String razorpayKeySecret;

    @GetMapping("/verify-keys")
    public ResponseEntity<?> verifyRazorpayKeys() {
        try {
            // Test if keys are loaded
            boolean hasKeyId = razorpayKeyId != null && !razorpayKeyId.isEmpty();
            boolean hasKeySecret = razorpayKeySecret != null && !razorpayKeySecret.isEmpty();
            
            // Mask secret for security
            String maskedSecret = hasKeySecret ? 
                    razorpayKeySecret.substring(0, 4) + "..." + 
                    razorpayKeySecret.substring(razorpayKeySecret.length() - 4) : "none";
            
            return ResponseEntity.ok(Map.of(
                "keyId", hasKeyId ? razorpayKeyId : "none",
                "secretKey", maskedSecret,
                "status", (hasKeyId && hasKeySecret) ? "Keys loaded" : "Missing keys"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/create-order")
    public ResponseEntity<?> createOrder(@RequestBody Map<String, Object> orderRequest) {
        // Check if user is logged in
        int userId = securityUtils.getCurrentUserId();
        
        try {
            User user = userService.getUserById(userId);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "User not found"));
            }
            
            Map<String, Object> orderData = paymentService.createRazorpayOrder(orderRequest, user);
            
            // Add user ID to the response
            orderData.put("userId", userId);
            
            return ResponseEntity.ok(orderData);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to create payment order: " + e.getMessage()));
        }
    }

    @PostMapping("/verify-payment")
    public ResponseEntity<?> verifyPayment(@RequestBody Map<String, String> paymentData) {
        // Check if user is logged in
        
        try {
            boolean isVerified = paymentService.verifyRazorpayPayment(paymentData);
            if (isVerified) {
                return ResponseEntity.ok(Map.of("status", "success", "message", "Payment verified successfully"));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("status", "failed", "message", "Payment verification failed"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to verify payment: " + e.getMessage()));
        }
    }

	
}
