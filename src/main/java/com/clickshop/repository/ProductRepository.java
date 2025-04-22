package com.clickshop.repository;


import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.clickshop.entity.OrderItem;
import com.clickshop.entity.Product;

@Repository                                             // table name, p key datatype
public interface ProductRepository extends CrudRepository<Product, Integer> {
	
    List<Product> findByCategory(String category);

    List<Product> findByFeaturedTrue();

    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Product> searchProducts(@Param("searchTerm") String searchTerm);

    @Query("SELECT DISTINCT p.category FROM Product p ORDER BY p.category")
    List<String> findAllCategories();

    List<Product> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);



    
	
	
}