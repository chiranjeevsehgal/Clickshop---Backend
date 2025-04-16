package com.clickshop.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import com.clickshop.entity.Product;
import com.clickshop.entity.User;
import com.clickshop.entity.Wishlist;

@Repository 											// table name, p key datatype
public interface WishlistRepository extends CrudRepository<Wishlist, Integer> {

	List<Wishlist> findByUser(User user);
    Optional<Wishlist> findByUserAndProduct(User user, Product product);
    void deleteByUserAndProduct(User user, Product product);
    boolean existsByUserAndProduct(User user, Product product);
    void deleteAll(Iterable<? extends Wishlist> entities);

}