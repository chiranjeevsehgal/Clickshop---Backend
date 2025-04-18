package com.clickshop.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.clickshop.entity.Product;
import com.clickshop.repository.ProductRepository;

@Service
public class ProductServiceImpl implements ProductService {
	Scanner sc = new Scanner(System.in);

	@Autowired
	ProductRepository productRepository;

	@Override
	public Product addProduct(Product product) {
		// TODO Auto-generated method stub
		Product product1 = productRepository.save(product);
		if (product1 != null)
			return product1;
		else {
			return null;
		}
	}

	@Override
	public boolean deleteProduct(int pno) {
		// TODO Auto-generated method stub
		if (productRepository.existsById(pno)) {
			productRepository.deleteById(pno);
			if (productRepository.existsById(pno)) {
				System.out.println("Product could not be deleted");
				return false;
			} else {
				return true;
			}
		} else {
			System.out.println("No product with id " + pno);
			return false;
		}
	}

	public boolean updateProduct(Product updatedProduct) {
	    Optional<Product> optionalProduct = productRepository.findById(updatedProduct.getId());
	    
	    if (optionalProduct.isPresent()) {
	        Product existingProduct = optionalProduct.get();
	        
	        // Update all fields
	        existingProduct.setName(updatedProduct.getName());
	        existingProduct.setDescription(updatedProduct.getDescription());
	        existingProduct.setPrice(updatedProduct.getPrice());
	        existingProduct.setStock(updatedProduct.getStock());
	        existingProduct.setCategory(updatedProduct.getCategory());
	        
	        // Only update image URL if it's provided
//	        if (updatedProduct.getImageUrl() != null && !updatedProduct.getImageUrl().isEmpty()) {
//	            existingProduct.setImageUrl(updatedProduct.getImageUrl());
//	        }
	        
	        // Save the updated product
	        productRepository.save(existingProduct);
	        return true;
	    }
	    
	    return false;
	}


	public List<Product> getAllProducts() {
		ArrayList<Product> productList = new ArrayList<Product>();
		productRepository.findAll().forEach(product -> productList.add(product));
		return productList;
	}
	
	
	public boolean reduceStock(int productId, int quantity) {
		Product product = productRepository.findById(productId).orElse(null);
		if (product == null || product.getStock() < quantity) {
			return false;
		}
		
		product.setStock(product.getStock() - quantity);
		productRepository.save(product);
		return true;
	}

	@Override
	public Product getProductByIdService(int pid) {
		return productRepository.findById(pid).get();
	}
}