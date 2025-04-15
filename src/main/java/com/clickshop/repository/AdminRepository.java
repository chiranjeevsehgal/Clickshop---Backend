package com.clickshop.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import com.clickshop.entity.Admin;

@Repository 											// table name, p key datatype
public interface AdminRepository extends CrudRepository<Admin, Integer> {

	@Query("SELECT COUNT(a) > 0 FROM Admin a WHERE a.userId = :userId")
	boolean isAdmin(int userId);

	Admin findByUserId(int userId);

}