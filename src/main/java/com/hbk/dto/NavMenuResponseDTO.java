package com.hbk.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NavMenuResponseDTO {

    private Long id;
    private Long parentId;
    private String name;
    private String path;
    private String visibleYn;
    private Integer sortOrder;
    private Integer depth;

    @Builder.Default
    private List<NavMenuResponseDTO> children = new ArrayList<>();
}