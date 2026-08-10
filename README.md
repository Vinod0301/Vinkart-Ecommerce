# Vinkart - Java Full Stack E-Commerce Project

## Stack
- Frontend: React, Vite, Axios, React Router
- Backend: Java 17, Spring Boot, Spring Security, JWT, Spring Data JPA
- Database: MySQL
- Build: Maven

## Included
Authentication, products, categories, cart, wishlist, checkout/order flow, reviews API, admin dashboard API, role-based security and seed data.

## Important
This is a runnable portfolio MVP. Payment gateway, image upload, email, advanced analytics, pagination and production hardening are intentionally left as extension points rather than fake integrations. Never commit real secrets.


## Stripe payments

This version uses Stripe Checkout from the Spring Boot backend. Stripe Checkout sessions are created server-side and the user is redirected to Stripe to complete payment. Stripe recommends creating a Checkout Session on your server and redirecting the customer to its URL.

1. Create a Stripe account and use a test-mode secret key.
2. Set the environment variable before starting Spring Boot:

```powershell
$env:STRIPE_SECRET_KEY="sk_test_your_key_here"
cd backend
mvn clean spring-boot:run
```

Or set `app.stripe.secret-key` in `backend/src/main/resources/application.properties`. Never commit a real Stripe secret key to GitHub.

For Stripe test mode, a common test card is `4242 4242 4242 4242`, with any future expiry, any 3-digit CVC, and any valid name.

## Default catalog

On startup, the seeder ensures these five categories exist and adds 50 products if they are missing: 10 Mobiles, 10 Laptops, 10 Men Clothing, 10 Women Clothing, and 10 Mobile & Laptop Accessories. Existing products are not deleted.

## Admin order management

Admin can update orders through the Admin Dashboard using: PENDING, CONFIRMED, PROCESSING, SHIPPED, OUT_FOR_DELIVERY, DELIVERED, or CANCELLED.


## Multiple Product Images
Admin can add multiple image URLs for each product. The first URL is the primary image, and all URLs are stored in the `product_images` table automatically via JPA `@ElementCollection`. Existing single `imageUrl` products continue to work.


## Product Specifications (Added)
Admin can now add flexible category-specific specifications to every product without changing Java code:
- Mobiles: RAM, Storage, Color, Display, Battery, Network, Warranty, Variants
- Laptops: RAM, Storage, Processor, Display, Graphics, OS, Warranty
- Clothing: Available Sizes, Color, Material, Fit, Care, Warranty
- Any category: add any custom key/value specification

Specifications are stored in the `product_specifications` JPA collection table and are displayed on product cards and the product details page.
The Admin page includes **Auto-fill Category Specs** plus manual **Add Specification** rows.
