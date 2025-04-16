package com.clickshop.entity;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

@Entity
@Table(name = "orders")
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private int orderId;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonIgnore // Prevents serialization of the user field
    private User user;

    @ManyToOne
    @JoinColumn(name = "product_id") 
    private Product product;

    @Override
	public String toString() {
		return "OrderItem [orderId=" + orderId + ", user=" + user + ", product=" + product + ", quantity=" + quantity
				+ ", totalPrice=" + totalPrice + ", orderDate=" + orderDate + ", orderStatus=" + orderStatus
				+ ", paymentId=" + paymentId + ", paymentStatus=" + paymentStatus + ", formattedDate=" + formattedDate
				+ ", subtotal=" + subtotal + ", shipping=" + shipping + ", discount=" + discount + "]";
	}

	@Column(name = "quantity")
    private int quantity;

    @Column(name = "total_price")
    private double totalPrice;

    @Column(name = "order_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date orderDate;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name="status")
    private OrderStatus orderStatus;
    
    public enum OrderStatus {
        PROCESSING, CANCELLED, SHIPPED, DELIVERED
    };
    
    // Added for Razorpay integration
    @Column(name = "payment_id")
    private String paymentId;
    
    @Column(name = "payment_status")
    private String paymentStatus;
    
    // For formatting display date
    @Column(name = "formatted_date")
    private String formattedDate;
    
    private Double subtotal;
    
    private Double shipping;
    
    private Double discount;

    public Double getSubtotal() {
		return subtotal;
	}

	public void setSubtotal(Double subtotal) {
		this.subtotal = subtotal;
	}

	public Double getShipping() {
		return shipping;
	}

	public void setShipping(Double shipping) {
		this.shipping = shipping;
	}

	public Double getDiscount() {
		return discount;
	}

	public void setDiscount(Double discount) {
		this.discount = discount;
	}

	// Constructors
    public OrderItem() {
    }

    public OrderItem(User user, Product product, int quantity, float totalPrice, String formattedDate) {
        this.user = user;
        this.product = product;
        this.quantity = quantity;
        this.totalPrice = totalPrice;
        this.orderDate = new Date();
        this.formattedDate = formattedDate;
        this.orderStatus = OrderStatus.PROCESSING;
    }

    // Getters and Setters
    public int getId() {
        return orderId;
    }

    public void setId(int id) {
        this.orderId = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double itemSubtotal) {
        this.totalPrice = itemSubtotal;
    }

    public Date getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(Date orderDate) {
        this.orderDate = orderDate;
    }

    public OrderStatus getOrderStatus() {
        return orderStatus;
    }
    
    public void setOrderStatus(OrderStatus status) {
        this.orderStatus = status;
    }
    
    public String getPaymentId() {
        return paymentId;
    }
    
    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }
    
    public String getPaymentStatus() {
        return paymentStatus;
    }
    
    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getFormattedDate() {
        return formattedDate;
    }

    public void setFormattedDate(String formattedDate) {
        this.formattedDate = formattedDate;
    }
}