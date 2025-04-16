package com.clickshop.service;

import java.util.List;
import java.util.Map;

import com.clickshop.entity.OrderItem;
import com.clickshop.entity.User;


public interface OrderService {
	
	public List<OrderItem> getOrderHistoryByUserId(int userId);
	String checkout(int userId);
	public List<OrderItem> getRecentOrders(int limit);
	public List<OrderItem> getAllOrders();
	public OrderItem getOrderById(int id);
	public boolean updateOrderStatus(int orderId, String newStatus);
	public List<OrderItem> getOrdersByStatus(String status);
	public boolean cancelOrder(int orderId) ;
	public Map<String, Object> prepareOrder(Map<String, Object> orderRequest, User user);
	public List<OrderItem> saveOrder(Map<String, Object> orderData, User user) ;
	

}
