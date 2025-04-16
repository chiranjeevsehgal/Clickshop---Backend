package com.clickshop.service;

import java.util.List;

import com.clickshop.entity.User;
import com.clickshop.entity.Wishlist;

public interface WishlistService {
	
	public List<Wishlist> getWishlistByUser(User user) ;
	public Wishlist addToWishlist(User user, int productId);
	public void removeFromWishlist(User user, int productId);
	public boolean isProductInWishlist(User user, int productId) ;
	public void clearWishlistByUser(User user);
}