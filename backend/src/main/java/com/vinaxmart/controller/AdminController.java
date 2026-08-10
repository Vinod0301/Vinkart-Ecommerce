package com.vinaxmart.controller;

import com.vinaxmart.repository.OrderRepository;
import com.vinaxmart.repository.ProductRepository;
import com.vinaxmart.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final UserRepository users;
    private final ProductRepository products;
    private final OrderRepository orders;

    public AdminController(UserRepository users, ProductRepository products, OrderRepository orders) {
        this.users = users;
        this.products = products;
        this.orders = orders;
    }

    @GetMapping("/dashboard")
    public Map<String, Object> dashboard() {
        double revenue = orders.findAll().stream()
                .filter(o -> "PAID".equalsIgnoreCase(o.getPaymentStatus()))
                .mapToDouble(x -> x.getTotal()).sum();
        return Map.of(
                "users", users.count(),
                "products", products.count(),
                "orders", orders.count(),
                "revenue", revenue,
                "pendingOrders", orders.countByStatus("PENDING"),
                "deliveredOrders", orders.countByStatus("DELIVERED")
        );
    }

    @GetMapping("/users")
    public Object userList() {
        return users.findAll();
    }

    @GetMapping("/orders")
    public Object orderList() {
        return orders.findAll();
    }

    @PutMapping("/orders/{id}/status")
    public Object updateStatus(@PathVariable Long id, @RequestParam String value) {
        List<String> allowed = List.of("PENDING", "CONFIRMED", "PROCESSING", "SHIPPED", "OUT_FOR_DELIVERY", "DELIVERED", "CANCELLED");
        String status = value == null ? "" : value.trim().toUpperCase();
        if (!allowed.contains(status)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid order status");
        }
        var order = orders.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
        order.setStatus(status);
        return orders.save(order);
    }
}
