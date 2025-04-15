package com.clickshop.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.clickshop.service.CartService;
import com.clickshop.service.ProductService;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/cart")
public class CartController {
	
	@Autowired
    private ProductService productService;
	
    @Autowired
    private CartService cartService;

	
    @PostMapping("/add")
    public ResponseEntity<Map<String, String>> addToCart(@RequestBody Map<String, Object> payload, HttpSession session) {
        try {
            System.out.println(payload);
            int userId = (int) session.getAttribute("userId");
            int productId = (int) payload.get("productId");
            int quantity = (int) payload.get("quantity");

            String result = cartService.addToCart(userId, productId, quantity);

            return ResponseEntity.ok(Map.of("message", "Item added to cart successfully."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Error adding to cart: " + e.getMessage()));
        }
    }
	
	  @DeleteMapping("/remove/{cartId}")
	    public ResponseEntity<Map<String, String>> removeCartItem(@PathVariable  int cartId) {
	        System.out.println(cartId);
	    	String response = cartService.removeCartItem(cartId);
	        Map<String, String> json = new HashMap<>();
	        json.put("message", response);
	        return ResponseEntity.ok(json);

	    }
	  
	  @PutMapping("/update/{itemId}")
	    public ResponseEntity<?> updateQuantity(@PathVariable int itemId, @RequestBody Map<String, Integer> payload) {
	        try {
	        	System.out.println("In update quantity controller");
	        	System.out.println(itemId);
	            int quantity = payload.get("quantity");
	            cartService.updateCartItemQuantity(itemId, quantity);
	            return ResponseEntity.ok().body(Map.of("message", "Quantity updated successfully"));
	        } catch (NoSuchElementException e) {
	            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Item not found"));
	        } catch (Exception e) {
	            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Failed to update quantity"));
	        }
	    }

}
