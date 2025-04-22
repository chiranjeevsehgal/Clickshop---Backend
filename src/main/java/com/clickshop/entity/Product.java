package com.clickshop.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "products")
public class Product {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pid")
	private int id;
	private String name, description, category;

	@Column(name = "image_url")
	private String imageUrl;

	private float price;
	private int stock;

	@Column(name = "created_at")
	private LocalDateTime createdAt;

	private boolean featured = false;

	public Product(int id, String name, String description, String category, String imageUrl, float price, int stock, boolean featured, LocalDateTime createdAt) {
		this.id = id;
		this.name = name;
		this.description = description;
		this.category = category;
		this.imageUrl = imageUrl;
		this.price = price;
		this.stock = stock;
		this.featured = featured;
		this.createdAt = createdAt;

	}

	public Product() {
		
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public LocalDateTime getCreatedAt() { 
		return createdAt; 
	}
	
	public void setCreatedAt(LocalDateTime createdAt) { 
		this.createdAt = createdAt; 
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public float getPrice() {
		return price;
	}

	public void setPrice(float price) {
		this.price = price;
	}

	public int getStock() {
		return stock;
	}

	public void setStock(int stock) {
		this.stock = stock;
	}

	public String toString() {
		return "ID: " + id + " | " + name + " | Price: " + price + " | Description: " + description + " | Stock: "
				+ stock + " | Category: " + category + " | Image Url: " + imageUrl + " | Created At: " + createdAt;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}

	public boolean isFeatured() {
		return featured;
	}

	public void setFeatured(boolean featured) {
		this.featured = featured;
	}
}