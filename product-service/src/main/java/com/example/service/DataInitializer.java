package com.example.service;

import com.example.model.Category;
import com.example.model.Product;
import com.example.repository.CategoryRepository;
import com.example.repository.ProductRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {
    
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    
    @PostConstruct
    @Transactional
    public void initializeData() {
        if (categoryRepository.count() == 0) {
            log.info("Initializing database with sample data...");
            createCategories();
            log.info("Database initialization completed!");
        } else {
            log.info("Database already contains data, skipping initialization.");
        }
    }
    
    private void createCategories() {
        // Electronics Category
        Category electronics = new Category("Electronics", "Electronic devices and gadgets");
        electronics = categoryRepository.save(electronics);
        
        electronics.addProduct(new Product("iPhone 15 Pro", "Latest Apple smartphone with A17 Pro chip", 999.99, 50));
        electronics.addProduct(new Product("Samsung Galaxy S24", "Flagship Android smartphone", 899.99, 30));
        electronics.addProduct(new Product("MacBook Air M3", "Lightweight laptop with M3 chip", 1299.99, 20));
        electronics.addProduct(new Product("Dell XPS 13", "Premium Windows laptop", 1099.99, 15));
        electronics.addProduct(new Product("iPad Pro", "Professional tablet for creative work", 799.99, 25));
        electronics.addProduct(new Product("Sony WH-1000XM5", "Noise-canceling wireless headphones", 349.99, 40));
        electronics.addProduct(new Product("Apple Watch Series 9", "Smartwatch with health tracking", 399.99, 35));
        
        // Clothing Category
        Category clothing = new Category("Clothing", "Fashion and apparel for all occasions");
        clothing = categoryRepository.save(clothing);
        
        clothing.addProduct(new Product("Levi's 501 Jeans", "Classic straight-fit denim jeans", 79.99, 100));
        clothing.addProduct(new Product("Nike Air Max Sneakers", "Comfortable running shoes", 129.99, 80));
        clothing.addProduct(new Product("Cotton T-Shirt", "Basic crew neck t-shirt", 19.99, 200));
        clothing.addProduct(new Product("Wool Sweater", "Cozy winter pullover", 89.99, 60));
        clothing.addProduct(new Product("Leather Jacket", "Genuine leather biker jacket", 299.99, 25));
        clothing.addProduct(new Product("Summer Dress", "Lightweight floral dress", 59.99, 45));
        clothing.addProduct(new Product("Business Suit", "Professional two-piece suit", 399.99, 30));
        
        // Home & Garden Category
        Category homeGarden = new Category("Home & Garden", "Everything for your home and outdoor space");
        homeGarden = categoryRepository.save(homeGarden);
        
        homeGarden.addProduct(new Product("Coffee Maker", "Programmable drip coffee maker", 89.99, 50));
        homeGarden.addProduct(new Product("Vacuum Cleaner", "Bagless upright vacuum", 199.99, 25));
        homeGarden.addProduct(new Product("Garden Hose", "50ft flexible garden hose", 39.99, 75));
        homeGarden.addProduct(new Product("Throw Pillows", "Decorative couch cushions (set of 2)", 29.99, 90));
        homeGarden.addProduct(new Product("LED Light Bulbs", "Energy-efficient bulbs (pack of 4)", 24.99, 120));
        homeGarden.addProduct(new Product("Plant Pot Set", "Ceramic pots with drainage (set of 3)", 34.99, 65));
        
        // Books Category
        Category books = new Category("Books", "Fiction, non-fiction, and educational books");
        books = categoryRepository.save(books);
        
        books.addProduct(new Product("The Great Gatsby", "Classic American novel by F. Scott Fitzgerald", 12.99, 150));
        books.addProduct(new Product("Clean Code", "Programming best practices guide", 45.99, 80));
        books.addProduct(new Product("Atomic Habits", "Self-improvement and habit formation", 16.99, 100));
        books.addProduct(new Product("The Midnight Library", "Contemporary fiction novel", 14.99, 75));
        books.addProduct(new Product("Sapiens", "Brief history of humankind", 18.99, 90));
        
        // Sports & Fitness Category
        Category sports = new Category("Sports & Fitness", "Equipment and gear for active lifestyle");
        sports = categoryRepository.save(sports);
        
        sports.addProduct(new Product("Yoga Mat", "Non-slip exercise mat", 29.99, 85));
        sports.addProduct(new Product("Dumbbells Set", "Adjustable weight set", 149.99, 40));
        sports.addProduct(new Product("Basketball", "Official size basketball", 24.99, 60));
        sports.addProduct(new Product("Tennis Racket", "Professional grade racket", 129.99, 35));
        sports.addProduct(new Product("Running Shoes", "Lightweight performance shoes", 119.99, 70));
        
        // Save all categories with their products
        categoryRepository.save(electronics);
        categoryRepository.save(clothing);
        categoryRepository.save(homeGarden);
        categoryRepository.save(books);
        categoryRepository.save(sports);
        
        log.info("Created {} categories with {} total products", 
                categoryRepository.count(), productRepository.count());
    }
}