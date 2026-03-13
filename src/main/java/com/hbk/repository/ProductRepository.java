package com.hbk.repository;

import com.hbk.entity.NavMenu;
import com.hbk.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * ProductRepository
 *
 * 기존 CRUD 기능 + 동적 검색(Specification) 기능을 함께 지원한다.
 *
 * JpaSpecificationExecutor를 추가하면
 *  - pagination
 *  - filter
 *  - dynamic query
 * 를 모두 지원할 수 있다.
 *
 * 기존 기능은 그대로 유지되며 확장만 되는 구조이다.
 */
@Repository
public interface ProductRepository
        extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    /**
     * 특정 카테고리 상품 조회
     */
    List<Product> findByCategory(NavMenu category);

    /**
     * 카테고리 id 기반 조회
     */
    List<Product> findByCategory_Id(Long categoryId);

    /**
     * slug 기반 상품 조회
     * 상품 상세 페이지에서 사용
     */
    Optional<Product> findBySlug(String slug);

    /**
     * slug 중복 체크
     * 상품 생성 시 slug 자동 생성 로직에서 사용
     */
    boolean existsBySlug(String slug);

    /**
     * 제목 검색
     * 관리자 검색 또는 사용자 검색 기능에서 사용
     */
    List<Product> findByTitleContaining(String keyword);

    //상세 조회용 메서드
    Optional<Product> findDetailById(Long id);
}