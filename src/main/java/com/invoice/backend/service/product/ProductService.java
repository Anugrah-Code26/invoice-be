package com.invoice.backend.service.product;

import com.invoice.backend.entity.product.Product;
import com.invoice.backend.entity.user.User;
import com.invoice.backend.infrastructure.product.dto.ProductDTO;
import com.invoice.backend.common.exceptions.DataNotFoundException;

import java.util.List;

public interface ProductService {
    Product createProduct(ProductDTO productDTO, User user);
    List<Product> getAllActiveProducts(User user);
    List<Product> getAllProducts(User user);
    Product getProductById(Long id, User user) throws DataNotFoundException;
    Product updateProduct(Long id, ProductDTO productDTO, User user) throws DataNotFoundException;
    void softDeleteProduct(Long id, User user) throws DataNotFoundException;
}
