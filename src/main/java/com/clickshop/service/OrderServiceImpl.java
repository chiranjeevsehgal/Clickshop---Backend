package com.clickshop.service;

import java.text.SimpleDateFormat;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import com.clickshop.entity.OrderItem.Order_Status;


import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;


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
		// TODO Auto-generated method stub
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
            Order_Status orderStatus = Order_Status.valueOf(status.toUpperCase());
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
    public boolean updateOrderStatus(int orderId, String newStatus) {
        try {
            OrderItem orderItem = orderRepository.findById(orderId)
                    .orElseThrow(() -> new NoSuchElementException("Order not found with ID: " + orderId));
            
            Order_Status status = Order_Status.valueOf(newStatus.toUpperCase());
            
            // Don't allow status changes for cancelled or delivered orders
            if (orderItem.getOrderStatus() == Order_Status.CANCELLED) {
                throw new IllegalStateException("Cannot change status of a cancelled order");
            }
            if (orderItem.getOrderStatus() == Order_Status.DELIVERED) {
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
    public boolean cancelOrder(int orderId) {
        try {
            OrderItem orderItem = orderRepository.findById(orderId)
                    .orElseThrow(() -> new NoSuchElementException("Order not found with ID: " + orderId));
            
            // Can't cancel already delivered orders
            if (orderItem.getOrderStatus() == Order_Status.DELIVERED) {
                throw new IllegalStateException("Cannot cancel a delivered order");
            }
            
            // Don't cancel already cancelled orders
            if (orderItem.getOrderStatus() == Order_Status.CANCELLED) {
                return true; // Already cancelled, return success
            }
            
            // Restore product stock
            Product product = orderItem.getProduct();
            if (product != null) {
                product.setStock(product.getStock() + orderItem.getQuantity());
                productRepository.save(product);
            }
            
            // Update order status
            orderItem.setOrderStatus(Order_Status.CANCELLED);
            orderRepository.save(orderItem);
            
            return true;
        } catch (NoSuchElementException e) {
            return false;
        }
    }
    
   

    

}
