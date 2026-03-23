package com.hbk.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hbk.dto.ProductCreateRequestDTO;
import com.hbk.dto.ProductResponseDTO;
import com.hbk.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/products")
@RequiredArgsConstructor
public class ProductAdminController {

    private final ProductService productService;
    private final ObjectMapper objectMapper;

    @PostMapping
    public ResponseEntity<Long> create(
            @RequestBody ProductCreateRequestDTO request
    ) {
        Long productId = productService.create(request);
        return ResponseEntity.ok(productId);
    }
    @PutMapping("/{id}")
    public ResponseEntity<?> update(
            @PathVariable Long id,
            @RequestBody ProductCreateRequestDTO request
    ) {
        try {
            String sizesJson = objectMapper.writeValueAsString(request.getSizes());
            String specJson = objectMapper.writeValueAsString(request.getSpec());

            ProductResponseDTO res = productService.update(
                    id,
                    request.getTitle(),
                    request.getDescription(),
                    request.getPrice(),
                    request.getBrandId(),
                    request.getCategoryId(),
                    sizesJson,
                    specJson,
                    null
            );

            return ResponseEntity.ok(res);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
