package com.clickshop.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.clickshop.entity.OrderItem;
import com.clickshop.entity.Product;
import com.clickshop.entity.User;
import com.clickshop.entity.User.Role;
import com.clickshop.security.SecurityUtils;
import com.clickshop.service.AdminService;
import com.clickshop.service.OrderService;
import com.clickshop.service.ProductService;
import com.clickshop.service.UserService;

@RestController
@RequestMapping("/admin")
public class AdminController {
	@Autowired
	private AdminService adminService;

	@Autowired
	private ProductService productService;

	@Autowired
	private UserService userService;

	@Autowired
	private OrderService orderService;

	@Autowired
	private SecurityUtils securityUtils;

	@GetMapping("/viewproducts")
	public ResponseEntity<?> getAllProducts() {
        
        try {
            List<Product> products = productService.getAllProducts();
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to load products: " + e.getMessage()));
        }
    }

	@GetMapping("/viewusers")
	public ResponseEntity<?> getAllUsers() {
		
		try {
			List<User> users = userService.getAllUsers();

			List<Map<String, Object>> usersList = new ArrayList<>();
			for (User user : users) {
				Map<String, Object> userMap = new HashMap<>();
				userMap.put("id", user.getId());
				userMap.put("name", user.getName());
				userMap.put("username", user.getUname());
				userMap.put("email", user.getEmail());
				userMap.put("role", user.getRole().toString());
				userMap.put("phone", user.getContact());
				userMap.put("address", user.getAddress());
//                userMap.put("createdAt", user.getCreatedAt());
				userMap.put("status", user.getStatus());

				usersList.add(userMap);
			}

			return ResponseEntity.ok(usersList);
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("error", "Failed to load users: " + e.getMessage()));
		}
	}

	@PutMapping("/{id}/deactivate")
	public ResponseEntity<Map<String, String>> deactivateUser(@PathVariable int id) {
		userService.updateUserStatus(id, User.Status.INACTIVE);
		Map<String, String> response = new HashMap<>();
		response.put("message", "User deactivated successfully");
		return ResponseEntity.ok(response);
	}

	@PutMapping("/{id}/activate")
	public ResponseEntity<Map<String, String>> activateUser(@PathVariable int id) {
		userService.updateUserStatus(id, User.Status.ACTIVE);
		Map<String, String> response = new HashMap<>();
		response.put("message", "User activated successfully");
		return ResponseEntity.ok(response);
	}

	@GetMapping("/viewadmins")
	public ResponseEntity<?> getAdminUsers() {
		
		try {
			List<User> allUsers = userService.getAllUsers();
			List<User> adminUsers = new ArrayList<>();

			// Filter admin users
			for (User user : allUsers) {
				if (user.getRole().equals(Role.ADMIN) || user.getRole().equals(Role.SUPER_ADMIN)) {
					adminUsers.add(user);
				}
			}

			// Transform to frontend-friendly format
			List<Map<String, Object>> usersList = new ArrayList<>();
			for (User user : adminUsers) {
				Map<String, Object> userMap = new HashMap<>();
				userMap.put("id", user.getId());
				userMap.put("name", user.getName());
				userMap.put("username", user.getUname());
				userMap.put("email", user.getEmail());
				userMap.put("role", user.getRole().toString());
				userMap.put("phone", user.getContact());
				userMap.put("address", user.getAddress());
//                userMap.put("createdAt", user.getCreatedAt());

				usersList.add(userMap);
			}

			return ResponseEntity.ok(usersList);
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("error", "Failed to load admin users: " + e.getMessage()));
		}
	}
	
	@PostMapping("/promote")
	@ResponseBody
	public ResponseEntity<Map<String, String>> promoteUser(@RequestBody Map<String, String> request) {
	    String userName = request.get("userName");

	    boolean promoted = userService.promoteToAdmin(userName);

	    Map<String, String> response = new HashMap<>();
	    if (promoted) {
	        response.put("status", "success");
	        response.put("message", "User promoted to ADMIN successfully!");
	        return ResponseEntity.ok(response);
	    } else {
	        response.put("status", "error");
	        response.put("message", "User is already an ADMIN or promotion failed.");
	        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
	    }
	}

	@PostMapping("/demote")
	@ResponseBody
	public ResponseEntity<Map<String, String>> demoteAdmin(@RequestBody Map<String, String> request) {
	    String userName = request.get("userName");

	    boolean promoted = userService.demoteAdmin(userName);

	    Map<String, String> response = new HashMap<>();
	    if (promoted) {
	        response.put("status", "success");
	        response.put("message", "Admin demoted to USER successfully!");
	        return ResponseEntity.ok(response);
	    } else {
	        response.put("status", "error");
	        response.put("message", "User is already a normal user or demotion failed.");
	        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
	    }
	}
	
	@GetMapping("/dashboard-stats")
	public ResponseEntity<Map<String, Object>> getDashboardStats() {
	    Map<String, Object> stats = adminService.getDashboardStats();
	    return ResponseEntity.ok(stats);
	}
	
	@GetMapping("/recent-orders")
    public List<OrderItem> getRecentOrders() {
        
		return orderService.getRecentOrders(5);
    }

}
