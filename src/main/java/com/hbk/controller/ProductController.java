package com.hbk.controller;

import com.hbk.dto.PageResponseDTO;
import com.hbk.dto.ProductListItemDTO;
import com.hbk.dto.ProductResponseDTO;
import com.hbk.dto.ProductSearchRequestDTO;
import com.hbk.service.ProductQueryService;
import com.hbk.service.ProductService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * ProductController
 *
 * 기존 상품 CRUD API는 유지하면서
 * 목록 조회만 pagination + filter + sort를 지원하도록 업그레이드한 컨트롤러.
 *
 * 주의:
 * - 상세/생성/수정/삭제는 기존 ProductService 사용
 * - 목록 조회는 ProductQueryService로 분리
 */
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class ProductController {

    private final ProductService productService;
    private final ProductQueryService productQueryService;

    /**
     * 상품 목록 조회
     *
     * 예:
     * GET /api/products?page=0&size=12&sort=latest
     * GET /api/products?brandId=3&categoryId=100
     * GET /api/products?keyword=vcore&minPrice=200000&maxPrice=300000
     */
    @GetMapping
    public PageResponseDTO<ProductListItemDTO> list(
            @RequestParam(required = false) Long brandId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer minPrice,
            @RequestParam(required = false) Integer maxPrice,
            @RequestParam(required = false) String status,
            @RequestParam(required = false, defaultValue = "latest") String sort,
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "12") Integer size
    ) {
        ProductSearchRequestDTO request = ProductSearchRequestDTO.builder()
                .brandId(brandId)
                .categoryId(categoryId)
                .keyword(keyword)
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .status(status)
                .sort(sort)
                .page(page)
                .size(size)
                .build();

        return productQueryService.search(request);
    }

    /**
     * 상품 상세 조회
     */
    @GetMapping("/{id}")
    public ProductResponseDTO detail(@PathVariable Long id) {
        return productService.getById(id);
    }

    /**
     * slug 기반 상품 상세 조회
     * 프론트 상세 페이지에서 /products/{slug} 형태로 쓰기 좋다.
     */
    @GetMapping("/slug/{slug}")
    public ProductResponseDTO getBySlug(@PathVariable String slug) {
        return productService.getBySlug(slug);
    }

    /**
     * 상품 등록
     * multipart/form-data
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ProductResponseDTO create(
            @RequestParam @NotBlank String title,
            @RequestParam(required = false) String description,
            @RequestParam @NotNull Integer price,
            @RequestParam @NotNull Long brandId,
            @RequestParam @NotNull Long categoryId,
            @RequestParam @NotBlank String sizes,
            @RequestParam @NotBlank String spec,
            @RequestPart("image") MultipartFile image
    ) throws Exception {
        return productService.create(
                title,
                description,
                price,
                brandId,
                categoryId,
                sizes,
                spec,
                image
        );
    }

    /**
     * 상품 수정
     * multipart/form-data
     */
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ProductResponseDTO update(
            @PathVariable Long id,
            @RequestParam @NotBlank String title,
            @RequestParam(required = false) String description,
            @RequestParam @NotNull Integer price,
            @RequestParam(required = false) Long brandId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam @NotBlank String sizes,
            @RequestParam @NotBlank String spec,
            @RequestPart(value = "image", required = false) MultipartFile image
    ) throws Exception {
        return productService.update(
                id,
                title,
                description,
                price,
                brandId,
                categoryId,
                sizes,
                spec,
                image
        );
    }

    /**
     * 상품 삭제
     */
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        productService.delete(id);
    }
}