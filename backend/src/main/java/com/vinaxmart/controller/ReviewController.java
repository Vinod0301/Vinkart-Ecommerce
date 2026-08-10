package com.vinaxmart.controller;

import com.vinaxmart.entity.*;
import com.vinaxmart.repository.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {
    private final ReviewRepository reviews;
    private final ProductRepository products;
    private final UserRepository users;

    public ReviewController(ReviewRepository reviews, ProductRepository products, UserRepository users) {
        this.reviews = reviews;
        this.products = products;
        this.users = users;
    }

    @GetMapping("/product/{pid}")
    public List<Review> all(@PathVariable Long pid) {
        return reviews.findByProduct(products.findById(pid).orElseThrow());
    }

    @PostMapping("/product/{pid}")
    @PreAuthorize("isAuthenticated()")
    public Review add(@PathVariable Long pid, @RequestBody Review review, Authentication authentication) {
        review.setId(null);
        review.setProduct(products.findById(pid).orElseThrow());
        review.setUser(users.findByEmail(authentication.getName()).orElseThrow());
        review.setCreatedAt(LocalDateTime.now());
        return reviews.save(review);
    }
}
