package com.clickshop.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.clickshop.entity.Cart;
import com.clickshop.entity.Product;
import com.clickshop.entity.User;
import com.clickshop.repository.CartRepository;
import com.clickshop.repository.ProductRepository;
import com.clickshop.repository.UserRepository;

import jakarta.transaction.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;


@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    public List<Map<String, Object>> getAllCartItems() {
        List<Cart> cartItems = cartRepository.findAll();
        return transformCartItems(cartItems);
    }

    @Override
    public List<Map<String, Object>> getCartItemsByUserId(int userId) {
        List<Cart> cartItems = cartRepository.findByUserId(userId);
        return transformCartItems(cartItems);
    }

    public List<Map<String, Object>> transformCartItems(List<Cart> cartItems) {
        List<Map<String, Object>> result = new ArrayList<>();

        for (Cart cart : cartItems) {
            Map<String, Object> cartMap = new HashMap<>();
            cartMap.put("cartId", cart.getCartId());
            cartMap.put("userId", cart.getUser().getId());
            cartMap.put("productId", cart.getProduct().getId());
            cartMap.put("productName", cart.getProduct().getName());
            cartMap.put("quantity", cart.getQuantity());
            cartMap.put("totalPrice", cart.getQuantity() * cart.getProduct().getPrice());
//            cartMap.put("orderDate", cart.getOrderDate());
            result.add(cartMap);
        }

        return result;
    }

    @Override
	@Transactional
	public String addToCart(int userId, int productId, int quantity) {
    	 Optional<User> userOpt = userRepository.findById(userId);
         Optional<Product> productOpt = productRepository.findById(productId);

         if (!userOpt.isPresent()) {
             return "User not found!";
         }
         if (!productOpt.isPresent()) {
             return "Product not found!";
         }

         User user = userOpt.get();
         Product product = productOpt.get();

         // Check if item already exists in cart
         Cart existingCart = cartRepository.findByUserIdAndProductId(userId, productId);
         if (existingCart != null) {
             existingCart.setQuantity(existingCart.getQuantity() + quantity);
             cartRepository.save(existingCart);
             return "Cart updated successfully!";
         }

         // New cart entry
         Cart newCart = new Cart();
         newCart.setUser(user);
         newCart.setProduct(product);
         newCart.setQuantity(quantity);

         cartRepository.save(newCart);
         return "Item added to cart successfully!";
     }
    
    @Transactional
    @Override
    public String removeCartItem(int cartId) {
        cartRepository.deleteById(cartId);
        return "Item removed from cart successfully!";
    }
    
    @Override
    public void updateCartItemQuantity(int itemId, int quantity) {
        Cart cartItem = cartRepository.findById(itemId)
            .orElseThrow(() -> new NoSuchElementException("Cart item not found"));

        cartItem.setQuantity(quantity);
        cartRepository.save(cartItem);
    }
}
