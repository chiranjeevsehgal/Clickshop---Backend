package com.clickshop.service;

import java.time.LocalDate;
import java.util.List;

import com.clickshop.entity.Product;

public interface ProductService {
	Product addProduct(Product product);

	boolean deleteProduct(int pno);

	public boolean updateProduct(Product updatedProduct);

	public List<Product> getAllProducts();
	
	public Product getProductByIdService(int pid);
	
	public boolean reduceStock(int productId, int quantity) ;
	
	public List<Product> getProductsByCategory(String category);

	public List<Product> searchProducts(String searchTerm);

	public List<Product> findPopularProducts();

	public List<String> findAllCategories();
	
	public List<Product> getProductsBetweenDates(LocalDate startDate, LocalDate endDate);



}
