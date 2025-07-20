package com.femi.productservice.controller;

import com.femi.productservice.dto.CreateProductRequest;
import com.femi.productservice.dto.ProductResponse;
import com.femi.productservice.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping("/seller/create")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ProductResponse> createProduct(
            @RequestBody CreateProductRequest request,
            Authentication authentication) {

        Long sellerId = Long.valueOf((String) authentication.getPrincipal());
        ProductResponse response = productService.createProduct(request, sellerId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/seller/my-products")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<List<ProductResponse>> getMyProducts(Authentication authentication) {
        Long sellerId = Long.valueOf((String) authentication.getPrincipal());
        return ResponseEntity.ok(productService.getProductsBySeller(sellerId));
    }

    @PostMapping("/admin/approve/{productId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductResponse> approveProduct(@PathVariable Long productId) {
        return ResponseEntity.ok(productService.approveProduct(productId));
    }

    @PostMapping("/admin/reject/{productId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductResponse> rejectProduct(@PathVariable Long productId) {
        return ResponseEntity.ok(productService.rejectProduct(productId));
    }

    @GetMapping("/admin/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ProductResponse>> getPendingProducts() {
        return ResponseEntity.ok(productService.getPendingProducts());
    }

    @GetMapping("/public/approved")
    public ResponseEntity<List<ProductResponse>> getApprovedProducts() {
        return ResponseEntity.ok(productService.getApprovedProducts());
    }
}
