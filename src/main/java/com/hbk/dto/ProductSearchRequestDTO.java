package com.hbk.dto;

import lombok.*;

/**
 * ProductSearchRequestDTO
 *
 * 상품 목록 검색 요청 DTO
 *
 * 필터 조건 + 페이지 조건을 함께 전달한다.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductSearchRequestDTO {

    /** 브랜드 필터 */
    private Long brandId;

    /** 카테고리 필터 */
    private Long categoryId;

    /** 제목 검색 */
    private String keyword;

    /** 최소 가격 */
    private Integer minPrice;

    /** 최대 가격 */
    private Integer maxPrice;

    /** 상품 상태 (ACTIVE 등) */
    private String status;

    /** 정렬 방식 */
    private String sort;

    /** 페이지 번호 */
    private Integer page;

    /** 페이지 크기 */
    private Integer size;
}