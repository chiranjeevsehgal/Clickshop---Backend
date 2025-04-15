package com.clickshop.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


import com.clickshop.entity.OrderItem;
import com.clickshop.entity.OrderItem.Order_Status;

@Repository                                             // table name, p key datatype
public interface OrderRepository extends JpaRepository<OrderItem, Integer> {

	 @Query("SELECT o FROM OrderItem o JOIN FETCH o.product p WHERE o.user.id = :userId")
	    List<OrderItem> findByUserId(@Param("userId") int userId);
	 
	    @Query("SELECT SUM(oi.quantity * oi.totalPrice) FROM OrderItem oi")
	 Double sumTotalRevenue();
	  @Query("SELECT o FROM OrderItem o ORDER BY o.orderDate DESC")
	  Page<OrderItem> findAllByOrderByOrderDateDesc(Pageable pageable);
	  
	   List<OrderItem> findByOrderStatus(Order_Status status);


}