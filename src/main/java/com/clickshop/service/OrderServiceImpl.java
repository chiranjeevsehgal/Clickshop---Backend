package com.clickshop.service;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.clickshop.entity.OrderItem.OrderStatus;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.clickshop.entity.Cart;
import com.clickshop.entity.OrderItem;
import com.clickshop.entity.Product;
import com.clickshop.entity.User;
import com.clickshop.repository.CartRepository;
import com.clickshop.repository.OrderRepository;
import com.clickshop.repository.ProductRepository;
import com.clickshop.repository.UserRepository;

import jakarta.transaction.Transactional;

@Service
public class OrderServiceImpl implements OrderService {

	@Autowired
	OrderRepository orderRepository;

	@Autowired
	private CartRepository cartRepository;

	@Autowired
	private ProductRepository productRepository;

	@Autowired
	private UserRepository userRepository;

	@Override
	public List<OrderItem> getOrderHistoryByUserId(int userId) {
		return orderRepository.findByUserId(userId);
	}

	@Transactional
	@Override
	public String checkout(int userId) {
		Optional<User> userOpt = userRepository.findById(userId);
		if (!userOpt.isPresent()) {
			return "User not found!";
		}

		List<Cart> cartItems = cartRepository.findByUserId(userId);
		if (cartItems.isEmpty()) {
			return "Cart is empty!";
		}

		for (Cart cart : cartItems) {
			Product product = cart.getProduct();
			if (product.getStock() < cart.getQuantity()) {
				return "Insufficient stock for product: " + product.getName();
			}

			// Deduct stock
			product.setStock(product.getStock() - cart.getQuantity());
			productRepository.save(product);

			// Create Order Entry
			String formattedDate = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date());
			OrderItem order = new OrderItem(cart.getUser(), product, cart.getQuantity(),
					cart.getQuantity() * product.getPrice(), formattedDate);
			orderRepository.save(order);
			
		}

		// Clear Cart
		cartRepository.deleteAll(cartItems);

