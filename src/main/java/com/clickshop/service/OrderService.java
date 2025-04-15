package com.clickshop.service;

import java.util.List;

import com.clickshop.entity.OrderItem;


public interface OrderService {
	
	public List<OrderItem> getOrderHistoryByUserId(int userId);
	String checkout(int userId);
	public List<OrderItem> getRecentOrders(int limit);
	public List<OrderItem> getAllOrders();
	public OrderItem getOrderById(int id);
	public boolean updateOrderStatus(int orderId, String newStatus);
	public List<OrderItem> getOrdersByStatus(String status);
	public boolean cancelOrder(int orderId) ;

}
