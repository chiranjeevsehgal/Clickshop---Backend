package com.clickshop.repository;

import java.util.List;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.clickshop.entity.Cart;
import com.clickshop.entity.User;

@Repository 										// table name, p key datatype
public interface CartRepository extends CrudRepository<Cart, Integer> {

	List<Cart> findByUserId(int id);

	void deleteByCartId(int cartId);

	Cart findByUserIdAndProductId(int userId, int productId);

	List<Cart> findAll();

	void deleteByUser(User user);


}