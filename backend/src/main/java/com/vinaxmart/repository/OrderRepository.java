package com.vinaxmart.repository;

import com.vinaxmart.entity.Order;
import com.vinaxmart.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserOrderByCreatedAtDesc(User user);
    long countByStatus(String status);
    Optional<Order> findByStripeSessionId(String stripeSessionId);
}
