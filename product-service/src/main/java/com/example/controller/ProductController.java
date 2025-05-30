package com.example.controller;

import com.example.model.Product;
import com.example.service.CategoryService;
import com.example.service.ProductService;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
// import org.springframework.security.access.prepost.PreAuthorize; // ❌ REMOVED
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;
    private final CategoryService categoryService;

    @Autowired
    public ProductController(ProductService productService, CategoryService categoryService) {
        this.productService = productService;
        this.categoryService = categoryService;
    }

    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getProductById(@PathVariable Long id) {
        return productService.getProductById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<Product>> getProductsByCategory(@PathVariable Long categoryId) {
        return ResponseEntity.ok(productService.getProductsByCategoryId(categoryId));
    }

    @GetMapping("/search")
    public ResponseEntity<List<Product>> searchProducts(@RequestParam String term) {
        return ResponseEntity.ok(productService.searchProducts(term));
    }

    @PostMapping
    // @PreAuthorize("hasRole('ADMIN')") // ❌ REMOVED - NO SECURITY
    public ResponseEntity<Product> createProduct(@RequestBody Product product) {
        System.out.println("✅ CREATE PRODUCT REQUEST RECEIVED: " + product.getName());
        return new ResponseEntity<>(productService.createProduct(product), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    // @PreAuthorize("hasRole('ADMIN')") // ❌ REMOVED - NO SECURITY
    public ResponseEntity<?> updateProduct(@PathVariable Long id, @RequestBody Product product) {
        try {
            return ResponseEntity.ok(productService.updateProduct(id, product));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    // @PreAuthorize("hasRole('ADMIN')") // ❌ REMOVED - NO SECURITY
    public ResponseEntity<?> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/stock")
    // @PreAuthorize("hasRole('ADMIN')") // ❌ REMOVED - NO SECURITY
    public ResponseEntity<?> updateStock(@PathVariable Long id, @RequestParam int quantity) {
        boolean updated = productService.updateStock(id, quantity);
        if (updated) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.badRequest().build();
    }
    
    @GetMapping("/debug")
    public ResponseEntity<Map<String, Object>> debugRequest(HttpServletRequest request) {
        Map<String, Object> debugInfo = new HashMap<>();
        
        // Headers
        Enumeration<String> headerNames = request.getHeaderNames();
        Map<String, String> headers = new HashMap<>();
        while (headerNames.hasMoreElements()) {
            String name = headerNames.nextElement();
            headers.put(name, request.getHeader(name));
        }
        debugInfo.put("headers", headers);
        
        // Security context
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        debugInfo.put("authentication", auth != null ? auth.getName() : "null");
        
        return ResponseEntity.ok(debugInfo);
    }
}