package com.clickshop.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.clickshop.entity.OrderItem;
import com.clickshop.entity.Product;
import com.clickshop.repository.ProductRepository;

@Service
public class ProductServiceImpl implements ProductService {
	Scanner sc = new Scanner(System.in);

	@Autowired
	ProductRepository productRepository;

	@Override
	public Product addProduct(Product product) {
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

	@Override
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
			// if (updatedProduct.getImageUrl() != null &&
			// !updatedProduct.getImageUrl().isEmpty()) {
			// existingProduct.setImageUrl(updatedProduct.getImageUrl());
			// }

			// Save the updated product
			productRepository.save(existingProduct);
			return true;
		}

		return false;
	}

	@Override
	public List<Product> getAllProducts() {
		ArrayList<Product> productList = new ArrayList<Product>();
		productRepository.findAll().forEach(product -> productList.add(product));
		return productList;
	}

	@Override
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

	@Override
	public List<Product> getProductsByCategory(String category) {
		return productRepository.findByCategory(category);

	}

	@Override
	public List<Product> searchProducts(String searchTerm) {
        
        return productRepository.searchProducts(searchTerm);
    }

	@Override
	public List<Product> findPopularProducts() {
        
        return productRepository.findByFeaturedTrue();
    }
	
	@Override
	public List<String> findAllCategories() {
        return productRepository.findAllCategories();
    }

	@Override
	public List<Product> getProductsBetweenDates(LocalDate startDate, LocalDate  endDate) {
        LocalDateTime start = startDate.atStartOfDay();
    	LocalDateTime end = endDate.atTime(23, 59, 59);
    	return productRepository.findByCreatedAtBetween(start, end);
    }

}