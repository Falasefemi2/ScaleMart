package com.femi.productservice.repository;

import com.femi.productservice.model.Product;
import com.femi.productservice.model.ProductStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findBySellerId(Long sellerId);
    List<Product> findByStatus(ProductStatus status);

}