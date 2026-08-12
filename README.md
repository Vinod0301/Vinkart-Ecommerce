# Vinkart - Java Full Stack E-Commerce Project

A full-stack e-commerce app I built to practice putting together a real Java + React project end to end — not just another CRUD demo, but something with actual store logic: cart, wishlist, checkout, order tracking, the works.

Backend is Spring Boot, frontend is React (Vite), database is MySQL. Everything runs locally, no external services required unless you want real payments.

## Why I built this

I wanted a project that actually felt like a store, not just a products table with a form on top. So instead of stopping at "list products, add to cart," I added things like per-category product specs (RAM/storage for a phone, size/material for clothing), a proper multi-step order status flow for the admin side, and Stripe Checkout for real payment sessions.

## What it does

- Sign up / log in with JWT, role-based access for customers vs admins
- Browse products by category, add to cart, wishlist stuff for later
- Checkout flow that creates a real order
- Stripe Checkout integration (server creates the session, redirects to Stripe) — falls back gracefully if you don't set a key
- Admin dashboard: manage products, categories, and orders through a 7-stage status pipeline (`PENDING → CONFIRMED → PROCESSING → SHIPPED → OUT_FOR_DELIVERY → DELIVERED` / `CANCELLED`)
- Product reviews
- Flexible specs per category — admin can auto-fill common specs (RAM, storage, color, etc.) or add custom key/value pairs, stored via JPA element collections
- Multiple image URLs per product
- Seed data on first run: 5 categories, 50 products, so the store isn't empty when you open it

## Stack

- **Backend:** Java 17, Spring Boot 3.5, Spring Security, JWT (JJWT), Spring Data JPA/Hibernate
- **Frontend:** React, Vite, Axios, React Router
- **Database:** MySQL
- **Payments:** Stripe Java SDK (Checkout Sessions)

## Running it locally

You'll need JDK 17+, Maven, MySQL 8+, and Node 18+.

**1. Database** — the app will create the `vinaxmart` DB automatically on first run (`createDatabaseIfNotExist=true`), so you just need MySQL running. Set your own username/password in `backend/src/main/resources/application.properties` (see note below).

**2. Backend**

```bash
cd backend
mvn clean spring-boot:run
```

Runs on `http://localhost:8080`. On first startup it seeds the catalog plus an admin account:

```
admin@vinaxmart.com / Admin@123
```

**3. Frontend**

```bash
cd frontend
npm install
npm run dev
```

Runs on `http://localhost:5173`.

**4. Stripe (optional)** — without a key the checkout still works in a safe local mode. To try real test payments:

```bash
$env:STRIPE_SECRET_KEY="sk_test_your_key_here"   # PowerShell
cd backend
mvn clean spring-boot:run
```

Test card: `4242 4242 4242 4242`, any future expiry, any CVC.

## A note before you push this

`application.properties` has a real DB password hardcoded in it. Swap it for your own (or read it from an env var) before this goes anywhere public — don't just leave mine in there.

## What's not in here (on purpose)

This is a portfolio-scale MVP, so I deliberately left some things as extension points instead of half-faking them: real image upload (currently just URLs), email notifications, pagination, and any kind of production hardening/rate-limiting. Wanted the code that *is* here to be solid rather than padding it out with stubs.

## Project layout

```
Vinkart-Ecommerce/
├── backend/    Spring Boot API (auth, products, cart, orders, admin...)
├── frontend/   React + Vite storefront
└── sql/        schema.sql if you want to inspect/seed manually
```
