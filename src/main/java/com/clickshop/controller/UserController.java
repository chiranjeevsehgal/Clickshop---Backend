package com.clickshop.controller;

import com.clickshop.entity.OrderItem;
import com.clickshop.entity.Product;
import com.clickshop.entity.User;
import com.clickshop.security.SecurityUtils;
import com.clickshop.service.CartService;
import com.clickshop.service.OrderService;
import com.clickshop.service.ProductService;
import com.clickshop.service.UserService;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
public class UserController {

	@Autowired
	private UserService userService;

	@Autowired
	private OrderService orderService;

	@Autowired
	private ProductService productService;

	@Autowired
	private CartService cartService;
	
	@Autowired
	private SecurityUtils securityUtils;

	// To check whether user is admin or user
	@GetMapping("/check-role")
	public ResponseEntity<String> checkUserRole() {
		
		User.Role role = securityUtils.getCurrentUserRole();
		System.out.println(role);
		return ResponseEntity.ok(role.toString()); // Return the role as a string
		
	}

	// Get user profile data
	@GetMapping("/profile")
	public ResponseEntity<?> getUserProfile() {
		// Check if user is logged in
		

		try {
			int userId = securityUtils.getCurrentUserId();
			System.out.println("here" + userId);
			
			// Get user details from service
			User user = userService.getUserById(userId);
			if (user == null) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "User not found"));
			}

			// Create response with user data (excluding sensitive information)
			Map<String, Object> userProfile = new HashMap<>();
			userProfile.put("id", user.getId());
			userProfile.put("name", user.getName());
			userProfile.put("email", user.getEmail());
			userProfile.put("phone", user.getContact());
			userProfile.put("address", user.getAddress());
			userProfile.put("username", user.getUname());

			return ResponseEntity.ok(userProfile);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("error", "An error occurred: " + e.getMessage()));
		}
	}

	// Edit user profile except password
	@PutMapping("/updateprofile")
	public ResponseEntity<?> updateUserProfile(@RequestBody Map<String, Object> profileData) {
		// Check if user is logged in
		
		try {
			int userId = securityUtils.getCurrentUserId();
			
			// Update user profile
			boolean updated = userService.updateUserProfile(userId, profileData);
			if (updated) {
				// Get updated user data
				User updatedUser = userService.getUserById(userId);

				// Create response with updated user data
				Map<String, Object> userProfile = new HashMap<>();
				userProfile.put("id", updatedUser.getId());
				userProfile.put("name", updatedUser.getName());
				userProfile.put("email", updatedUser.getEmail());
				userProfile.put("phone", updatedUser.getContact());
				userProfile.put("address", updatedUser.getAddress());
				// userProfile.put("createdAt", updatedUser.getCreatedAt());

				return ResponseEntity.ok(userProfile);
			} else {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Failed to update profile"));
			}
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("error", "An error occurred: " + e.getMessage()));
		}
	}

	@GetMapping("/add") // Handles GET request for the login page
	public String showRegisterPage(ModelMap model) {
		return ("register");
	}

	// Update user password
	@PutMapping("/changepassword")
	public ResponseEntity<String> updatePassword(

		@RequestParam String oldPassword, @RequestParam String newPassword) {

		int uid = securityUtils.getCurrentUserId();
		
		try {
			boolean isUpdated = userService.updateUser(uid, oldPassword, newPassword);
			if (isUpdated) {
				System.out.println("Password Changed");
				return ResponseEntity.ok("Password updated successfully!");
			} else {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to update password.");
			}
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
		}
	}

	@GetMapping(value = "/vieworders", produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<List<OrderItem>> getOrderHistory() {
		try {
			int uid = securityUtils.getCurrentUserId();
			
			List<OrderItem> orders = orderService.getOrderHistoryByUserId(uid);
			System.out.println(orders.toString());
			return ResponseEntity.ok(orders);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	@GetMapping(value = "/cartitems", produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<List<Map<String, Object>>> getCartItem(HttpServletRequest request) {
		// Get the authenticated user from the security c
		int userId = securityUtils.getCurrentUserId();

		System.out.println("User ID from JWT: " + userId);

		List<Map<String, Object>> cartItems = cartService.getCartItemsByUserId(userId);
		return ResponseEntity.ok(cartItems);
	}

	@DeleteMapping("/delete")
	@ResponseBody
	public String deleteUser(@RequestParam("id") int userId) {
		try {
			boolean isDeleted = userService.deleteUser(userId);
			return isDeleted ? "SUCCESS" : "FAIL";
		} catch (Exception e) {
			e.printStackTrace();
			return "ERROR";
		}
	}

	// Get user by email
	@GetMapping("/email/{email}")
	public User getUserByEmail(@PathVariable String email) {
		return userService.getUserByEmail(email);
	}

	// Get user by id
	@GetMapping("/{id}")
	public User getUserById(@PathVariable int id) {
		return userService.getUserById(id);
	}

	// Get all users
	@GetMapping("/all")
	public ArrayList<User> getAllUsers() {
		return userService.getAllUsers();
	}

	// Check if a user is an admin
	@GetMapping("/is-admin/{userId}")
	public boolean isAdmin(@PathVariable int userId) {
		return userService.isAdmin(userId);
	}

}