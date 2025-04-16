package com.clickshop.service;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.clickshop.entity.Product;
import com.clickshop.entity.User;
import com.clickshop.entity.Wishlist;
import com.clickshop.repository.ProductRepository;
import com.clickshop.repository.WishlistRepository;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;

import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;


@Service
public class PaymentServiceImpl implements PaymentService {

    @Value("${razorpay.key.id}")
    private String razorpayKeyId;
    
    @Value("${razorpay.key.secret}")
    private String razorpayKeySecret;

    /**
     * Creates a new order in Razorpay for payment processing
     */
    @Override
    public Map<String, Object> createRazorpayOrder(Map<String, Object> orderRequest, User user) {
        
        // Extract order total
        Map<String, Object> orderSummary = extractOrderSummary(orderRequest);
        System.out.println("HI");
        System.out.println(orderRequest);
        System.out.println(orderSummary);
        double amount = (double) orderSummary.get("total");
        
        // Convert to paise (Razorpay uses smallest currency unit)
        int amountInPaise = convertToPaise(amount);
        
        // Generate a receipt ID
        String receiptId = UUID.randomUUID().toString();
        
        // Create Razorpay order
        RazorpayClient razorpayClient = null;
        try {
            razorpayClient = new RazorpayClient(razorpayKeyId, razorpayKeySecret);
        } catch (RazorpayException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        JSONObject orderOptions = new JSONObject();
        orderOptions.put("amount", amountInPaise);
        orderOptions.put("currency", "INR");
        orderOptions.put("receipt", receiptId);
        orderOptions.put("payment_capture", 1); // Auto-capture payment
        orderOptions.put("receipt", "receipt_" + System.currentTimeMillis());
        
        // Create order in Razorpay
        Order razorpayOrder = null;
        try {
            razorpayOrder = razorpayClient.orders.create(orderOptions);
        } catch (RazorpayException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        // Create internal order in database and get order ID
        String orderId = createInternalOrder(orderRequest, user, razorpayOrder.get("id"));
        
        // Prepare response
        Map<String, Object> response = new HashMap<>();
        response.put("orderId", orderId);
        response.put("razorpayOrderId", razorpayOrder.get("id"));
        response.put("amount", amountInPaise);
        response.put("currency", "INR");
        response.put("keyId", razorpayKeyId);
        
        return response;
    }
    
    /**
     * Verifies the payment signature from Razorpay
     */
    @Override
    public boolean verifyRazorpayPayment(Map<String, String> paymentData) {
        try {
            // Extract payment verification data
        	System.out.println(paymentData);
            String razorpayOrderId = paymentData.get("razorpayOrderId");
            String razorpayPaymentId = paymentData.get("razorpayPaymentId");
            String razorpaySignature = paymentData.get("razorpaySignature");
            
            // Verify signature
            String data = razorpayOrderId + "|" + razorpayPaymentId;
            boolean isValidSignature = Utils.verifySignature(data, razorpaySignature, razorpayKeySecret);
            
            if (isValidSignature) {
                // Update order status in database
                updateOrderPaymentStatus(paymentData.get("orderId"), razorpayPaymentId, "COMPLETED");
                return true;
            }
            
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Helper method to extract order summary from order request
     */
    private Map<String, Object> extractOrderSummary(Map<String, Object> orderRequest) {
        Map<String, Object> summary = new HashMap<>();
        
        // Extract values with null checks
        Object subtotalObj = orderRequest.getOrDefault("subtotal", 0.0);
        Object shippingObj = orderRequest.getOrDefault("shipping", 0.0);
        Object discountObj = orderRequest.getOrDefault("discount", 0.0);
        
        // Calculate total
        double subtotal = convertToDouble(subtotalObj);
        double shipping = convertToDouble(shippingObj);
        double discount = convertToDouble(discountObj);
        double total = subtotal + shipping - discount;
        
        summary.put("total", total);
        return summary;
    }
    
    private double convertToDouble(Object value) {
        if (value == null) {
            return 0.0;
        }
        
        if (value instanceof Double) {
            return (Double) value;
        } else if (value instanceof Integer) {
            return ((Integer) value).doubleValue();
        } else if (value instanceof Long) {
            return ((Long) value).doubleValue();
        } else if (value instanceof Float) {
            return ((Float) value).doubleValue();
        } else if (value instanceof String) {
            try {
                return Double.parseDouble((String) value);
            } catch (NumberFormatException e) {
                return 0.0;
            }
        }
        
        // For any other type, try to convert to string first then parse
        try {
            return Double.parseDouble(String.valueOf(value));
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
    
    /**
     * Helper method to convert amount to paise (smallest currency unit)
     */
    private int convertToPaise(double amount) {
        BigDecimal bd = new BigDecimal(amount);
        bd = bd.setScale(2, RoundingMode.HALF_UP);
        bd = bd.multiply(new BigDecimal(100));
        return bd.intValue();
    }
    
    /**
     * Helper method to create an internal order in database
     * This would typically involve your order service
     */
    private String createInternalOrder(Map<String, Object> orderRequest, User user, String razorpayOrderId) {
        // This is a placeholder implementation
        // In a real application, you would use OrderService to create the order
        // For now, just generate and return an order ID
        
        // Generate a unique order ID
        String orderId = "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        
        // Here you would typically save the order to your database
        // orderService.createOrder(orderDetails, user, orderId, razorpayOrderId);
        
        return orderId;
    }
    
    /**
     * Helper method to update order payment status in database
     */
    private void updateOrderPaymentStatus(String orderId, String paymentId, String status) {
        // This is a placeholder implementation
        // In a real application, you would use OrderService to update the order
        
        // orderService.updateOrderPaymentStatus(orderId, paymentId, status);
    }

}
