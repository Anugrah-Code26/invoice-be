package com.invoice.backend.service.product;

import com.invoice.backend.entity.product.Product;
import com.invoice.backend.entity.user.User;
import com.invoice.backend.infrastructure.product.dto.ProductDTO;
import com.invoice.backend.common.exceptions.DataNotFoundException;

import java.util.List;

public interface ProductService {
    Product createProduct(ProductDTO productDTO);
    List<Product> findProducts(String search, Double minPrice, Double maxPrice);
    List<Product> getAllActiveProducts();
    List<Product> getAllProducts();
    Product getProductById(Long id) throws DataNotFoundException;
    Product updateProduct(Long id, ProductDTO productDTO) throws DataNotFoundException;
    void softDeleteProduct(Long id) throws DataNotFoundException;
}
