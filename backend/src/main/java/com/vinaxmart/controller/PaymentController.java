package com.vinaxmart.controller;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import com.vinaxmart.entity.Order;
import com.vinaxmart.entity.OrderItem;
import com.vinaxmart.entity.User;
import com.vinaxmart.repository.CartItemRepository;
import com.vinaxmart.repository.OrderRepository;
import com.vinaxmart.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final CartItemRepository cartRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final String frontendUrl;

    public PaymentController(
            CartItemRepository cartRepository,
            OrderRepository orderRepository,
            UserRepository userRepository,
            @Value("${app.frontend-url:http://localhost:5173}") String frontendUrl,
            @Value("${app.stripe.secret-key:}") String stripeSecretKey) {
        this.cartRepository = cartRepository;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.frontendUrl = frontendUrl;
        Stripe.apiKey = stripeSecretKey;
    }

    @PostMapping("/create-checkout-session")
    public Map<String, String> createCheckoutSession(Authentication authentication, @RequestBody Map<String, String> body) {
        if (Stripe.apiKey == null || Stripe.apiKey.isBlank()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Stripe is not configured. Add app.stripe.secret-key to application.properties.");
        }

        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        var cart = cartRepository.findByUser(user);
        if (cart.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cart is empty");
        }

        String address = body.getOrDefault("address", "Not provided");
        try {
            SessionCreateParams.Builder sessionBuilder = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setClientReferenceId(String.valueOf(user.getId()))
                    .setSuccessUrl(frontendUrl + "/payment-success?session_id={CHECKOUT_SESSION_ID}")
                    .setCancelUrl(frontendUrl + "/checkout?cancelled=true")
                    .setBillingAddressCollection(SessionCreateParams.BillingAddressCollection.REQUIRED)
                    .putMetadata("userId", String.valueOf(user.getId()))
                    .putMetadata("address", address);

            for (var item : cart) {
                if (item.getQuantity() <= 0 || item.getQuantity() > item.getProduct().getStock()) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Insufficient stock for " + item.getProduct().getName());
                }
                long unitAmount = Math.round(item.getProduct().getPrice()
                        * (1 - item.getProduct().getDiscount() / 100.0) * 100);
                if (unitAmount < 50) {
                    unitAmount = 50;
                }

                SessionCreateParams.LineItem lineItem = SessionCreateParams.LineItem.builder()
                        .setQuantity((long) item.getQuantity())
                        .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                                .setCurrency("inr")
                                .setUnitAmount(unitAmount)
                                .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                        .setName(item.getProduct().getName())
                                        .setDescription(buildLineDescription(item.getProduct().getDescription(), item.getSelectedSize(), item.getSelectedVariant()))
                                        .build())
                                .build())
                        .build();
                sessionBuilder.addLineItem(lineItem);
            }

            Session session = Session.create(sessionBuilder.build());
            return Map.of("sessionId", session.getId(), "url", session.getUrl());
        } catch (StripeException e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Stripe checkout could not be created: " + e.getMessage());
        }
    }

    private String buildLineDescription(String description, String size, String variant) {
        String base = (description == null || description.isBlank()) ? "Vinkart product" : description;
        StringBuilder selected = new StringBuilder();
        if (size != null && !size.isBlank()) selected.append("Size: ").append(size);
        if (variant != null && !variant.isBlank()) {
            if (selected.length() > 0) selected.append(" | ");
            selected.append("Variant: ").append(variant);
        }
        return selected.length() == 0 ? base : base + " | " + selected;
    }

    @PostMapping("/confirm")
    @Transactional
    public Order confirmPayment(Authentication authentication, @RequestParam String sessionId) {
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        try {
            Session session = Session.retrieve(sessionId);
            if (!String.valueOf(user.getId()).equals(session.getClientReferenceId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Payment session does not belong to this user");
            }
            if (!"paid".equalsIgnoreCase(session.getPaymentStatus())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payment has not been completed");
            }

            var existing = orderRepository.findByStripeSessionId(sessionId);
            if (existing.isPresent()) {
                return existing.get();
            }

            var cart = cartRepository.findByUser(user);
            if (cart.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cart is empty or the order was already created");
            }

            var items = new ArrayList<OrderItem>();
            double total = 0;
            for (var cartItem : cart) {
                var product = cartItem.getProduct();
                if (cartItem.getQuantity() <= 0 || cartItem.getQuantity() > product.getStock()) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Insufficient stock for " + product.getName());
                }
                double price = product.getPrice() * (1 - product.getDiscount() / 100.0);
                items.add(OrderItem.builder()
                        .product(product)
                        .quantity(cartItem.getQuantity())
                        .price(price)
                        .selectedSize(cartItem.getSelectedSize())
                        .selectedVariant(cartItem.getSelectedVariant())
                        .build());
                product.setStock(product.getStock() - cartItem.getQuantity());
                total += price * cartItem.getQuantity();
            }

            String address = session.getMetadata().getOrDefault("address", "Not provided");
            Order order = Order.builder()
                    .user(user)
                    .items(items)
                    .total(total)
                    .status("CONFIRMED")
                    .paymentStatus("PAID")
                    .stripeSessionId(sessionId)
                    .address(address)
                    .createdAt(LocalDateTime.now())
                    .build();

            Order saved = orderRepository.save(order);
            cartRepository.deleteByUser(user);
            return saved;
        } catch (StripeException e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Could not verify Stripe payment: " + e.getMessage());
        }
    }
}
