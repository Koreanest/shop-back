package com.hbk.repository;

import com.hbk.entity.NavMenu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NavMenuRepository extends JpaRepository<NavMenu, Long> {

    // 루트(1차) 메뉴 조회
    List<NavMenu> findByParentIsNullOrderBySortOrderAscIdAsc();

    // 특정 부모의 자식 메뉴 조회
    List<NavMenu> findByParentIdOrderBySortOrderAscIdAsc(Long parentId);

    // 루트 메뉴의 최대 정렬값
    @Query("select coalesce(max(n.sortOrder), 0) from NavMenu n where n.parent is null")
    Integer maxSortOrderRoot();

    // 특정 부모 아래 자식 메뉴의 최대 정렬값
    @Query("select coalesce(max(n.sortOrder), 0) from NavMenu n where n.parent.id = :parentId")
    Integer maxSortOrderByParent(@Param("parentId") Long parentId);
}