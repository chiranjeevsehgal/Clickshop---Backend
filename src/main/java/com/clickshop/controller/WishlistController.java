package com.clickshop.controller;


import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.clickshop.entity.User;
import com.clickshop.entity.Wishlist;
import com.clickshop.security.SecurityUtils;
import com.clickshop.service.UserService;
import com.clickshop.service.WishlistService;


@RestController
@RequestMapping("/wishlist")
public class WishlistController  {
	
	@Autowired
    private WishlistService wishlistService;
	
    @Autowired
    private UserService userService;

    @Autowired
    private SecurityUtils securityUtils;

    @GetMapping("")
    @ResponseBody
    public ResponseEntity<?> getWishlist() {
        try{
            int userId = securityUtils.getCurrentUserId();
            User user = userService.getUserById(userId);
            List<Wishlist> wishlist = wishlistService.getWishlistByUser(user);
            return ResponseEntity.ok(wishlist);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch wishlist"));
        }
    }

    @PostMapping("/add/{productId}")
    public ResponseEntity<?> addToWishlist(@PathVariable("productId") int productId) {
        int uid = securityUtils.getCurrentUserId();
        
        try {
            User user = userService.getUserById(uid);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "User not found"));
            }
            Wishlist wishlistItem = wishlistService.addToWishlist(user, productId);
            return ResponseEntity.status(HttpStatus.CREATED).body(wishlistItem);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to add product to wishlist"));
        }
    }

    @DeleteMapping("/remove/{productId}")
    public ResponseEntity<?> removeFromWishlist(@PathVariable("productId") int productId) {
        
        try {
            int uid = securityUtils.getCurrentUserId();
            User user = userService.getUserById(uid);
            wishlistService.removeFromWishlist(user, productId);
            return ResponseEntity.ok(Map.of("deleted", true));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to remove product from wishlist"));
        }
    }

    @GetMapping("/check/{productId}")
    public ResponseEntity<?> checkProductInWishlist(@PathVariable("productId") int productId) {
        
        try {
            int uid = securityUtils.getCurrentUserId();
            User user = userService.getUserById(uid);
            boolean inWishlist = wishlistService.isProductInWishlist(user, productId);
            return ResponseEntity.ok(Map.of("inWishlist", inWishlist));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to check wishlist status"));
        }
    }

    @DeleteMapping("/clear")
    public ResponseEntity<?> clearWishlist() {
        
        try {
            int uid = securityUtils.getCurrentUserId();
            User user = userService.getUserById(uid);
            
            wishlistService.clearWishlistByUser(user);
            return ResponseEntity.ok(Map.of("cleared", true));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to clear wishlist"));
        }
    }
}
