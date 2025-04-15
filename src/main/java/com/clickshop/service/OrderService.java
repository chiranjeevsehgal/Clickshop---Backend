package com.clickshop.service;

import java.util.List;

import com.clickshop.entity.OrderItem;


public interface OrderService {
	
	public List<OrderItem> getOrderHistoryByUserId(int userId);
	String checkout(int userId);
	
	public List<OrderItem> getRecentOrders(int limit);

}
