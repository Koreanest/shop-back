package com.hbk.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartAddRequestDTO {

    @NotNull
    private Long skuId;

    @NotNull
    @Min(1)
    private Integer quantity;
}