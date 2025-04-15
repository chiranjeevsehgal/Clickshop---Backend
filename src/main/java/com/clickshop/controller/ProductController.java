package com.clickshop.controller;


import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.clickshop.entity.Product;
import com.clickshop.entity.User;
import com.clickshop.service.AdminService;
import com.clickshop.service.ProductService;
import com.clickshop.service.UserService;
import com.clickshop.utils.SessionUtil;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/product")
public class ProductController {
	@Autowired
    private ProductService productService;


	 @GetMapping("/addproduct")
	 public String  showProductPage(ModelMap model) {
		 return ("addProduct"); 
	 }
	 
	 @PostMapping("/add")
	 public String addProduct(@ModelAttribute Product product, RedirectAttributes redirectAttributes) {
	     boolean isAdded = productService.addProduct(product);
	     
	     if (isAdded) {
//	    	 model.addAttribute("success", "true");
	    	 redirectAttributes.addAttribute("success", "true");

	     } else {
	    	 redirectAttributes.addAttribute("error", "true");
	     }
	     
	     return "redirect:/clickshop/product/addproduct";
	 }

	 
	 @DeleteMapping("/delete")
	 @ResponseBody
	 public String deleteProduct(@RequestParam("id") int productId) {
	     try {
	    	 boolean isDeleted = productService.deleteProduct(productId);
	         return isDeleted ? "SUCCESS" : "FAIL";
	     } catch (Exception e) {
	         e.printStackTrace();
	         return "ERROR";
	     }
	 }
	 
	 @PutMapping("/update")
	    public ResponseEntity<String> updateProductField(
	            @RequestParam("id") int productId,
	            @RequestParam("field") String field,
	            @RequestParam("value") String value) {	
	        try {
	        	System.out.println(productId);
	        	System.out.println(field);
	        	System.out.println(value);
	            boolean isUpdated = productService.updateProductField(productId, field, value);
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
	    public ResponseEntity<?> getProductById(@PathVariable("id") int id, HttpSession session) {
	        User.Role role = (User.Role) session.getAttribute("role");
	        
	        if (!SessionUtil.isValidSession(session) || !role.equals(User.Role.USER)) {
	            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
	                    .body(Map.of("error", "Unauthorized access"));
	        }
	        
	        try {
	            Product product = productService.getProductByIdService(id);
	            return ResponseEntity.ok(product);
	        } catch (Exception e) {
	            return ResponseEntity.status(HttpStatus.NOT_FOUND)
	                    .body(Map.of("error", "Product not found"));
	        }
	    }
	 
	 @GetMapping("/category/{category}")
	    public ResponseEntity<?> getProductsByCategory(@PathVariable String category, HttpSession session) {
	        User.Role role = (User.Role) session.getAttribute("role");
	        
	        if (!SessionUtil.isValidSession(session) || !role.equals(User.Role.USER)) {
	            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
	                    .body(Map.of("error", "Unauthorized access"));
	        }
	        
//	        List<Product> products = productService.getProductsByCategory(category);
	        List<Product> products = null;
	        return ResponseEntity.ok(products);
	    }
	    
	    @GetMapping("/products/search")
	    public ResponseEntity<?> searchProducts(@RequestParam("term") String searchTerm, HttpSession session) {
	        User.Role role = (User.Role) session.getAttribute("role");
	        
	        if (!SessionUtil.isValidSession(session) || !role.equals(User.Role.USER)) {
	            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
	                    .body(Map.of("error", "Unauthorized access"));
	        }
	        
//	        List<Product> products = productService.searchProducts(searchTerm);
	        List<Product> products=null;
	        return ResponseEntity.ok(products);
	    }
	 
}
