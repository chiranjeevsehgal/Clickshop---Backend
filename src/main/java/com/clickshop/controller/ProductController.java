package com.clickshop.controller;

import java.time.LocalDate;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
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

import com.clickshop.entity.OrderItem;
import com.clickshop.entity.Product;
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

	@GetMapping("/between")
    public List<Product> getProductsBetweenDates(
        @RequestParam("startDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
        @RequestParam("endDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
    return productService.getProductsBetweenDates(startDate, endDate);
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

	@PostMapping("/{productId}/reduce-stock")
	public ResponseEntity<?> reduceStock(@PathVariable int productId, @RequestBody Map<String, Integer> request) {
		Integer quantity = request.get("quantity");
		if (quantity == null || quantity <= 0) {
			Map<String, String> errorResponse = new HashMap<>();
			errorResponse.put("error", "Invalid quantity");
			return ResponseEntity.badRequest().body(errorResponse);
		}

		try {
			boolean success = productService.reduceStock(productId, quantity);
			if (success) {
				Map<String, String> successResponse = new HashMap<>();
				successResponse.put("message", "Stock updated successfully");
				return ResponseEntity.ok().body(successResponse);
			} else {
				Map<String, String> errorResponse = new HashMap<>();
				errorResponse.put("error", "Failed to update stock");
				return ResponseEntity.badRequest().body(errorResponse);
			}
		} catch (Exception e) {
			Map<String, String> errorResponse = new HashMap<>();
			errorResponse.put("error", e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
		}
	}

	@GetMapping("/categories")
    public ResponseEntity<List<String>> getAllCategories() {
        return ResponseEntity.ok(productService.findAllCategories());
    }

	@GetMapping
    public ResponseEntity<List<Product>> getAllProducts(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean popular,
            @RequestParam(required = false, defaultValue = "12") Integer limit
    ) {
        if (category != null && !category.isEmpty()) {
            return ResponseEntity.ok(productService.getProductsByCategory(category));
        } else if (search != null && !search.isEmpty()) {
            return ResponseEntity.ok(productService.searchProducts(search));
        } else if (popular != null && popular) {
            return ResponseEntity.ok(productService.findPopularProducts());
        } else {
            return ResponseEntity.ok(productService.getAllProducts());
        }
    }

	@GetMapping("/category/{category}")
	public ResponseEntity<?> getProductsByCategory(@PathVariable String category) {

		List<Product> products = productService.getProductsByCategory(category);
		return ResponseEntity.ok(products);
	}

	@GetMapping("/products/search")
	public ResponseEntity<?> searchProducts(@RequestParam("term") String searchTerm) {
		// List<Product> products = productService.searchProducts(searchTerm);
		List<Product> products = null;
		return ResponseEntity.ok(products);
	}

}
