package com.clickshop.repository;


import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.clickshop.entity.User;

@Repository                                             // table name, p key datatype
public interface UserRepository extends CrudRepository<User, Integer> {
	
	User findByEmail(String email);
    User findByUname(String uname);
    
    List<User> findByRoleIn(List<User.Role> roles);
    
    boolean existsByEmail(String email);
    boolean existsByUname(String username);
	
	
}