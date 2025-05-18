// Add this to your Product Service as a temporary debug endpoint
// src/main/java/com/example/productservice/controller/DebugController.java

package com.example.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/debug")
public class DebugController {

    @GetMapping("/cors")
    public ResponseEntity<Map<String, String>> debugCors() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "CORS debug endpoint");
        response.put("timestamp", String.valueOf(System.currentTimeMillis()));
        response.put("service", "product-service");
        return ResponseEntity.ok(response);
    }
}