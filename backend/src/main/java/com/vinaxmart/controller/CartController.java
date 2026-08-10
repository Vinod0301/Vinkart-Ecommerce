package com.vinaxmart.controller;
import com.vinaxmart.entity.*; import com.vinaxmart.repository.*; import org.springframework.security.core.Authentication; import org.springframework.web.bind.annotation.*; import java.util.*;
@RestController @RequestMapping("/api/cart") public class CartController {
 final CartItemRepository cart; final UserRepository users; final ProductRepository products;
 CartController(CartItemRepository c,UserRepository u,ProductRepository p){cart=c;users=u;products=p;}
 User u(Authentication a){return users.findByEmail(a.getName()).orElseThrow();}
 @GetMapping public List<CartItem> get(Authentication a){return cart.findByUser(u(a));}
 @PostMapping("/{productId}") public CartItem add(Authentication a,@PathVariable Long productId,@RequestParam(defaultValue="1") int quantity,@RequestParam(defaultValue="") String size,@RequestParam(defaultValue="") String variant){
  User x=u(a); Product p=products.findById(productId).orElseThrow();
  String selectedSize=size==null?"":size.trim(); String selectedVariant=variant==null?"":variant.trim();
  CartItem i=cart.findByUser(x).stream().filter(c->c.getProduct().getId().equals(p.getId()) && Objects.equals(c.getSelectedSize(),selectedSize) && Objects.equals(c.getSelectedVariant(),selectedVariant)).findFirst().orElse(CartItem.builder().user(x).product(p).quantity(0).selectedSize(selectedSize).selectedVariant(selectedVariant).build());
  if(i.getQuantity()+quantity>p.getStock()) throw new RuntimeException("Requested quantity exceeds available stock");
  i.setQuantity(i.getQuantity()+quantity); return cart.save(i);
 }
 @PutMapping("/{id}") public CartItem qty(Authentication a,@PathVariable Long id,@RequestParam int quantity){CartItem i=cart.findById(id).orElseThrow();if(!i.getUser().getId().equals(u(a).getId()))throw new RuntimeException("Forbidden");if(quantity<1||quantity>i.getProduct().getStock())throw new RuntimeException("Invalid quantity");i.setQuantity(quantity);return cart.save(i);}
 @DeleteMapping("/{id}") public void remove(Authentication a,@PathVariable Long id){CartItem i=cart.findById(id).orElseThrow();if(!i.getUser().getId().equals(u(a).getId()))throw new RuntimeException("Forbidden");cart.delete(i);}
}
