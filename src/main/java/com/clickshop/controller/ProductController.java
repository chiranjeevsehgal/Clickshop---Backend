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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.clickshop.entity.Product;
import com.clickshop.entity.User;
import com.clickshop.security.SecurityUtils;
import com.clickshop.service.ProductService;

@RestController
@RequestMapping("/product")
public class ProductController {
	@Autowired
	private ProductService productService;

	@Autowired
	private SecurityUtils securityUtils;

	@PostMapping("/add")
	@ResponseBody
	public ResponseEntity<?> addProduct(@RequestBody Product product) {
		try {
			Product addedProduct = productService.addProduct(product);

			if (addedProduct != null) {
				return ResponseEntity.status(HttpStatus.CREATED).body(addedProduct);
			} else {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST)
						.body(Map.of("error", "Failed to add product"));
			}
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("error", "An error occurred: " + e.getMessage()));
		}
	}

	@DeleteMapping("/delete/{productId}")
	@ResponseBody
	public ResponseEntity<String> deleteProduct(@PathVariable("productId") int productId) {
		try {
			boolean isDeleted = productService.deleteProduct(productId);
			if (isDeleted) {
				return ResponseEntity.ok("SUCCESS");
			} else {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("FAIL");
			}
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("ERROR");
		}
	}

	@PutMapping("/update/{productId}")
	public ResponseEntity<String> updateProduct(
			@PathVariable("productId") int productId,
			@RequestBody Product product) {
		try {
			System.out.println("Updating product: " + productId);

			// Set the product ID to ensure it matches the path parameter
			product.setId(productId);

			boolean isUpdated = productService.updateProduct(product);
			if (isUpdated) {
				return ResponseEntity.ok("Product updated successfully!");
			} else {
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Product not found!");
			}
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
		}
	}

	@GetMapping("/{id}")
	public ResponseEntity<?> getProductById(@PathVariable("id") int id) {

		try {
				Product product = productService.getProductByIdService(id);
				return ResponseEntity.ok(product);
			} catch (Exception e) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND)
						.body(Map.of("error", "Product not found"));
			}
	}

	@GetMapping("/category/{category}")
	public ResponseEntity<?> getProductsByCategory(@PathVariable String category) {

		// List<Product> products = productService.getProductsByCategory(category);
		List<Product> products = null;
		return ResponseEntity.ok(products);
	}

	@GetMapping("/products/search")
	public ResponseEntity<?> searchProducts(@RequestParam("term") String searchTerm) {
		// List<Product> products = productService.searchProducts(searchTerm);
		List<Product> products = null;
		return ResponseEntity.ok(products);
	}

}
