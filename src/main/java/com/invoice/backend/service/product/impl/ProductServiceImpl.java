package com.invoice.backend.service.product.impl;

import com.invoice.backend.entity.product.Product;
import com.invoice.backend.entity.user.User;
import com.invoice.backend.infrastructure.product.dto.ProductDTO;
import com.invoice.backend.infrastructure.product.repository.ProductRepository;
import com.invoice.backend.service.product.ProductService;
import com.invoice.backend.common.exceptions.DataNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;

    @Override
    public Product createProduct(ProductDTO productDTO, User user) {
        Product product = new Product();
        product.setUser(user);
        product.setName(productDTO.getName());
        product.setDescription(productDTO.getDescription());
        product.setPrice(productDTO.getPrice());
        product.setDeleted(false);

        return productRepository.save(product);
    }

    @Override
    public List<Product> getAllActiveProducts(User user) {
        return productRepository.findActiveByUserId(user.getId());
    }

    @Override
    public List<Product> getAllProducts(User user) {
        return productRepository.findByUserId(user.getId());
    }

    @Override
    public Product getProductById(Long id, User user) throws DataNotFoundException {
        return productRepository.findById(id)
                .filter(product -> product.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new DataNotFoundException("Product not found"));
    }

    @Override
    public Product updateProduct(Long id, ProductDTO productDTO, User user) throws DataNotFoundException {
        Product product = getProductById(id, user);
        product.setName(productDTO.getName());
        product.setDescription(productDTO.getDescription());
        product.setPrice(productDTO.getPrice());

        return productRepository.save(product);
    }

    @Override
    public void softDeleteProduct(Long id, User user) throws DataNotFoundException {
        Product product = getProductById(id, user);
        product.setDeleted(true);
        productRepository.save(product);
    }
}