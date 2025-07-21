package com.femi.productservice.service;

import com.femi.productservice.dto.CreateProductRequest;
import com.femi.productservice.dto.ProductResponse;
import com.femi.productservice.model.Product;
import com.femi.productservice.model.ProductStatus;
import com.femi.productservice.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    public ProductResponse createProduct(CreateProductRequest request, Long sellerId) {
        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .stockQuantity(request.getStockQuantity())
                .sellerId(sellerId)
                .imageUrls(request.getImageUrls())
                .category(request.getCategory())
                .status(ProductStatus.PENDING)
                .build();

        Product saved = productRepository.save(product);

        return ProductResponse.builder()
                .id(saved.getId())
                .name(saved.getName())
                .description(saved.getDescription())
                .price(saved.getPrice())
                .stockQuantity(saved.getStockQuantity())
                .sellerId(saved.getSellerId())
                .imageUrls(saved.getImageUrls())
                .category(saved.getCategory())
                .status(saved.getStatus())
                .createdAt(saved.getCreatedAt())
                .build();
    }

    public List<ProductResponse> getProductsBySeller(Long sellerId) {
        return productRepository.findBySellerId(sellerId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public ProductResponse approveProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));

        product.setStatus(ProductStatus.APPROVED);
        product.setUpdatedAt(LocalDateTime.now());

        Product updated = productRepository.save(product);

        return toDto(updated);
    }

    public ProductResponse rejectProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));

        product.setStatus(ProductStatus.REJECTED);
        product.setUpdatedAt(LocalDateTime.now());

        Product updated = productRepository.save(product);

        return toDto(updated);
    }

    public List<ProductResponse> getPendingProducts() {
        return productRepository.findByStatus(ProductStatus.PENDING)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<ProductResponse> getApprovedProducts() {
        return productRepository.findByStatus(ProductStatus.APPROVED)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));
        return toDto(product);
    }


    private ProductResponse toDto(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .stockQuantity(product.getStockQuantity())
                .sellerId(product.getSellerId())
                .imageUrls(product.getImageUrls())
                .category(product.getCategory())
                .status(product.getStatus())
                .createdAt(product.getCreatedAt())
                .build();
    }
}
