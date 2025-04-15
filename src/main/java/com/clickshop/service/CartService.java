package com.clickshop.service;

import java.util.List;
import java.util.Map;

import com.clickshop.entity.Cart;

public interface CartService {
	public List<Map<String, Object>> getAllCartItems();
	
	public List<Map<String, Object>> getCartItemsByUserId(int userId);
	
	public List<Map<String, Object>> transformCartItems(List<Cart> cartItems) ;	
	
	public String addToCart(int userId, int productId, int quantity) ;
	
    String removeCartItem(int cartId);
    
    void updateCartItemQuantity(int itemId, int quantity);

}