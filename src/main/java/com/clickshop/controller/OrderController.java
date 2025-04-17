package com.clickshop.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.clickshop.entity.OrderItem;
import com.clickshop.entity.User;
import com.clickshop.entity.User.Role;
import com.clickshop.service.CartService;
import com.clickshop.service.OrderService;
import com.clickshop.service.UserService;
import com.clickshop.utils.SessionUtil;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/orders")
public class OrderController {
	

	@Autowired
	private OrderService orderService;
	
	@Autowired
	private UserService userService;

	@Autowired
	private CartService cartService;

	@GetMapping("/all")
    public ResponseEntity<?> getAllOrders(HttpSession session) {
        User.Role role = (Role) session.getAttribute("role");
        
        // Check if user is admin
        if (!SessionUtil.isValidSession(session) || role == null || 
            (!role.equals(Role.ADMIN) && !role.equals(Role.SUPER_ADMIN))) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Unauthorized access", "redirect", "/auth/login"));
        }
        
        try {
            List<OrderItem> orders = orderService.getAllOrders();
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to load orders: " + e.getMessage()));
        }
    }
	
	@GetMapping("/{id}")
	public ResponseEntity<?> getOrderById(@PathVariable int id, HttpSession session) {
	    User.Role role = (Role) session.getAttribute("role");

	    if (!SessionUtil.isValidSession(session) || role == null ||
	        (!role.equals(Role.ADMIN) && !role.equals(Role.SUPER_ADMIN))) {
	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
	                .body(Map.of("error", "Unauthorized access", "redirect", "/auth/login"));
	    }

	    try {
	        OrderItem order = orderService.getOrderById(id);
	        User customer = order.getUser();
	        Map<String, Object> response = new HashMap<>();
	        response.put("order", order);
	        response.put("customer", customer);
	        	
	        return ResponseEntity.ok(response);
	    } catch (NoSuchElementException e) {
	        return ResponseEntity.status(HttpStatus.NOT_FOUND)
	                .body(Map.of("error", e.getMessage()));
	    } catch (Exception e) {
	        e.printStackTrace();
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body(Map.of("error", "Failed to fetch order"));
	    }
	}
	
//	Update the status of order
	@PutMapping("/{orderId}/status")
	public ResponseEntity<?> updateOrderStatus(
	        @PathVariable int orderId,
	        @RequestBody Map<String, String> statusUpdate,
	        HttpSession session) {
	    User.Role role = (Role) session.getAttribute("role");
	    
	    // Check if user is admin
	    if (!SessionUtil.isValidSession(session) || role == null || 
	        (!role.equals(Role.ADMIN) && !role.equals(Role.SUPER_ADMIN))) {
	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
	                .body(Map.of("error", "Unauthorized access", "redirect", "/auth/login"));
	    }
	    
	    try {
	        String newStatus = statusUpdate.get("status");
	        if (newStatus == null || newStatus.isEmpty()) {
	            return ResponseEntity.badRequest().body(Map.of("error", "Status cannot be empty"));
	        }
	        
	        boolean updated = orderService.updateOrderStatus(orderId, newStatus);
	        if (updated) {
	            return ResponseEntity.ok(Map.of("message", "Order status updated successfully"));
	        } else {
	            return ResponseEntity.status(HttpStatus.NOT_FOUND)
	                    .body(Map.of("error", "Order not found or status update failed"));
	        }
	    } catch (IllegalArgumentException e) {
	        return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
	    } catch (Exception e) {
	        e.printStackTrace();
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body(Map.of("error", "Failed to update order status: " + e.getMessage()));
	    }
	}

	/**
	 * Cancel an order item
	 */
	@PutMapping("/{orderId}/cancel")
	public ResponseEntity<?> cancelOrder(@PathVariable int orderId, HttpSession session) {
	    User.Role role = (Role) session.getAttribute("role");
	    
	    // Check if user is admin
	    if (!SessionUtil.isValidSession(session) || role == null || 
	        (!role.equals(Role.ADMIN) && !role.equals(Role.SUPER_ADMIN))) {
	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
	                .body(Map.of("error", "Unauthorized access", "redirect", "/auth/login"));
	    }
	    
	    try {
	        boolean cancelled = orderService.cancelOrder(orderId);
	        if (cancelled) {
	            return ResponseEntity.ok(Map.of("message", "Order cancelled successfully"));
	        } else {
	            return ResponseEntity.status(HttpStatus.NOT_FOUND)
	                    .body(Map.of("error", "Order not found or cancellation failed"));
	        }
	    } catch (IllegalStateException e) {
	        return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
	    } catch (Exception e) {
	        e.printStackTrace();
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body(Map.of("error", "Failed to cancel order: " + e.getMessage()));
	    }
	}

	/**
	 * Get orders by status
	 */
	@GetMapping("/status/{status}")
	public ResponseEntity<?> getOrdersByStatus(
	        @PathVariable String status,
	        HttpSession session) {
	    User.Role role = (Role) session.getAttribute("role");
	    
	    // Check if user is admin
	    if (!SessionUtil.isValidSession(session) || role == null || 
	        (!role.equals(Role.ADMIN) && !role.equals(Role.SUPER_ADMIN))) {
	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
	                .body(Map.of("error", "Unauthorized access", "redirect", "/auth/login"));
	    }
	    
	    try {
	        List<OrderItem> orders = orderService.getOrdersByStatus(status);
	        return ResponseEntity.ok(orders);
	    } catch (IllegalArgumentException e) {
	        return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
	    } catch (Exception e) {
	        e.printStackTrace();
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body(Map.of("error", "Failed to load orders: " + e.getMessage()));
	    }
	}

	@PostMapping("/create")
    public ResponseEntity<?> createOrder(@RequestBody Map<String, Object> orderRequest, HttpSession session) {
        // Check if user is logged in
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Please login to create an order"));
        }
        
        try {
            User user = userService.getUserById(userId);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "User not found"));
            }
            
            // Here, we're just preparing order data and not saving it yet
            // The actual saving happens after payment confirmation
            Map<String, Object> orderData = orderService.prepareOrder(orderRequest, user);
            return ResponseEntity.ok(orderData);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to create order: " + e.getMessage()));
        }
    }

	@PostMapping("/save")
	public ResponseEntity<?> saveOrder(@RequestBody Map<String, Object> orderData, HttpSession session) {
	    // Check if user is logged in
	    Integer userId = (Integer) session.getAttribute("userId");
	    if (userId == null) {
	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
	                .body(Map.of("message", "Please login to save an order"));
	    }

	    try {
	        User user = userService.getUserById(userId);
	        if (user == null) {
	            return ResponseEntity.status(HttpStatus.NOT_FOUND)
	                    .body(Map.of("error", "User not found"));
	        }

	        // Use the service to save the order
	        List<OrderItem> savedOrders = orderService.saveOrder(orderData, user);
	        
	        // Clear the user's cart
	        cartService.clearCart(userId);
	        
	        return ResponseEntity.ok(Map.of(
	                "orderId", savedOrders.get(0).getId(),
	                "totalOrders", savedOrders.size(),
	                "message", "Orders placed successfully!"
	            ));
	    } catch (Exception e) {
	        e.printStackTrace();
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body(Map.of("error", "Failed to save order: " + e.getMessage()));
	    }
	}

	
}
