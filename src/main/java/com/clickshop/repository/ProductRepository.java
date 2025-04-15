package com.clickshop.repository;


import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.clickshop.entity.Product;

@Repository                                             // table name, p key datatype
public interface ProductRepository extends CrudRepository<Product, Integer> {
	
	
	
}