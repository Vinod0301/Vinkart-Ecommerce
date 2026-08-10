package com.vinaxmart.controller;

import com.vinaxmart.entity.Product;
import com.vinaxmart.repository.ProductRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    private final ProductRepository productRepository;

    public ProductController(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @GetMapping
    public List<Product> all(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long categoryId) {

        boolean hasSearch = search != null && !search.isBlank();
        boolean hasCategory = categoryId != null;

        if (hasSearch && hasCategory) {
            return productRepository.findByCategoryIdAndNameContainingIgnoreCase(categoryId, search.trim());
        }
        if (hasCategory) {
            return productRepository.findByCategoryId(categoryId);
        }
        if (hasSearch) {
            return productRepository.findByNameContainingIgnoreCase(search.trim());
        }
        return productRepository.findAll();
    }

    @GetMapping("/{id}")
    public Product one(@PathVariable Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Product add(@RequestBody Product product) {
        product.setId(null);
        return productRepository.save(product);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Product edit(@PathVariable Long id, @RequestBody Product product) {
        if (!productRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found");
        }
        product.setId(id);
        return productRepository.save(product);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void del(@PathVariable Long id) {
        if (!productRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found");
        }
        productRepository.deleteById(id);
    }
}
