package com.vinaxmart.config;

import com.vinaxmart.entity.Category;
import com.vinaxmart.entity.Product;
import com.vinaxmart.entity.Role;
import com.vinaxmart.entity.User;
import com.vinaxmart.repository.CategoryRepository;
import com.vinaxmart.repository.ProductRepository;
import com.vinaxmart.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner seedData(UserRepository userRepository,
                               PasswordEncoder passwordEncoder,
                               CategoryRepository categoryRepository,
                               ProductRepository productRepository,
                               PlatformTransactionManager transactionManager,
                               JdbcTemplate jdbcTemplate) {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        return args -> transactionTemplate.executeWithoutResult(status -> {
            // Older versions of VinaxMart created product_specifications with both
            // spec_name and spec_key as NOT NULL. The current flexible Map mapping
            // writes spec_name, so keep the legacy spec_key column nullable.
            try {
                jdbcTemplate.execute("ALTER TABLE product_specifications MODIFY COLUMN spec_key VARCHAR(100) NULL");
            } catch (Exception ignored) {
                // Column may not exist on a fresh database; Hibernate will create the
                // current mapping. Do not stop application startup for this compatibility step.
            }

            if (userRepository.findByEmail("admin@vinaxmart.com").isEmpty()) {
                User admin = User.builder()
                        .name("VinaxMart Admin")
                        .email("admin@vinaxmart.com")
                        .password(passwordEncoder.encode("Admin@123"))
                        .role(Role.ADMIN)
                        .enabled(true)
                        .build();
                userRepository.save(admin);
            }

            Map<String, Category> categories = new HashMap<>();
            for (String name : List.of("Mobiles", "Laptops", "Men Clothing", "Women Clothing", "Mobile & Laptop Accessories")) {
                Category category = categoryRepository.findAll().stream()
                        .filter(c -> c.getName().equalsIgnoreCase(name))
                        .findFirst()
                        .orElseGet(() -> categoryRepository.save(Category.builder().name(name).build()));
                categories.put(name, category);
            }

            List<SeedProduct> seedProducts = List.of(
                    new SeedProduct("Mobiles", "iPhone 17 Pro Max", "Premium smartphone iPhone 17 Pro Max with reliable performance, modern design and great everyday value.", 149999, 10, 20, "https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?w=900", 4.1),
                    new SeedProduct("Mobiles", "Samsung Galaxy S25 Ultra", "Premium smartphone Samsung Galaxy S25 Ultra with reliable performance, modern design and great everyday value.", 139999, 12, 30, "https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?w=900", 4.2),
                    new SeedProduct("Mobiles", "Google Pixel 10 Pro", "Premium smartphone Google Pixel 10 Pro with reliable performance, modern design and great everyday value.", 109999, 14, 40, "https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?w=900", 4.3),
                    new SeedProduct("Mobiles", "OnePlus 13", "Premium smartphone OnePlus 13 with reliable performance, modern design and great everyday value.", 69999, 16, 50, "https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?w=900", 4.4),
                    new SeedProduct("Mobiles", "Xiaomi 15 Pro", "Premium smartphone Xiaomi 15 Pro with reliable performance, modern design and great everyday value.", 79999, 10, 60, "https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?w=900", 4.5),
                    new SeedProduct("Mobiles", "Nothing Phone (3)", "Premium smartphone Nothing Phone (3) with reliable performance, modern design and great everyday value.", 49999, 12, 20, "https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?w=900", 4.6),
                    new SeedProduct("Mobiles", "Motorola Edge 60 Pro", "Premium smartphone Motorola Edge 60 Pro with reliable performance, modern design and great everyday value.", 59999, 14, 30, "https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?w=900", 4.7),
                    new SeedProduct("Mobiles", "vivo X200 Pro", "Premium smartphone vivo X200 Pro with reliable performance, modern design and great everyday value.", 89999, 16, 40, "https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?w=900", 4.8),
                    new SeedProduct("Mobiles", "OPPO Find X8 Pro", "Premium smartphone OPPO Find X8 Pro with reliable performance, modern design and great everyday value.", 94999, 10, 50, "https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?w=900", 4.9),
                    new SeedProduct("Mobiles", "Realme GT 7 Pro", "Premium smartphone Realme GT 7 Pro with reliable performance, modern design and great everyday value.", 54999, 12, 60, "https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?w=900", 4.1),
                    new SeedProduct("Laptops", "Dell Inspiron 15", "Performance laptop Dell Inspiron 15 with reliable performance, modern design and great everyday value.", 64999, 10, 20, "https://images.unsplash.com/photo-1496181133206-80ce9b88a853?w=900", 4.1),
                    new SeedProduct("Laptops", "HP Pavilion 14", "Performance laptop HP Pavilion 14 with reliable performance, modern design and great everyday value.", 67999, 12, 30, "https://images.unsplash.com/photo-1496181133206-80ce9b88a853?w=900", 4.2),
                    new SeedProduct("Laptops", "Lenovo IdeaPad Slim 5", "Performance laptop Lenovo IdeaPad Slim 5 with reliable performance, modern design and great everyday value.", 62999, 14, 40, "https://images.unsplash.com/photo-1496181133206-80ce9b88a853?w=900", 4.3),
                    new SeedProduct("Laptops", "ASUS Vivobook 15", "Performance laptop ASUS Vivobook 15 with reliable performance, modern design and great everyday value.", 57999, 16, 50, "https://images.unsplash.com/photo-1496181133206-80ce9b88a853?w=900", 4.4),
                    new SeedProduct("Laptops", "Acer Aspire 5", "Performance laptop Acer Aspire 5 with reliable performance, modern design and great everyday value.", 52999, 10, 60, "https://images.unsplash.com/photo-1496181133206-80ce9b88a853?w=900", 4.5),
                    new SeedProduct("Laptops", "MacBook Air M4", "Performance laptop MacBook Air M4 with reliable performance, modern design and great everyday value.", 99999, 12, 20, "https://images.unsplash.com/photo-1496181133206-80ce9b88a853?w=900", 4.6),
                    new SeedProduct("Laptops", "MacBook Pro 14 M4", "Performance laptop MacBook Pro 14 M4 with reliable performance, modern design and great everyday value.", 169999, 14, 30, "https://images.unsplash.com/photo-1496181133206-80ce9b88a853?w=900", 4.7),
                    new SeedProduct("Laptops", "HP Victus Gaming 16", "Performance laptop HP Victus Gaming 16 with reliable performance, modern design and great everyday value.", 94999, 16, 40, "https://images.unsplash.com/photo-1496181133206-80ce9b88a853?w=900", 4.8),
                    new SeedProduct("Laptops", "Lenovo LOQ Gaming", "Performance laptop Lenovo LOQ Gaming with reliable performance, modern design and great everyday value.", 89999, 10, 50, "https://images.unsplash.com/photo-1496181133206-80ce9b88a853?w=900", 4.9),
                    new SeedProduct("Laptops", "ASUS TUF Gaming A15", "Performance laptop ASUS TUF Gaming A15 with reliable performance, modern design and great everyday value.", 84999, 12, 60, "https://images.unsplash.com/photo-1496181133206-80ce9b88a853?w=900", 4.1),
                    new SeedProduct("Men Clothing", "Men Classic Cotton Shirt", "Men fashion essential Men Classic Cotton Shirt with reliable performance, modern design and great everyday value.", 1299, 20, 20, "https://images.unsplash.com/photo-1523381210434-271e8be1f52b?w=900", 4.1),
                    new SeedProduct("Men Clothing", "Men Slim Fit Jeans", "Men fashion essential Men Slim Fit Jeans with reliable performance, modern design and great everyday value.", 1899, 22, 30, "https://images.unsplash.com/photo-1523381210434-271e8be1f52b?w=900", 4.2),
                    new SeedProduct("Men Clothing", "Men Casual Polo T-Shirt", "Men fashion essential Men Casual Polo T-Shirt with reliable performance, modern design and great everyday value.", 899, 24, 40, "https://images.unsplash.com/photo-1523381210434-271e8be1f52b?w=900", 4.3),
                    new SeedProduct("Men Clothing", "Men Formal Shirt", "Men fashion essential Men Formal Shirt with reliable performance, modern design and great everyday value.", 1499, 26, 50, "https://images.unsplash.com/photo-1523381210434-271e8be1f52b?w=900", 4.4),
                    new SeedProduct("Men Clothing", "Men Bomber Jacket", "Men fashion essential Men Bomber Jacket with reliable performance, modern design and great everyday value.", 2499, 20, 60, "https://images.unsplash.com/photo-1523381210434-271e8be1f52b?w=900", 4.5),
                    new SeedProduct("Men Clothing", "Men Chino Trousers", "Men fashion essential Men Chino Trousers with reliable performance, modern design and great everyday value.", 1699, 22, 20, "https://images.unsplash.com/photo-1523381210434-271e8be1f52b?w=900", 4.6),
                    new SeedProduct("Men Clothing", "Men Oversized T-Shirt", "Men fashion essential Men Oversized T-Shirt with reliable performance, modern design and great everyday value.", 999, 24, 30, "https://images.unsplash.com/photo-1523381210434-271e8be1f52b?w=900", 4.7),
                    new SeedProduct("Men Clothing", "Men Linen Shirt", "Men fashion essential Men Linen Shirt with reliable performance, modern design and great everyday value.", 1799, 26, 40, "https://images.unsplash.com/photo-1523381210434-271e8be1f52b?w=900", 4.8),
                    new SeedProduct("Men Clothing", "Men Hoodie", "Men fashion essential Men Hoodie with reliable performance, modern design and great everyday value.", 2199, 20, 50, "https://images.unsplash.com/photo-1523381210434-271e8be1f52b?w=900", 4.9),
                    new SeedProduct("Men Clothing", "Men Denim Jacket", "Men fashion essential Men Denim Jacket with reliable performance, modern design and great everyday value.", 2999, 22, 60, "https://images.unsplash.com/photo-1523381210434-271e8be1f52b?w=900", 4.1),
                    new SeedProduct("Women Clothing", "Women Floral Kurti", "Women fashion essential Women Floral Kurti with reliable performance, modern design and great everyday value.", 1299, 20, 20, "https://images.unsplash.com/photo-1483985988355-763728e1935b?w=900", 4.1),
                    new SeedProduct("Women Clothing", "Women Casual Top", "Women fashion essential Women Casual Top with reliable performance, modern design and great everyday value.", 999, 22, 30, "https://images.unsplash.com/photo-1483985988355-763728e1935b?w=900", 4.2),
                    new SeedProduct("Women Clothing", "Women Straight Jeans", "Women fashion essential Women Straight Jeans with reliable performance, modern design and great everyday value.", 1799, 24, 40, "https://images.unsplash.com/photo-1483985988355-763728e1935b?w=900", 4.3),
                    new SeedProduct("Women Clothing", "Women Summer Dress", "Women fashion essential Women Summer Dress with reliable performance, modern design and great everyday value.", 1999, 26, 50, "https://images.unsplash.com/photo-1483985988355-763728e1935b?w=900", 4.4),
                    new SeedProduct("Women Clothing", "Women Denim Jacket", "Women fashion essential Women Denim Jacket with reliable performance, modern design and great everyday value.", 2499, 20, 60, "https://images.unsplash.com/photo-1483985988355-763728e1935b?w=900", 4.5),
                    new SeedProduct("Women Clothing", "Women Palazzo Pants", "Women fashion essential Women Palazzo Pants with reliable performance, modern design and great everyday value.", 1499, 22, 20, "https://images.unsplash.com/photo-1483985988355-763728e1935b?w=900", 4.6),
                    new SeedProduct("Women Clothing", "Women Cotton Saree", "Women fashion essential Women Cotton Saree with reliable performance, modern design and great everyday value.", 2299, 24, 30, "https://images.unsplash.com/photo-1483985988355-763728e1935b?w=900", 4.7),
                    new SeedProduct("Women Clothing", "Women Co-ord Set", "Women fashion essential Women Co-ord Set with reliable performance, modern design and great everyday value.", 1899, 26, 40, "https://images.unsplash.com/photo-1483985988355-763728e1935b?w=900", 4.8),
                    new SeedProduct("Women Clothing", "Women Hoodie", "Women fashion essential Women Hoodie with reliable performance, modern design and great everyday value.", 1799, 20, 50, "https://images.unsplash.com/photo-1483985988355-763728e1935b?w=900", 4.9),
                    new SeedProduct("Women Clothing", "Women Anarkali Dress", "Women fashion essential Women Anarkali Dress with reliable performance, modern design and great everyday value.", 2699, 22, 60, "https://images.unsplash.com/photo-1483985988355-763728e1935b?w=900", 4.1),
                    new SeedProduct("Mobile & Laptop Accessories", "65W GaN Fast Charger", "Mobile and laptop accessory 65W GaN Fast Charger with reliable performance, modern design and great everyday value.", 2499, 15, 20, "https://images.unsplash.com/photo-1609592424843-5b6d6d0b1f7d?w=900", 4.1),
                    new SeedProduct("Mobile & Laptop Accessories", "10000mAh Power Bank", "Mobile and laptop accessory 10000mAh Power Bank with reliable performance, modern design and great everyday value.", 1499, 17, 30, "https://images.unsplash.com/photo-1609592424843-5b6d6d0b1f7d?w=900", 4.2),
                    new SeedProduct("Mobile & Laptop Accessories", "20000mAh Power Bank", "Mobile and laptop accessory 20000mAh Power Bank with reliable performance, modern design and great everyday value.", 1999, 19, 40, "https://images.unsplash.com/photo-1609592424843-5b6d6d0b1f7d?w=900", 4.3),
                    new SeedProduct("Mobile & Laptop Accessories", "USB-C Braided Cable", "Mobile and laptop accessory USB-C Braided Cable with reliable performance, modern design and great everyday value.", 499, 21, 50, "https://images.unsplash.com/photo-1609592424843-5b6d6d0b1f7d?w=900", 4.4),
                    new SeedProduct("Mobile & Laptop Accessories", "Wireless Mouse", "Mobile and laptop accessory Wireless Mouse with reliable performance, modern design and great everyday value.", 999, 15, 60, "https://images.unsplash.com/photo-1609592424843-5b6d6d0b1f7d?w=900", 4.5),
                    new SeedProduct("Mobile & Laptop Accessories", "Bluetooth Keyboard", "Mobile and laptop accessory Bluetooth Keyboard with reliable performance, modern design and great everyday value.", 1499, 17, 20, "https://images.unsplash.com/photo-1609592424843-5b6d6d0b1f7d?w=900", 4.6),
                    new SeedProduct("Mobile & Laptop Accessories", "Laptop Cooling Pad", "Mobile and laptop accessory Laptop Cooling Pad with reliable performance, modern design and great everyday value.", 1299, 19, 30, "https://images.unsplash.com/photo-1609592424843-5b6d6d0b1f7d?w=900", 4.7),
                    new SeedProduct("Mobile & Laptop Accessories", "USB-C Hub 7-in-1", "Mobile and laptop accessory USB-C Hub 7-in-1 with reliable performance, modern design and great everyday value.", 2499, 21, 40, "https://images.unsplash.com/photo-1609592424843-5b6d6d0b1f7d?w=900", 4.8),
                    new SeedProduct("Mobile & Laptop Accessories", "Laptop Stand Aluminium", "Mobile and laptop accessory Laptop Stand Aluminium with reliable performance, modern design and great everyday value.", 1799, 15, 50, "https://images.unsplash.com/photo-1609592424843-5b6d6d0b1f7d?w=900", 4.9),
                    new SeedProduct("Mobile & Laptop Accessories", "Tempered Glass Screen Protector", "Mobile and laptop accessory Tempered Glass Screen Protector with reliable performance, modern design and great everyday value.", 299, 17, 60, "https://images.unsplash.com/photo-1609592424843-5b6d6d0b1f7d?w=900", 4.1)
            );

            for (SeedProduct seed : seedProducts) {
                boolean exists = productRepository.findAll().stream()
                        .anyMatch(p -> p.getName().equalsIgnoreCase(seed.name()));
                if (!exists) {
                    Product product = Product.builder()
                            .name(seed.name())
                            .description(seed.description())
                            .price(seed.price())
                            .discount(seed.discount())
                            .stock(seed.stock())
                            .imageUrl(seed.imageUrl())
                            .rating(seed.rating())
                            .category(categories.get(seed.category()))
                            .specifications(defaultSpecifications(seed.category()))
                            .build();
                    productRepository.save(product);
                } else {
                    // Also enrich older products created by previous versions.
                    productRepository.findAll().stream()
                            .filter(p -> p.getName().equalsIgnoreCase(seed.name()))
                            .findFirst()
                            .ifPresent(existing -> {
                                if (existing.getSpecifications() == null || existing.getSpecifications().isEmpty()) {
                                    existing.setSpecifications(defaultSpecifications(seed.category()));
                                    productRepository.save(existing);
                                }
                            });
                }
            }

            System.out.println("Vinkart seed complete: " + productRepository.count() + " products available.");
            System.out.println("Admin: admin@vinaxmart.com / Admin@123");
        });
    }


    private Map<String, String> defaultSpecifications(String category) {
        Map<String, String> specs = new java.util.LinkedHashMap<>();
        switch (category) {
            case "Mobiles" -> {
                specs.put("RAM", "8GB");
                specs.put("Storage", "128GB / 256GB");
                specs.put("Variants", "8GB / 128GB, 12GB / 256GB");
                specs.put("Color", "Black");
                specs.put("Display", "6.7 inch");
                specs.put("Battery", "5000mAh");
                specs.put("Network", "5G");
                specs.put("Warranty", "1 Year");
            }
            case "Laptops" -> {
                specs.put("RAM", "16GB");
                specs.put("Storage", "512GB SSD");
                specs.put("Variants", "16GB / 512GB, 16GB / 1TB");
                specs.put("Processor", "Intel Core i7");
                specs.put("Display", "15.6 inch FHD");
                specs.put("Graphics", "Integrated");
                specs.put("Operating System", "Windows 11");
                specs.put("Warranty", "1 Year");
            }
            case "Men Clothing", "Women Clothing" -> {
                specs.put("Available Sizes", "S, M, L, XL, XXL");
                specs.put("Variants", "Black, White, Blue");
                specs.put("Color", "Black");
                specs.put("Material", "Cotton");
                specs.put("Fit", "Regular");
                specs.put("Care", "Machine Wash");
                specs.put("Warranty", "No Warranty");
            }
            default -> {
                specs.put("Color", "Black");
                specs.put("Material", "Premium");
                specs.put("Compatibility", "Universal");
                specs.put("Warranty", "1 Year");
            }
        }
        return specs;
    }

    private record SeedProduct(String category, String name, String description, double price, double discount, int stock, String imageUrl, double rating) {}
}
