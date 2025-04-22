package com.clickshop.controller;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.clickshop.entity.OrderItem;
import com.clickshop.entity.User;
import com.clickshop.security.SecurityUtils;
import com.clickshop.service.CartService;
import com.clickshop.service.OrderService;
import com.clickshop.service.UserService;

@RestController
@RequestMapping("/orders")
public class OrderController {
	

	@Autowired
	private OrderService orderService;
	
	@Autowired
	private UserService userService;

	@Autowired
	private CartService cartService;

	@Autowired
	private SecurityUtils securityUtils;

	@GetMapping("/all")
    public ResponseEntity<?> getAllOrders() {
        
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
	public ResponseEntity<?> getOrderById(@PathVariable int id) {
	
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
	        @RequestBody Map<String, String> statusUpdate
	        ) {

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
    public ResponseEntity<?> cancelOrder(@PathVariable int orderId) {
        try {
            boolean cancelled = orderService.cancelOrder(orderId);
            if (cancelled) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "Order cancelled successfully");
                return ResponseEntity.ok(response);
            } else {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Order cannot be cancelled. It may be already shipped or delivered.");
                return ResponseEntity.badRequest().body(errorResponse);
            }
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

	@GetMapping("/between")
    public List<OrderItem> getOrdersBetweenDates(
            @RequestParam("startDate") @DateTimeFormat(pattern = "yyyy-MM-dd") Date  startDate,
            @RequestParam("endDate") @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate) {
        return orderService.getOrdersBetweenDates(startDate, endDate);
    }

	/**
	 * Get orders by status
	 */
	@GetMapping("/status/{status}")
	public ResponseEntity<?> getOrdersByStatus(
	        @PathVariable String status) {
	  
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
    public ResponseEntity<?> createOrder(@RequestBody Map<String, Object> orderRequest) {
        // Check if user is logged in
        int userId = securityUtils.getCurrentUserId();
        
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
	public ResponseEntity<?> saveOrder(@RequestBody Map<String, Object> orderData) {
	    // Check if user is logged in
	    int userId = securityUtils.getCurrentUserId();
	
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
