package com.hbk.service;

import com.hbk.dto.PageResponseDTO;
import com.hbk.dto.ProductListItemDTO;
import com.hbk.dto.ProductSearchRequestDTO;
import com.hbk.entity.Product;
import com.hbk.repository.ProductRepository;
import com.hbk.repository.spec.ProductSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * ProductQueryService
 *
 * 조회 전용 서비스
 *
 * 기존 ProductService는
 * CREATE / UPDATE / DELETE 중심
 *
 * 조회 기능을 분리하면
 *
 * 1️⃣ 서비스 책임 분리
 * 2️⃣ 조회 최적화
 * 3️⃣ 유지보수성 증가
 */
@Service
@RequiredArgsConstructor
public class ProductQueryService {

    private final ProductRepository repo;

    /**
     * 상품 검색
     */
    @Transactional(readOnly = true)
    public PageResponseDTO<ProductListItemDTO> search(ProductSearchRequestDTO req) {

        int page = req.getPage() != null ? req.getPage() : 0;
        int size = req.getSize() != null ? req.getSize() : 12;

        Pageable pageable =
                PageRequest.of(page, size, resolveSort(req.getSort()));

        Specification<Product> spec = Specification
                .where(ProductSpecification.withJoins())
                .and(ProductSpecification.brandIdEq(req.getBrandId()))
                .and(ProductSpecification.categoryIdEq(req.getCategoryId()))
                .and(ProductSpecification.titleContains(req.getKeyword()))
                .and(ProductSpecification.priceGoe(req.getMinPrice()))
                .and(ProductSpecification.priceLoe(req.getMaxPrice()))
                .and(ProductSpecification.statusEq(req.getStatus()));

        Page<ProductListItemDTO> result =
                repo.findAll(spec, pageable)
                        .map(ProductListItemDTO::from);

        return PageResponseDTO.from(result);
    }

    /**
     * 정렬 처리
     */
    private Sort resolveSort(String sort) {

        if (sort == null || sort.isBlank()) {
            return Sort.by(Sort.Direction.DESC, "id");
        }

        return switch (sort) {

            case "priceAsc" ->
                    Sort.by(Sort.Direction.ASC, "price");

            case "priceDesc" ->
                    Sort.by(Sort.Direction.DESC, "price");

            case "titleAsc" ->
                    Sort.by(Sort.Direction.ASC, "title");

            default ->
                    Sort.by(Sort.Direction.DESC, "id");
        };
    }
}