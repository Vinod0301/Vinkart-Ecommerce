package com.vinaxmart.repository;
import com.vinaxmart.entity.*; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface CartItemRepository extends JpaRepository<CartItem,Long>{
 List<CartItem> findByUser(User u);
 Optional<CartItem> findByUserAndProduct(User u,Product p);
 void deleteByUser(User u);
}
