package com.hbk.repository.spec;

import com.hbk.entity.Product;
import com.hbk.entity.ProductStatus;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

/**
 * ProductSpecification
 *
 * 상품 검색 필터 조건을 정의하는 클래스
 *
 * 동적 검색을 위해 Specification 패턴을 사용한다.
 */
public class ProductSpecification {

    /**
     * Brand 필터
     */
    public static Specification<Product> brandIdEq(Long brandId) {
        return (root, query, cb) ->
                brandId == null ? null :
                        cb.equal(root.get("brand").get("id"), brandId);
    }

    /**
     * Category 필터
     */
    public static Specification<Product> categoryIdEq(Long categoryId) {
        return (root, query, cb) ->
                categoryId == null ? null :
                        cb.equal(root.get("category").get("id"), categoryId);
    }

    /**
     * 제목 검색
     */
    public static Specification<Product> titleContains(String keyword) {
        return (root, query, cb) -> {

            if (keyword == null || keyword.isBlank()) return null;

            return cb.like(
                    cb.lower(root.get("title")),
                    "%" + keyword.toLowerCase() + "%"
            );
        };
    }

    /**
     * 최소 가격
     */
    public static Specification<Product> priceGoe(Integer minPrice) {
        return (root, query, cb) ->
                minPrice == null ? null :
                        cb.greaterThanOrEqualTo(root.get("price"), minPrice);
    }

    /**
     * 최대 가격
     */
    public static Specification<Product> priceLoe(Integer maxPrice) {
        return (root, query, cb) ->
                maxPrice == null ? null :
                        cb.lessThanOrEqualTo(root.get("price"), maxPrice);
    }

    /**
     * 상품 상태 필터
     */
    public static Specification<Product> statusEq(String status) {

        return (root, query, cb) -> {

            if (status == null || status.isBlank()) return null;

            return cb.equal(
                    root.get("status"),
                    ProductStatus.valueOf(status)
            );
        };
    }

    /**
     * Brand / Category join
     *
     * DTO 변환 시 N+1 문제를 방지하기 위해
     * fetch join을 적용한다.
     */
    public static Specification<Product> withJoins() {

        return (root, query, cb) -> {

            root.fetch("brand", JoinType.LEFT);
            root.fetch("category", JoinType.LEFT);

            query.distinct(true);

            return null;
        };
    }
}