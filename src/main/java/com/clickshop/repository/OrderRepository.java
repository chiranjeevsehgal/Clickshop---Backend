package com.clickshop.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


import com.clickshop.entity.OrderItem;

@Repository                                             // table name, p key datatype
public interface OrderRepository extends CrudRepository<OrderItem, Integer> {

	 @Query("SELECT o FROM OrderItem o JOIN FETCH o.product p WHERE o.user.id = :userId")
	    List<OrderItem> findByUserId(@Param("userId") int userId);
	 
	    @Query("SELECT SUM(oi.quantity * oi.totalPrice) FROM OrderItem oi")
	 Double sumTotalRevenue();
	  @Query("SELECT o FROM OrderItem o ORDER BY o.orderDate DESC")
	  Page<OrderItem> findAllByOrderByOrderDateDesc(Pageable pageable);

}