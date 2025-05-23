package com.invoice.backend.infrastructure.product.controller;

import com.invoice.backend.entity.product.Product;
import com.invoice.backend.entity.user.User;
import com.invoice.backend.infrastructure.product.dto.ProductDTO;
import com.invoice.backend.service.product.ProductService;
import com.invoice.backend.common.exceptions.DataNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/products")
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<Product> createProduct(
            @Valid @RequestBody ProductDTO productDTO) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(productService.createProduct(productDTO));
    }

    @GetMapping
    public ResponseEntity<List<Product>> getAllActiveProducts() {
        return ResponseEntity.ok(productService.getAllActiveProducts());
    }

    @GetMapping("/all")
    public ResponseEntity<List<Product>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(
            @PathVariable Long id) throws DataNotFoundException {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductDTO productDTO) throws DataNotFoundException {
        return ResponseEntity.ok(productService.updateProduct(id, productDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> softDeleteProduct(
            @PathVariable Long id) throws DataNotFoundException {
        productService.softDeleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}
