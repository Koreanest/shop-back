package com.hbk.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NavMenuRequestDTO {

    private String name;
    private String path;
    private String visibleYn;
    private Long parentId;
    private Integer sortOrder;
}