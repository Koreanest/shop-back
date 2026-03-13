package com.hbk.dto;

import lombok.*;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * PageResponseDTO
 *
 * Spring Page 객체를
 * 프론트에서 사용하기 쉬운 JSON 형태로 변환한다.
 *
 * 예시 응답
 *
 * {
 *   items: [...],
 *   page: 0,
 *   size: 12,
 *   totalElements: 40,
 *   totalPages: 4,
 *   first: true,
 *   last: false
 * }
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PageResponseDTO<T> {

    /** 실제 데이터 목록 */
    private List<T> items;

    /** 현재 페이지 */
    private int page;

    /** 페이지 크기 */
    private int size;

    /** 전체 데이터 수 */
    private long totalElements;

    /** 전체 페이지 수 */
    private int totalPages;

    /** 첫 페이지 여부 */
    private boolean first;

    /** 마지막 페이지 여부 */
    private boolean last;

    /**
     * Spring Page → DTO 변환
     */
    public static <T> PageResponseDTO<T> from(Page<T> page) {

        return PageResponseDTO.<T>builder()
                .items(page.getContent())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .build();
    }
}