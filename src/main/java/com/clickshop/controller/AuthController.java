package com.clickshop.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.clickshop.entity.Product;
import com.clickshop.entity.User;
import com.clickshop.entity.User.Role;
import com.clickshop.service.CartService;
import com.clickshop.service.OrderService;
import com.clickshop.service.ProductService;
import com.clickshop.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class AuthController {
    
	@Autowired
    private UserService userService;
    
    @Autowired
    private OrderService orderService;

    @Autowired
    private ProductService productService;

    @Autowired
    private CartService cartService;
    
 // Add a new user
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody Map<String, Object> registrationData) {
        try {
            // Create new user from the registration data
            User user = new User();
            user.setName((String) registrationData.get("name"));
            user.setEmail((String) registrationData.get("email"));
            user.setUname((String) registrationData.get("username"));
            user.setPassword((String) registrationData.get("password"));
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
            
            System.out.println("Registering user: " + user);
            
            boolean isAdded = userService.addUserService(user);
            
            if (isAdded) {
                Map<String, String> response = new HashMap<>();
                response.put("message", "User registered successfully");
                response.put("status", "success");
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
    
    @GetMapping("/register") 
    public String  showRegisterPage(ModelMap model, HttpServletRequest request) {
    	return ("register"); 
    }
    
    // User login
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> loginUser(@RequestBody Map<String, String> loginData, HttpSession session) {
        String username = loginData.get("username");
        String password = loginData.get("password");

        System.out.println(username + " " + password);
        User user = userService.loginUser(username, password);

        if (user != null) {
            session.setAttribute("loggedInUser", user);
            session.setAttribute("userId", user.getId());
            session.setAttribute("role", user.getRole());
            session.setAttribute("isAdmin", user.isAdmin());

            return ResponseEntity.ok().body(Map.of(
                "message", "Login successful",
                "userId", user.getId(),
                "role", user.getRole(),
                "isAdmin", user.isAdmin()
            ));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                "message", "Invalid username or password"
            ));
        }
    }


	@GetMapping("/logout")
	public ResponseEntity<String> logout(HttpSession session) {
	    session.invalidate(); // Destroy session
	    return ResponseEntity.ok("Logged out successfully");
	}
    
    @GetMapping("/login") // Handles GET request for the login page
    public String  showLoginPage(ModelMap model, HttpSession session) {
    	Object uid = session.getAttribute("userId");
    	if (uid == null) {
    		return ("loginpage");    		
    	}
    	User.Role role= (Role) session.getAttribute("role");
    	if (role.equals(Role.ADMIN) || role.equals(Role.SUPER_ADMIN)){
    		return "redirect:/clickshop/admin/dashboard";
    	}
    	else
    		return "redirect:/clickshop/users/userdashboard";
    }

}
