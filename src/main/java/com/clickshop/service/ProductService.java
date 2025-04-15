package com.clickshop.service;

import java.util.List;

import com.clickshop.entity.Product;

public interface ProductService {
	boolean addProduct(Product product);

//	ArrayList<Product> displayProducts();

	boolean deleteProduct(int pno);

	public boolean updateProductField(int productId, String field, String value);

	public List<Product> getAllProducts();

	public Product getProductByIdService(int pid);
}