		return "Order placed successfully!";
	}
	
	@Override
	public List<OrderItem> getRecentOrders(int limit) {

		Pageable pageable = PageRequest.of(0, limit);
	    Page<OrderItem> page = orderRepository.findAllByOrderByOrderDateDesc(pageable);
	    return page.getContent();
    }
	
	@Override
	public List<OrderItem> getAllOrders() {
        return orderRepository.findAll(Sort.by(Sort.Direction.DESC, "orderDate"));
    }
	
	@Override
	public OrderItem getOrderById(int id) {
	    return orderRepository.findById(id)
	            .orElseThrow(() -> new NoSuchElementException("Order not found with id: " + id));
	}
	
	 /**
     * Get orders by status
     */
    public List<OrderItem> getOrdersByStatus(String status) {
        try {
            OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
            return orderRepository.findByOrderStatus(orderStatus);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status: " + status + 
                ". Valid statuses are: PROCESSING, CANCELLED, SHIPPED, DELIVERED");
        }
    }
    
    /**
     * Update order status
     */
    @Transactional
    @Override
    public boolean updateOrderStatus(int orderId, String newStatus) {
        try {
            OrderItem orderItem = orderRepository.findById(orderId)
                    .orElseThrow(() -> new NoSuchElementException("Order not found with ID: " + orderId));
            
            OrderStatus status = OrderStatus.valueOf(newStatus.toUpperCase());
            
            // Don't allow status changes for cancelled or delivered orders
            if (orderItem.getOrderStatus() == OrderStatus.CANCELLED) {
                throw new IllegalStateException("Cannot change status of a cancelled order");
            }
            if (orderItem.getOrderStatus() == OrderStatus.DELIVERED) {
                throw new IllegalStateException("Cannot change status of a delivered order");
            }
            
            // Update the status
            orderItem.setOrderStatus(status);
            orderRepository.save(orderItem);
            return true;
        } catch (NoSuchElementException e) {
            return false;
        }
    }
    
    /**
     * Cancel an order and restore product stock
     */
    @Transactional
    @Override
    public boolean cancelOrder(int orderId) {
        try {
            OrderItem orderItem = orderRepository.findById(orderId)
                    .orElseThrow(() -> new NoSuchElementException("Order not found with ID: " + orderId));
            
            // Can't cancel already delivered orders
            if (orderItem.getOrderStatus() == OrderStatus.DELIVERED) {
                throw new IllegalStateException("Cannot cancel a delivered order");
            }
            
            // Don't cancel already cancelled orders
            if (orderItem.getOrderStatus() == OrderStatus.CANCELLED) {
                return true; // Already cancelled, return success
            }
            
            // Restore product stock
            Product product = orderItem.getProduct();
            if (product != null) {
                product.setStock(product.getStock() + orderItem.getQuantity());
                productRepository.save(product);
            }
            
            // Update order status
            orderItem.setOrderStatus(OrderStatus.CANCELLED);
            orderRepository.save(orderItem);
            
            return true;
        } catch (NoSuchElementException e) {
            return false;
        }
    }

        /**
     * Prepares order data before payment
     */
    @Override
    public Map<String, Object> prepareOrder(Map<String, Object> orderRequest, User user) {
        String orderId = "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        
        Map<String, Object> orderData = new HashMap<>();
        orderData.put("orderId", orderId);
        orderData.put("userId", user.getId());
        orderData.put("items", orderRequest.get("items"));
        orderData.put("subtotal", orderRequest.get("subtotal"));
        orderData.put("shipping", orderRequest.get("shipping"));
        orderData.put("discount", orderRequest.get("discount"));
        orderData.put("total", orderRequest.get("total"));
        orderData.put("status", "PENDING");
        orderData.put("createdAt", LocalDateTime.now());
        
        return orderData;
    }

        /**
     * Saves the order after successful payment
     */
    @Override
    @Transactional
    public List<OrderItem> saveOrder(Map<String, Object> orderData, User user) {
        try {
            // Extract payment information
            String paymentId = (String) orderData.get("paymentId");
            String paymentStatus = (String) orderData.get("paymentStatus");
            
            
            // Extract all items from cart
            Map<String, Object> usertemp = (Map<String, Object>) orderData.get("user");
            List<Map<String, Object>> items = (List<Map<String, Object>>) usertemp.get("cart");
            
            if (items == null || items.isEmpty()) {
                throw new RuntimeException("No items found in order");
            }
            System.out.println("In order save");
            System.out.println(orderData);
            List<OrderItem> savedOrders = new ArrayList<>();
            SimpleDateFormat formatter = new SimpleDateFormat("MMM dd, yyyy");
            Date currentDate = new Date();
            String formattedDate = formatter.format(currentDate);
            
            int t=0;
            // Process each item in the cart
            for (Map<String, Object> item : items) {
                // Get the product
            	Map<String, Object> itemtemp = (Map<String, Object>) item.get("product");
            	System.out.println("Item"+(t+1));
            	System.out.println(item);
            	System.out.println(itemtemp);
                int productId = getIntValue(itemtemp, "id");
                Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new RuntimeException("Product not found with ID: " + productId));

                // Create an order for this product
                OrderItem order = new OrderItem();
                order.setUser(user);
                order.setProduct(product);
                order.setQuantity(getIntValue(item, "quantity"));
                
                // Calculate item total price (price * quantity)
                float itemPrice = getFloatValue(itemtemp, "price");
                double itemSubtotal = itemPrice * order.getQuantity();

                // Set order date
                order.setOrderDate(currentDate);
                order.setFormattedDate(formattedDate);

                // Set payment information
                order.setPaymentId(paymentId);
                order.setPaymentStatus(paymentStatus);

                // Set financial details (pro-rated for each item)
                order.setSubtotal(itemSubtotal);
                
                // Calculate proportional shipping and discount
                // double totalAmount = getDoubleValue(orderData, "amount");
                double shippingTotal = 99.0;
                double discountTotal = 0;
                double discountRate = 0;
                discountTotal = getDoubleValue(orderData, "discount");
                discountRate = getDoubleValue(orderData, "discountRate");

                order.setShipping(shippingTotal);
                
                if (discountTotal != 0) {
                    // Calculate proportional shipping and discount based on item's share of total
                    order.setDiscount(itemSubtotal*discountRate);
                } else {
                    order.setDiscount(0.0);
                }
                order.setTotalPrice(itemSubtotal+shippingTotal-discountTotal);
                // Set status to PROCESSING
                order.setOrderStatus(OrderItem.OrderStatus.PROCESSING);

                // Save the order
                OrderItem savedOrder = orderRepository.save(order);
                savedOrders.add(savedOrder);
            }

            return savedOrders;
        } catch (Exception e) {
            throw new RuntimeException("Error saving orders: " + e.getMessage(), e);
        }
    }
    
    /**
     * Helper method to safely get an integer value from a map
     */
    private int getIntValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            return 0;
        }
        
        if (value instanceof Integer) {
            return (Integer) value;
        } else if (value instanceof Long) {
            return ((Long) value).intValue();
        } else if (value instanceof Double) {
            return ((Double) value).intValue();
        } else if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        
        return 0;
    }
    
    /**
     * Helper method to safely get a float value from a map
     */
    private float getFloatValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            return 0.0f;
        }
        
        if (value instanceof Float) {
            return (Float) value;
        } else if (value instanceof Double) {
            return ((Double) value).floatValue();
        } else if (value instanceof Integer) {
            return ((Integer) value).floatValue();
        } else if (value instanceof Long) {
            return ((Long) value).floatValue();
        } else if (value instanceof String) {
            try {
                return Float.parseFloat((String) value);
            } catch (NumberFormatException e) {
                return 0.0f;
            }
        }
        
        return 0.0f;
    }
    
    /**
     * Helper method to safely get a double value from a map
     */
    private double getDoubleValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            return 0.0;
        }
        
        if (value instanceof Double) {
            return (Double) value;
        } else if (value instanceof Integer) {
            return ((Integer) value).doubleValue();
        } else if (value instanceof Long) {
            return ((Long) value).doubleValue();
        } else if (value instanceof Float) {
            return ((Float) value).doubleValue();
        } else if (value instanceof String) {
            try {
                return Double.parseDouble((String) value);
            } catch (NumberFormatException e) {
                return 0.0;
            }
        }
        
        return 0.0;
    }    
   

}
