package com.clickshop.controller;

import com.clickshop.entity.OrderItem;
import com.clickshop.entity.Product;
import com.clickshop.entity.User;
import com.clickshop.entity.User.Role;
import com.clickshop.service.CartService;
import com.clickshop.service.OrderService;
import com.clickshop.service.ProductService;
import com.clickshop.service.UserService;
import com.clickshop.utils.SessionUtil;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

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

//	To check whether user is admin or user
	@GetMapping("/check-role")
	public ResponseEntity<String> checkUserRole(HttpSession session) {
		User.Role role = (User.Role) session.getAttribute("role");

		if (role != null) {
			return ResponseEntity.ok(role.toString()); // Return the role as a string
		} else {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not authenticated");
		}
	}

//    Get user profile data
	@GetMapping("/profile")
	public ResponseEntity<?> getUserProfile(HttpSession session) {
		// Check if user is logged in
		if (!SessionUtil.isValidSession(session)) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body(Map.of("error", "Unauthorized access", "redirect", "/auth/login"));
		}

		try {
			// Get userId from session
			Integer userId = (Integer) session.getAttribute("userId");
			System.out.println("here" + userId);
			if (userId == null) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
						.body(Map.of("error", "No user ID in session", "redirect", "/auth/login"));
			}

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
	public ResponseEntity<?> updateUserProfile(@RequestBody Map<String, Object> profileData, HttpSession session) {
		// Check if user is logged in
		if (!SessionUtil.isValidSession(session)) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body(Map.of("error", "Unauthorized access", "redirect", "/auth/login"));
		}

		try {
			// Get userId from session
			Integer userId = (Integer) session.getAttribute("userId");
			if (userId == null) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
						.body(Map.of("error", "No user ID in session", "redirect", "/auth/login"));
			}

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
//                userProfile.put("createdAt", updatedUser.getCreatedAt());

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

	@GetMapping("/userdashboard")
	public String showDashboardPage(ModelMap model, HttpSession session) {
		User.Role role = (Role) session.getAttribute("role");
		if (!SessionUtil.isValidSession(session) || role.equals(Role.ADMIN) || role.equals(Role.SUPER_ADMIN)) {
			return "redirect:/clickshop/auth/login";
		} else if (role.equals(Role.USER)) {
			return "userDashboard";
		}
		return "redirect:/clickshop/auth/login";
	}

	@GetMapping("/changepassword")
	public String showPasswordPage(ModelMap model, HttpSession session) {
		User.Role role = (Role) session.getAttribute("role");
		if (!SessionUtil.isValidSession(session) || role.equals(Role.ADMIN) || role.equals(Role.SUPER_ADMIN)) {
			return "redirect:/clickshop/auth/login";
		} else if (role.equals(Role.USER)) {
			return "changePassword";
		}
		return "redirect:/clickshop/auth/login";
	}

	@GetMapping("/usercart")
	public String showCartPage(ModelMap model, HttpSession session) {

		User.Role role = (Role) session.getAttribute("role");
		if (!SessionUtil.isValidSession(session) || role.equals(Role.ADMIN) || role.equals(Role.SUPER_ADMIN)) {
			return "redirect:/clickshop/auth/login";
		} else if (role.equals(Role.USER)) {
			int uid = (int) session.getAttribute("userId");
			List<Map<String, Object>> cartItems = cartService.getCartItemsByUserId(uid);
			model.addAttribute("cartItems", cartItems);
			return ("viewCart");
		}
		return "redirect:/clickshop/auth/login";
	}

	@GetMapping("/products")
	public ResponseEntity<?> getProducts(HttpServletRequest request, HttpSession session) {
		User.Role role = (User.Role) session.getAttribute("role");

		if (role == null || role.equals(User.Role.USER)) {
			List<Product> products = productService.getAllProducts();
			return ResponseEntity.ok(products);
		}

		if (role.equals(User.Role.ADMIN) || role.equals(User.Role.SUPER_ADMIN)) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body(Map.of("error", "Unauthorized access", "redirect", "/auth/login"));
		}

		return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
				.body(Map.of("error", "Authentication required", "redirect", "/clickshop/auth/login"));
	}

	// Update user password
	@PutMapping("/changepassword")
	public ResponseEntity<String> updatePassword(

			@RequestParam String oldPassword, @RequestParam String newPassword, HttpSession session) {

		if (!SessionUtil.isValidSession(session)) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated. Please log in.");
		}

		Integer uid = (Integer) session.getAttribute("userId");
		System.out.println(uid);
		if (uid == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated. Please log in.");
		}

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
	public ResponseEntity<List<OrderItem>> getOrderHistory(HttpSession session) {
		try {
			Integer uid = (Integer) session.getAttribute("userId");
			if (uid == null) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
			}

			List<OrderItem> orders = orderService.getOrderHistoryByUserId(uid);
			System.out.println(orders.toString());
			return ResponseEntity.ok(orders);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	@GetMapping(value = "/cartitems", produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<List<Map<String, Object>>> getCartItem(HttpSession session) {
		int uid = (int) session.getAttribute("userId");
		System.out.println(uid);
		List<Map<String, Object>> cartItems = cartService.getCartItemsByUserId(uid);
//    	System.out.println(cartItems);
		return ResponseEntity.ok(cartItems);
	}

	@GetMapping("/checkout")
	public ResponseEntity<String> checkout(HttpSession session) {
		int userId = (int) session.getAttribute("userId");
		System.out.println("User ID during checkout: " + userId);

		String resp = orderService.checkout(userId);
		System.out.println("Checkout response: " + resp); // Log the actual response string

		return ResponseEntity.ok(resp); // Return the response as plain text
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