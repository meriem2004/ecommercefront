package com.example.service;

import com.example.model.Category;
import com.example.model.Product;

import java.util.List;
import java.util.Optional;

public interface ProductService {
    List<Product> getAllProducts();
    Optional<Product> getProductById(Long id);
    List<Product> getProductsByCategory(Category category);
    List<Product> getProductsByCategoryId(Long categoryId);
    List<Product> searchProducts(String keyword);
    Product createProduct(Product product);
    Product updateProduct(Long id, Product product);
    void deleteProduct(Long id);
    boolean updateStock(Long id, int quantity);
}
