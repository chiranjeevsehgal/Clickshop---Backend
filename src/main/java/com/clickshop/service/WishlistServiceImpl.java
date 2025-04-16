package com.clickshop.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.clickshop.entity.Product;
import com.clickshop.entity.User;
import com.clickshop.entity.Wishlist;
import com.clickshop.repository.ProductRepository;
import com.clickshop.repository.WishlistRepository;

import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;


@Service
public class WishlistServiceImpl implements WishlistService {

    @Autowired
    private WishlistRepository wishlistRepository;
    
    @Autowired
    private ProductRepository productRepository;

    @Override
    public List<Wishlist> getWishlistByUser(User user) {
        return wishlistRepository.findByUser(user);
    }

    @Override
    @Transactional
    public void clearWishlistByUser(User user) {
        List<Wishlist> userWishlist = wishlistRepository.findByUser(user);
        if (!userWishlist.isEmpty()) {
            wishlistRepository.deleteAll(userWishlist);
        }
    }

    @Override
    public Wishlist addToWishlist(User user, int productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        
        // Check if product already exists in wishlist
        if(wishlistRepository.existsByUserAndProduct(user, product)) {
            throw new RuntimeException("Product already in wishlist");
        }
        
        Wishlist wishlist = new Wishlist(user, product);
        return wishlistRepository.save(wishlist);
    }

    @Override
    @Transactional
    public void removeFromWishlist(User user, int productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        
        Optional<Wishlist> wishlistItem = wishlistRepository.findByUserAndProduct(user, product);
        
        if (!wishlistItem.isPresent()) {
            throw new RuntimeException("Product not found in wishlist");
        }
        
        wishlistRepository.deleteById(wishlistItem.get().getId());
    }

    @Override
    public boolean isProductInWishlist(User user, int productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        
        return wishlistRepository.existsByUserAndProduct(user, product);
    }
}
