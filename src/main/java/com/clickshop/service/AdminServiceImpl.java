package com.clickshop.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.clickshop.repository.AdminRepository;
import com.clickshop.repository.OrderRepository;
import com.clickshop.repository.ProductRepository;
import com.clickshop.repository.UserRepository;

@Service
public class AdminServiceImpl implements AdminService {
	
	@Autowired
	AdminRepository adminRepository;

	@Autowired
	UserRepository userRepository;

	@Autowired
	ProductRepository productRepository;

	@Autowired
	OrderRepository orderRepository;
	
	public Map<String, Object> getDashboardStats() {
	    Map<String, Object> stats = new HashMap<>();
	    stats.put("userCount", userRepository.count());
	    stats.put("productCount", productRepository.count());
	    stats.put("orderCount", orderRepository.count());
	    stats.put("totalRevenue", orderRepository.sumTotalRevenue());
	    return stats;
	}

}
