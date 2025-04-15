package com.clickshop.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.clickshop.entity.Product;
import com.clickshop.repository.ProductRepository;

@Service
public class ProductServiceImpl implements ProductService {
	Scanner sc = new Scanner(System.in);

	@Autowired
	ProductRepository productRepository;

	@Override
	public boolean addProduct(Product product) {
		// TODO Auto-generated method stub
		Product product1 = productRepository.save(product);
		if (product1 != null)
			return true;
		else {
			return false;
		}
	}

//	@Override
//	public ArrayList<Product> displayProducts() {
//		
//		ArrayList<Product> productList = new ArrayList<Product>();
//		productRepository.findAll().forEach(product -> productList.add(product));
//		return productList;
//	}

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

	public boolean updateProductField(int productId, String field, String value) {
		Optional<Product> optionalProduct = productRepository.findById(productId);
		if (optionalProduct.isPresent()) {
			Product product = optionalProduct.get();

			switch (field) {
			case "name":
				product.setName(value);
				break;
			case "price":
				product.setPrice(Float.parseFloat(value));
				break;
			case "description":
				product.setDescription(value);
				break;
			case "stock":
				product.setStock(Integer.parseInt(value));
				break;
			case "category":
				product.setCategory(value);
				break;
			default:
				throw new IllegalArgumentException("Invalid field: " + field);
			}

			productRepository.save(product);
			return true;
		}
		return false;
	}

	public List<Product> getAllProducts() {
		ArrayList<Product> productList = new ArrayList<Product>();
		productRepository.findAll().forEach(product -> productList.add(product));
		return productList;
	}

	@Override
	public Product getProductByIdService(int pid) {
		// TODO Auto-generated method stub
		return productRepository.findById(pid).get();
	}
}