package com.hbk.service;

import com.hbk.dto.NavMenuRequestDTO;
import com.hbk.dto.NavMenuResponseDTO;
import com.hbk.entity.NavMenu;
import com.hbk.repository.NavMenuRepository;
import com.hbk.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class NavMenuService {

    private final NavMenuRepository navMenuRepository;
    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public List<NavMenuResponseDTO> tree() {
        List<NavMenu> roots = navMenuRepository.findByParentIsNullOrderBySortOrderAscIdAsc();

        return roots.stream()
                .map(this::toTreeDto)
                .collect(Collectors.toList());
    }

    public NavMenuResponseDTO create(NavMenuRequestDTO req) {
        String name = req.getName() == null ? "" : req.getName().trim();
        if (name.isEmpty()) {
            throw new IllegalArgumentException("name is required");
        }

        NavMenu parent = null;
        int depth = 1;

        if (req.getParentId() != null) {
            parent = navMenuRepository.findById(req.getParentId())
                    .orElseThrow(() -> new EntityNotFoundException("parent not found: " + req.getParentId()));

            depth = parent.getDepth() + 1;
            if (depth > 3) {
                throw new IllegalArgumentException("depth max is 3");
            }
        }

        Integer rootMax = navMenuRepository.maxSortOrderRoot();
        Integer childMax = parent == null ? 0 : navMenuRepository.maxSortOrderByParent(parent.getId());

        int sortOrder = req.getSortOrder() != null
                ? req.getSortOrder()
                : (parent == null ? (rootMax == null ? 0 : rootMax) + 1 : (childMax == null ? 0 : childMax) + 1);

        String visibleYn = (req.getVisibleYn() == null || req.getVisibleYn().isBlank())
                ? "Y"
                : req.getVisibleYn().trim().toUpperCase();

        String path = req.getPath();
        if (path != null) {
            path = path.trim();
            if (!path.isEmpty() && !path.startsWith("/")) {
                path = "/" + path;
            }
        }

        NavMenu saved = navMenuRepository.save(
                NavMenu.builder()
                        .name(name)
                        .path(path)
                        .visibleYn(visibleYn)
                        .sortOrder(sortOrder)
                        .depth(depth)
                        .parent(parent)
                        .build()
        );

        return toFlatDto(saved);
    }

    public void delete(Long id) {
        NavMenu menu = navMenuRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("menu not found: " + id));

        // 1. 방어 로직: 하위 카테고리가 존재하는지 확인 (자기 참조 FK 방어)
        if (menu.getChildren() != null && !menu.getChildren().isEmpty()) {
            throw new IllegalStateException("하위 카테고리가 존재하여 삭제할 수 없습니다. 하위 카테고리를 먼저 삭제해 주세요.");
        }

        // 2. 방어 로직: 해당 카테고리에 속한 상품이 존재하는지 확인 (products FK 방어)
        if (productRepository.existsByCategory_Id(id)) {
            throw new IllegalStateException("해당 카테고리에 등록된 상품이 존재하여 삭제할 수 없습니다. 상품의 카테고리를 먼저 변경하거나 삭제해 주세요.");
        }

        // 검증을 통과한 경우에만 안전하게 삭제 수행
        navMenuRepository.delete(menu);
    }

    private NavMenuResponseDTO toFlatDto(NavMenu n) {
        return NavMenuResponseDTO.builder()
                .id(n.getId())
                .parentId(n.getParent() != null ? n.getParent().getId() : null)
                .name(n.getName())
                .path(n.getPath())
                .visibleYn(n.getVisibleYn())
                .sortOrder(n.getSortOrder())
                .depth(n.getDepth())
                .children(Collections.emptyList())
                .build();
    }

    private NavMenuResponseDTO toTreeDto(NavMenu n) {
        NavMenuResponseDTO dto = toFlatDto(n);

        if (n.getChildren() != null && !n.getChildren().isEmpty()) {
            dto.setChildren(
                    n.getChildren().stream()
                            .sorted(Comparator
                                    .comparing((NavMenu m) -> m.getSortOrder() == null ? 0 : m.getSortOrder())
                                    .thenComparing(NavMenu::getId))
                            .map(this::toTreeDto)
                            .collect(Collectors.toList())
            );
        }

        return dto;
    }
}