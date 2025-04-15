package com.clickshop.service;

import java.text.SimpleDateFormat;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import java.util.Date;
import java.util.List;
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
	
	public List<OrderItem> getRecentOrders(int limit) {

		Pageable pageable = PageRequest.of(0, limit);
	    Page<OrderItem> page = orderRepository.findAllByOrderByOrderDateDesc(pageable);
	    return page.getContent();
    }

}
